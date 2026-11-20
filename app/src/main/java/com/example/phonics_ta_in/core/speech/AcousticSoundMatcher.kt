package com.example.phonics_ta_in.core.speech

import kotlin.math.abs

/**
 * Decides whether a child's recorded isolated sound is the target letter's sound, by comparing it with what
 * Android text-to-speech says for that letter.
 *
 * Why not just threshold the distance to the target? A child's voice differs a lot from the TTS voice (pitch, and
 * above all a shorter vocal tract: the formants that make an "i" an "i" sit 15-30% higher), so the absolute distance
 * is never small and varies from voice to voice and device to device. Three things make the comparison usable:
 *
 *  1. VTLN: the child's audio is analysed with several frequency-axis warps ([WARPS]); the best-aligned one is used
 *     for each reference, which cancels most of the speaker difference.
 *  2. The child's sound is compared with the TTS reference of the target AND of its sibling letters (other vowels,
 *     other consonants, ...). It passes when the target is the closest reference (or practically tied for closest)
 *     and clearly closer than the typical sibling. The caller leaves the target's short/long twin (அ / ஆ, க / கா ...)
 *     out of the siblings: the two differ in duration, not in sound quality, so they cannot be told apart this way.
 *  3. For vowels the caller can require voicing ([evaluate]'s requireVoicing): a hissing "sh" has a similar spectrum
 *     to an "i" but is not periodic.
 *
 * On synthetic voices (adult TTS-like reference vs. child/female/noisy speakers, 10 Tamil vowels) this accepted
 * the right vowel every time with about 1 wrong accept in 400. Real voices will be messier: the constants below are
 * the first things to tune, and [Result.detail] (logged by the app) shows the numbers needed to do it.
 *
 * Pure Kotlin: no Android classes, no logging, so it can be unit tested.
 */
object AcousticSoundMatcher {

    enum class Verdict {
        MATCH,
        NO_MATCH,
        TOO_QUIET,
        TOO_SHORT,
        /** A vowel was expected but the sound is not voiced (hissing, breath, noise). */
        NOT_VOICED,
        NOT_ENOUGH_REFERENCES
    }

    class Result(
        val verdict: Verdict,
        val targetDistance: Float = Float.NaN,
        val bestPeerDistance: Float = Float.NaN,
        val medianPeerDistance: Float = Float.NaN,
        /** 1 = the target reference was the closest of all references. */
        val targetRank: Int = 0,
        val peerCount: Int = 0,
        val detail: String = ""
    )

    // --- Tunables (all relative, so they do not depend on the device's microphone gain) ---

    /** Recordings quieter than this (peak RMS, 0..1) are treated as silence. */
    const val MIN_PEAK_RMS = 0.01f

    /** Fewer sibling references than this and the comparison is not trustworthy -> use the word fallback. */
    const val MIN_PEERS = 3

    /** Target distance may be at most this much larger than the best sibling's and still count as "closest". */
    const val TIE_MARGIN = 1.06f

    /** Target distance must also be below this fraction of the median sibling distance (stands out from the crowd). */
    const val STAND_OUT_RATIO = 0.90f

    /** With requireVoicing: at least this fraction of the loud frames must be voiced. */
    const val MIN_VOICED_FRACTION = 0.5f

    /** Frequency-axis warps tried on the child's audio (1.0 = as recorded; > 1 compensates a shorter vocal tract). */
    val WARPS = floatArrayOf(0.85f, 0.92f, 1.0f, 1.08f, 1.16f, 1.24f, 1.32f, 1.40f)

    /**
     * A recording whose audible part is shorter than this (80 ms) is a click or a cough, not a letter. Measured before
     * the silence-trimming padding, which would otherwise make any click look long enough. Applies to the child's
     * recording only: TTS references (some consonants are very short) are not subject to it.
     */
    const val MIN_ACTIVE_SAMPLES = SoundFingerprint.SAMPLE_RATE * 8 / 100

    /** Audio cut after this length: a one-letter sound never needs more than ~2.5 s. */
    private const val MAX_SAMPLES = SoundFingerprint.SAMPLE_RATE * 5 / 2

    /** Peak the audio is scaled to before the features are computed (keeps the log-mel floor out of the way). */
    private const val TARGET_PEAK = 0.5f

