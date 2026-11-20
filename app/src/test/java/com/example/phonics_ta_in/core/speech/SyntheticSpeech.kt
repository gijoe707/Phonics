package com.example.phonics_ta_in.core.speech

import java.util.Random
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Test-only synthetic speech: vowels are a harmonic series (the "voice") shaped by three formant resonances, which is
 * what distinguishes one vowel from another. Scaling the formants and the pitch imitates a different speaker:
 * a child has a higher pitch AND a shorter vocal tract (formants about 15-30% higher) than an adult TTS voice.
 */
internal object SyntheticSpeech {

    const val SR = SoundFingerprint.SAMPLE_RATE

    /** F1, F2, F3 of an adult voice in Hz. */
    private val FORMANTS = mapOf(
        "a" to floatArrayOf(700f, 1220f, 2600f),
        "i" to floatArrayOf(300f, 2300f, 3000f),
        "u" to floatArrayOf(320f, 800f, 2250f),
        "e" to floatArrayOf(530f, 1850f, 2500f),
        "o" to floatArrayOf(570f, 840f, 2400f)
    )
    private val BANDWIDTHS = floatArrayOf(90f, 110f, 160f)

    val VOWELS = FORMANTS.keys.toList()

    /**
     * 150 ms of silence + the vowel + 500 ms of silence (like a real recording / TTS file).
     * @param noise standard deviation of added white noise, relative to a vowel peak of 1.0
     * @param gain  overall loudness applied last
     */
    fun vowel(
        name: String,
        f0: Float,
        seconds: Float,
        formantScale: Float = 1f,
        noise: Float = 0f,
        gain: Float = 1f,
        seed: Long = 0
    ): FloatArray {
        val formants = FORMANTS.getValue(name).map { it * formantScale }
        val n = (seconds * SR).toInt()
        val x = FloatArray(n)
        var phase = 0.0
        for (i in 0 until n) {
            val t = i.toDouble() / SR
            val vibrato = 1.0 + 0.01 * sin(2 * PI * 5 * t)
            phase += 2 * PI * f0 * vibrato / SR
            var sum = 0.0
            var k = 1
            while (k * f0 < 5500f) {
                val fk = k * f0
                var g = 0.0
                for (j in 0 until 3) {
                    val d = (fk - formants[j]) / BANDWIDTHS[j]
                    g += 1.0 / (1.0 + d * d)
                }
                sum += g / k * sin(k * phase)
                k++
            }
            val fade = min(1.0, min(t / 0.05, (seconds - t) / 0.05))
            x[i] = (sum * fade).toFloat()
        }
        normalise(x)
        val rng = Random(seed)
        for (i in 0 until n) x[i] += noise * rng.nextGaussian().toFloat()
        return withSilence(x, gain)
    }

    /** Band-limited noise ("sss" / "shh"): hiss around [centerHz], not periodic. */
    fun hiss(centerHz: Float, seconds: Float, seed: Long = 1): FloatArray {
        val n = (seconds * SR).toInt()
        val rng = Random(seed)
        val r = 0.9f
        val a1 = 2f * r * cos(2 * PI * centerHz / SR).toFloat()
        val a2 = -r * r
        val x = FloatArray(n)
        var y1 = 0f
        var y2 = 0f
        for (i in 0 until n) {
            val y = rng.nextGaussian().toFloat() + a1 * y1 + a2 * y2
            x[i] = y
            y2 = y1
            y1 = y
        }
        normalise(x)
        return withSilence(x, 0.5f)
    }

    private fun normalise(x: FloatArray) {
        var peak = 0f
        for (v in x) peak = max(peak, abs(v))
        if (peak > 0f) for (i in x.indices) x[i] /= peak
    }

    private fun withSilence(x: FloatArray, gain: Float): FloatArray {
        val lead = (0.15f * SR).toInt()
        val tail = (0.5f * SR).toInt()
        val out = FloatArray(lead + x.size + tail)
        for (i in x.indices) out[lead + i] = x[i] * gain
        return out
    }
}
