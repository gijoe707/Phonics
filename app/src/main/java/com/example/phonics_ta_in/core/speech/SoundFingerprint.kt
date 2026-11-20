package com.example.phonics_ta_in.core.speech

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Acoustic "fingerprint" of a short sound: a sequence of MFCC frames, compared with DTW.
 *
 * Used to check whether a child's isolated vowel/consonant sound resembles the sound that
 * Android text-to-speech produces for the same letter. Pure Kotlin (no Android classes) so it can be unit tested.
 *
 * Note: no per-utterance cepstral mean normalisation on purpose. For a single steady vowel it would subtract
 * the vowel's own spectrum and make every vowel look identical. Gain is already removed by dropping c0.
 */
object SoundFingerprint {

    const val SAMPLE_RATE = 16_000

    /** Fewer frames than this (about 60 ms of sound) is not enough to compare. */
    const val MIN_FRAMES = 6

    private const val FRAME_LEN = 400      // 25 ms
    private const val HOP = 160            // 10 ms
    private const val FFT_SIZE = 512
    private const val NUM_MEL = 26
    private const val NUM_CEPS = 12        // c1..c12 (c0 dropped = loudness)
    private const val INF = 1e30f

    // Voicing detector (see voicedFraction)
    private const val VOICING_FRAME = 640          // 40 ms: needs at least two periods of the lowest pitch
    private const val MIN_PITCH_LAG = SAMPLE_RATE / 500   // highest pitch searched: 500 Hz
    private const val MAX_PITCH_LAG = SAMPLE_RATE / 70    // lowest pitch searched: 70 Hz
    private const val VOICING_THRESHOLD = 0.45f

    private val hamming = FloatArray(FRAME_LEN) {
        (0.54 - 0.46 * cos(2.0 * PI * it / (FRAME_LEN - 1))).toFloat()
    }

    private val melBanks = java.util.concurrent.ConcurrentHashMap<Int, Array<FloatArray>>()

    /** Mel filterbank whose frequency axis is scaled by [warp] (1.0 = unwarped); cached per warp factor. */
    private fun melBankFor(warp: Float): Array<FloatArray> =
        melBanks.getOrPut((warp * 1000f).roundToInt()) { buildMelBank(warp.toDouble()) }

    private val dctBasis: Array<FloatArray> by lazy {
        Array(NUM_CEPS) { k ->
            FloatArray(NUM_MEL) { n -> cos(PI * (k + 1) * (n + 0.5) / NUM_MEL).toFloat() }
        }
    }

    /** Standard cepstral liftering (L = 22): tones down the high, speaker-specific coefficients. */
    private val lifter = FloatArray(NUM_CEPS) { (1.0 + 11.0 * sin(PI * (it + 1) / 22.0)).toFloat() }

    // ---------------------------------------------------------------------------------------------
    // Pre-processing
    // ---------------------------------------------------------------------------------------------

    /** Linear-interpolation resampler (good enough for speech features). */
    fun resample(input: FloatArray, fromRate: Int, toRate: Int = SAMPLE_RATE): FloatArray {
        if (input.isEmpty() || fromRate == toRate || fromRate <= 0) return input
        val ratio = fromRate.toDouble() / toRate
        val outLen = (input.size / ratio).toInt()
        val out = FloatArray(outLen)
        for (i in 0 until outLen) {
            val pos = i * ratio
            val i0 = pos.toInt()
            val frac = (pos - i0).toFloat()
            val a = input[i0]
            val b = if (i0 + 1 < input.size) input[i0 + 1] else a
            out[i] = a + (b - a) * frac
        }
        return out
    }

    private fun frameRms(x: FloatArray, frame: Int = 320, hop: Int = 160): FloatArray {
        if (x.size < frame) return FloatArray(0)
        val n = (x.size - frame) / hop + 1
        return FloatArray(n) { f ->
            var s = 0f
            val start = f * hop
            for (i in start until start + frame) s += x[i] * x[i]
            sqrt(s / frame)
        }
    }

    /** Loudest 20 ms frame (RMS, 0..1). Used to reject silent recordings. */
    fun peakRms(x: FloatArray): Float {
        var peak = 0f
        for (v in frameRms(x)) if (v > peak) peak = v
        return peak
    }