    /**
     * Resamples to 16 kHz, trims the silence around the sound, caps the length and scales to a fixed peak.
     * Returns null when nothing usable is left (silence, or shorter than [SoundFingerprint.MIN_FRAMES] frames).
     */
    fun prepare(samples: FloatArray, sampleRate: Int, minActiveSamples: Int = 0): FloatArray? {
        if (samples.isEmpty()) return null
        val resampled = SoundFingerprint.resample(samples, sampleRate)
        if (minActiveSamples > 0 && SoundFingerprint.activeLength(resampled) < minActiveSamples) return null
        val trimmed = SoundFingerprint.trimSilence(resampled)
        if (trimmed.isEmpty()) return null
        val limited = if (trimmed.size > MAX_SAMPLES) trimmed.copyOfRange(0, MAX_SAMPLES) else trimmed
        val scaled = scaleToPeak(limited)
        val frames = (scaled.size - 400) / 160 + 1 // same framing as SoundFingerprint.mfcc
        return if (frames >= SoundFingerprint.MIN_FRAMES) scaled else null
    }

    /**
     * Fingerprint of a TTS reference (unwarped MFCC frames), or null when the audio is silent/too short.
     */
    fun fingerprintOf(samples: FloatArray, sampleRate: Int): Array<FloatArray>? {
        val prepared = prepare(samples, sampleRate) ?: return null
        val frames = SoundFingerprint.mfcc(prepared)
        return if (frames.size >= SoundFingerprint.MIN_FRAMES) frames else null
    }

    /**
     * @param recording     16 kHz mono recording of the child
     * @param target        fingerprint of the TTS reference for the target letter
     * @param peers         fingerprints of the TTS references of the sibling letters (without the target and without
     *                      the target's short/long twin)
     * @param requireVoicing true for vowel targets: reject sounds that are not periodic (voiced)
     */
    fun evaluate(
        recording: FloatArray,
        target: Array<FloatArray>,
        peers: Collection<Array<FloatArray>>,
        requireVoicing: Boolean = false
    ): Result {
        if (peers.size < MIN_PEERS) {
            return Result(Verdict.NOT_ENOUGH_REFERENCES, peerCount = peers.size, detail = "only ${peers.size} sibling references")
        }
        val peak = SoundFingerprint.peakRms(recording)
        if (peak < MIN_PEAK_RMS) {
            return Result(Verdict.TOO_QUIET, peerCount = peers.size, detail = "peakRms=$peak")
        }
        val prepared = prepare(recording, SoundFingerprint.SAMPLE_RATE, MIN_ACTIVE_SAMPLES)
            ?: return Result(Verdict.TOO_SHORT, peerCount = peers.size, detail = "recording too short (under 80 ms of sound)")

        var voicedInfo = ""
        if (requireVoicing) {
            val voiced = SoundFingerprint.voicedFraction(prepared)
            voicedInfo = " voiced=%.2f".format(voiced)
            if (voiced < MIN_VOICED_FRACTION) {
                return Result(Verdict.NOT_VOICED, peerCount = peers.size, detail = "voiced=%.2f".format(voiced))
            }
        }

        val warped = WARPS.map { SoundFingerprint.mfcc(prepared, it) }
        fun distanceTo(reference: Array<FloatArray>): Float {
            var best = Float.MAX_VALUE
            for (child in warped) {
                val d = SoundFingerprint.distance(child, reference)
                if (d < best) best = d
            }
            return best
        }

        val dTarget = distanceTo(target)
        val dPeers = peers.map { distanceTo(it) }.sorted()
        val best = dPeers.first()
        val median = median(dPeers)
        val rank = 1 + dPeers.count { it < dTarget }

        val isClosest = dTarget <= best * TIE_MARGIN
        val standsOut = dTarget <= median * STAND_OUT_RATIO
        val verdict = if (isClosest && standsOut) Verdict.MATCH else Verdict.NO_MATCH

        val detail = "target=%.2f bestPeer=%.2f medianPeer=%.2f rank=%d/%d closest=%b standsOut=%b frames=%d%s".format(
            dTarget, best, median, rank, dPeers.size + 1, isClosest, standsOut, warped[0].size, voicedInfo
        )
        return Result(verdict, dTarget, best, median, rank, peers.size, detail)
    }

    private fun median(sorted: List<Float>): Float {
        val n = sorted.size
        return if (n % 2 == 1) sorted[n / 2] else (sorted[n / 2 - 1] + sorted[n / 2]) / 2f
    }

    private fun scaleToPeak(x: FloatArray): FloatArray {
        var peak = 0f
        for (v in x) if (abs(v) > peak) peak = abs(v)
        if (peak < 1e-6f) return x
        val g = TARGET_PEAK / peak
        return FloatArray(x.size) { x[it] * g }
    }
}
