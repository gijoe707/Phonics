package com.example.phonics_ta_in.core.speech

import com.example.phonics_ta_in.core.speech.AcousticSoundMatcher.Verdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AcousticSoundMatcherTest {

    /** What TTS would say: an adult voice (pitch 120 Hz), 0.6 s. */
    private val references: Map<String, Array<FloatArray>> = SyntheticSpeech.VOWELS.associateWith {
        val audio = SyntheticSpeech.vowel(it, f0 = 120f, seconds = 0.6f)
        assertNotNull("reference for $it", AcousticSoundMatcher.fingerprintOf(audio, SyntheticSpeech.SR))
        AcousticSoundMatcher.fingerprintOf(audio, SyntheticSpeech.SR)!!
    }

    private fun evaluate(recording: FloatArray, target: String, requireVoicing: Boolean = true) =
        AcousticSoundMatcher.evaluate(
            recording = recording,
            target = references.getValue(target),
            peers = references.filterKeys { it != target }.values,
            requireVoicing = requireVoicing
        )

    private class Speaker(val name: String, val f0: Float, val seconds: Float, val formantScale: Float, val noise: Float, val gain: Float)

    // Different pitch, different vocal-tract length, different speed, different loudness, some room noise.
    private val speakers = listOf(
        Speaker("small child", f0 = 260f, seconds = 0.9f, formantScale = 1.25f, noise = 0.01f, gain = 0.3f),
        Speaker("young child, short sound", f0 = 300f, seconds = 0.5f, formantScale = 1.30f, noise = 0.005f, gain = 0.8f),
        Speaker("woman", f0 = 200f, seconds = 0.7f, formantScale = 1.15f, noise = 0.005f, gain = 0.5f),
        Speaker("man", f0 = 110f, seconds = 0.6f, formantScale = 0.95f, noise = 0.005f, gain = 0.6f)
    )

    @Test
    fun everyVowelIsAcceptedForItselfAndRejectedForTheOthers_acrossSpeakers() {
        val problems = mutableListOf<String>()
        for (speaker in speakers) {
            for (spoken in SyntheticSpeech.VOWELS) {
                val recording = SyntheticSpeech.vowel(
                    spoken, speaker.f0, speaker.seconds, speaker.formantScale, speaker.noise, speaker.gain, seed = 3
                )
                for (target in SyntheticSpeech.VOWELS) {
                    val result = evaluate(recording, target)
                    val accepted = result.verdict == Verdict.MATCH
                    if (spoken == target && !accepted) {
                        problems += "${speaker.name}: said '$spoken' but target '$target' was rejected (${result.verdict}: ${result.detail})"
                    }
                    if (spoken != target && accepted) {
                        problems += "${speaker.name}: said '$spoken' but target '$target' was accepted (${result.detail})"
                    }
                }
            }
        }
        assertTrue(problems.joinToString("\n"), problems.isEmpty())
    }

    @Test
    fun silenceIsTooQuiet() {
        val result = evaluate(FloatArray(SyntheticSpeech.SR * 2), "a")
        assertEquals(Verdict.TOO_QUIET, result.verdict)
    }

    @Test
    fun aVeryShortBlipIsTooShort() {
        val blip = FloatArray(SyntheticSpeech.SR) // 1 s of silence with a 50 ms burst in it
        for (i in 8000 until 8800) blip[i] = 0.4f * kotlin.math.sin(i * 0.2f)
        val result = evaluate(blip, "a", requireVoicing = false)
        assertEquals(result.detail, Verdict.TOO_SHORT, result.verdict)
    }

    @Test
    fun hissingIsNotAcceptedAsAVowel() {
        // "shh" has energy in the same region as the vowel i, so only the voicing check keeps it out
        val shh = SyntheticSpeech.hiss(centerHz = 3500f, seconds = 0.5f)
        for (target in SyntheticSpeech.VOWELS) {
            val result = evaluate(shh, target, requireVoicing = true)
            assertEquals("target $target: ${result.detail}", Verdict.NOT_VOICED, result.verdict)
        }
    }

    @Test
    fun withoutEnoughSiblingReferencesTheCheckStepsAside() {
        val recording = SyntheticSpeech.vowel("a", 260f, 0.8f, 1.25f)
        val result = AcousticSoundMatcher.evaluate(
            recording = recording,
            target = references.getValue("a"),
            peers = listOf(references.getValue("i"), references.getValue("u"))
        )
        assertEquals(Verdict.NOT_ENOUGH_REFERENCES, result.verdict)
    }

    @Test
    fun voicedFractionSeparatesVowelsFromHiss() {
        for (f0 in listOf(110f, 200f, 260f, 330f, 400f)) {
            val vowel = AcousticSoundMatcher.prepare(SyntheticSpeech.vowel("a", f0, 0.8f, 1.2f, noise = 0.1f), SyntheticSpeech.SR)!!
            val voiced = SoundFingerprint.voicedFraction(vowel)
            assertTrue("vowel at $f0 Hz scored $voiced", voiced >= 0.8f)
        }
        for (center in listOf(2500f, 4500f)) {
            val hiss = AcousticSoundMatcher.prepare(SyntheticSpeech.hiss(center, 0.5f), SyntheticSpeech.SR)!!
            val voiced = SoundFingerprint.voicedFraction(hiss)
            assertTrue("hiss at $center Hz scored $voiced", voiced <= 0.3f)
        }
    }
}