    /**
     * Length in samples of the audible part of [x] (first to last frame above [relativeThreshold] of the loudest
     * frame), measured BEFORE [trimSilence] adds its safety padding. 0 for a silent signal.
     */
    fun activeLength(x: FloatArray, relativeThreshold: Float = 0.12f): Int {
        val rms = frameRms(x)
        if (rms.isEmpty()) return 0
        var peak = 0f
        for (v in rms) if (v > peak) peak = v
        if (peak < 1e-4f) return 0
        val threshold = peak * relativeThreshold
        val first = rms.indexOfFirst { it >= threshold }
        val last = rms.indexOfLast { it >= threshold }
        return (last - first) * 160 + 320
    }

    /** Cuts leading/trailing silence. Returns an empty array for a silent signal. */
    fun trimSilence(x: FloatArray, relativeThreshold: Float = 0.12f): FloatArray {
        val rms = frameRms(x)
        if (rms.isEmpty()) return x
        var peak = 0f
        for (v in rms) if (v > peak) peak = v
        if (peak < 1e-4f) return FloatArray(0)

        val threshold = peak * relativeThreshold
        var first = rms.indexOfFirst { it >= threshold }
        var last = rms.indexOfLast { it >= threshold }
        first = max(0, first - 2)
        last = min(rms.size - 1, last + 2)
        val start = first * 160
        val end = min(x.size, last * 160 + 320)
        return x.copyOfRange(start, end)
    }

    // ---------------------------------------------------------------------------------------------
    // Features
    // ---------------------------------------------------------------------------------------------

    /**
     * MFCC frames (c1..c12, liftered) of 16 kHz mono audio. Empty if the audio is too short.
     *
     * [warp] scales the frequency axis of the filterbank (vocal-tract-length normalisation): a child's formants sit
     * 15-30% higher than an adult TTS voice's, so the child's audio is analysed with warp > 1 to line its vowels up
     * with the reference. 1.0 leaves the spectrum unwarped (use that for the references).
     */
    fun mfcc(samples: FloatArray, warp: Float = 1f): Array<FloatArray> {
        if (samples.size < FRAME_LEN) return emptyArray()
        val melBank = melBankFor(warp)

        val emphasised = FloatArray(samples.size)
        emphasised[0] = samples[0]
        for (i in 1 until samples.size) emphasised[i] = samples[i] - 0.97f * samples[i - 1]

        val frameCount = (emphasised.size - FRAME_LEN) / HOP + 1
        val features = Array(frameCount) { FloatArray(NUM_CEPS) }
        val re = FloatArray(FFT_SIZE)
        val im = FloatArray(FFT_SIZE)
        val bins = FFT_SIZE / 2 + 1
        val power = FloatArray(bins)
        val logMel = FloatArray(NUM_MEL)

        for (f in 0 until frameCount) {
            val start = f * HOP
            re.fill(0f)
            im.fill(0f)
            for (i in 0 until FRAME_LEN) re[i] = emphasised[start + i] * hamming[i]
            fft(re, im)
            for (k in 0 until bins) power[k] = re[k] * re[k] + im[k] * im[k]

            for (m in 0 until NUM_MEL) {
                val weights = melBank[m]
                var energy = 0f
                for (k in 0 until bins) energy += weights[k] * power[k]
                logMel[m] = ln(energy + 1e-8f)
            }
            for (c in 0 until NUM_CEPS) {
                val basis = dctBasis[c]
                var sum = 0f
                for (n in 0 until NUM_MEL) sum += basis[n] * logMel[n]
                features[f][c] = sum * lifter[c]
            }
        }
        return features
    }

    /**
     * Dynamic-time-warping distance between two feature sequences (so a slow and a quick "aa" still match),
     * normalised by path length. Smaller = more similar.
     */
    fun distance(a: Array<FloatArray>, b: Array<FloatArray>): Float {
        if (a.isEmpty() || b.isEmpty()) return Float.MAX_VALUE
        val n = a.size
        val m = b.size
        var prev = FloatArray(m + 1) { INF }
        var cur = FloatArray(m + 1)
        prev[0] = 0f
        for (i in 1..n) {
            cur[0] = INF
            for (j in 1..m) {
                val best = min(prev[j], min(cur[j - 1], prev[j - 1]))
                cur[j] = euclidean(a[i - 1], b[j - 1]) + best
            }
            val tmp = prev
            prev = cur
            cur = tmp
        }
        return prev[m] / (n + m)
    }

    /**
     * Fraction (0..1) of the louder frames of 16 kHz audio [x] that are voiced: periodic with a pitch between 70 and
     * 500 Hz (normalised autocorrelation peak >= 0.45). Vowels and humming score close to 1; hissing sounds such as
     * "s" / "sh", breath and background noise score close to 0. Needs at least 40 ms of audio.
     */
    fun voicedFraction(x: FloatArray): Float {
        if (x.size < VOICING_FRAME) return 0f
        val n = (x.size - VOICING_FRAME) / HOP + 1

        val rms = FloatArray(n)
        var peak = 0f
        for (f in 0 until n) {
            var s = 0f
            val start = f * HOP
            for (i in start until start + VOICING_FRAME) s += x[i] * x[i]
            rms[f] = sqrt(s / VOICING_FRAME)
            if (rms[f] > peak) peak = rms[f]
        }
        if (peak < 1e-4f) return 0f

        val seg = FloatArray(VOICING_FRAME)
        var active = 0
        var voiced = 0
        for (f in 0 until n) {
            if (rms[f] < 0.25f * peak) continue // only judge the loud part, not the silence around the sound
            val start = f * HOP
            var mean = 0f
            for (i in 0 until VOICING_FRAME) mean += x[start + i]
            mean /= VOICING_FRAME
            for (i in 0 until VOICING_FRAME) seg[i] = x[start + i] - mean
            active++

            var best = 0f
            for (lag in MIN_PITCH_LAG..MAX_PITCH_LAG) {
                var cross = 0f
                var e1 = 0f
                var e2 = 0f
                for (i in 0 until VOICING_FRAME - lag) {
                    val a = seg[i]
                    val b = seg[i + lag]
                    cross += a * b
                    e1 += a * a
                    e2 += b * b
                }
                val denom = sqrt(e1 * e2)
                if (denom > 1e-9f) {
                    val r = cross / denom
                    if (r > best) best = r
                }
            }
            if (best >= VOICING_THRESHOLD) voiced++
        }
        return if (active == 0) 0f else voiced.toFloat() / active
    }

    private fun euclidean(x: FloatArray, y: FloatArray): Float {
        var s = 0f
        for (i in x.indices) {
            val d = x[i] - y[i]
            s += d * d
        }
        return sqrt(s)
    }

    // ---------------------------------------------------------------------------------------------
    // Internals
    // ---------------------------------------------------------------------------------------------

    private fun hzToMel(hz: Double) = 2595.0 * log10(1.0 + hz / 700.0)
    private fun melToHz(mel: Double) = 700.0 * (10.0.pow(mel / 2595.0) - 1.0)

    private fun buildMelBank(warp: Double): Array<FloatArray> {
        val bins = FFT_SIZE / 2 + 1
        val lo = hzToMel(80.0)
        val hi = hzToMel(7600.0)
        val points = DoubleArray(NUM_MEL + 2) { melToHz(lo + (hi - lo) * it / (NUM_MEL + 1)) * warp }
        return Array(NUM_MEL) { m ->
            val left = points[m]
            val center = points[m + 1]
            val right = points[m + 2]
            FloatArray(bins) { k ->
                val f = k.toDouble() * SAMPLE_RATE / FFT_SIZE
                when {
                    f <= left || f >= right -> 0f
                    f <= center -> ((f - left) / (center - left)).toFloat()
                    else -> ((right - f) / (right - center)).toFloat()
                }
            }
        }
    }

    /** In-place iterative radix-2 FFT. Size must be a power of two. */
    private fun fft(re: FloatArray, im: FloatArray) {
        val n = re.size
        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while ((j and bit) != 0) {
                j = j xor bit
                bit = bit shr 1
            }
            j = j xor bit
            if (i < j) {
                val tr = re[i]; re[i] = re[j]; re[j] = tr
                val ti = im[i]; im[i] = im[j]; im[j] = ti
            }
        }
        var len = 2
        while (len <= n) {
            val half = len / 2
            val angle = -2.0 * PI / len
            val stepRe = cos(angle).toFloat()
            val stepIm = sin(angle).toFloat()
            var i = 0
            while (i < n) {
                var wr = 1f
                var wi = 0f
                for (k in 0 until half) {
                    val a = i + k
                    val b = a + half
                    val vr = re[b] * wr - im[b] * wi
                    val vi = re[b] * wi + im[b] * wr
                    re[b] = re[a] - vr
                    im[b] = im[a] - vi
                    re[a] = re[a] + vr
                    im[a] = im[a] + vi
                    val nextWr = wr * stepRe - wi * stepIm
                    wi = wr * stepIm + wi * stepRe
                    wr = nextWr
                }
                i += len
            }
            len = len shl 1
        }
    }
}
