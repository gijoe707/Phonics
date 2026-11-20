package com.example.phonics_ta_in.core.speech

import com.example.phonics_ta_in.core.curriculum.CurriculumRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SoundSiblingsTest {

    private fun peerTransliterations(unitId: String): List<String> {
        val unit = CurriculumRepository.getUnit(unitId)!!
        return SoundSiblings.comparisonPeers(unit, CurriculumRepository.poolFor(unit)).map { it.transliteration }
    }

    @Test
    fun shortVowelLeavesOutItselfAndItsLongTwin() {
        val peers = peerTransliterations("unit_a")
        assertFalse("a" in peers)
        assertFalse("aa" in peers)
        assertEquals(10, peers.size)
    }

    @Test
    fun longVowelLeavesOutItselfAndItsShortTwin() {
        val peers = peerTransliterations("unit_ii")
        assertFalse("i" in peers)
        assertFalse("ii" in peers)
        assertTrue("a" in peers)
        assertEquals(10, peers.size)
    }

    @Test
    fun diphthongHasNoTwin_theSimilarSingleVowelsStayInTheComparison() {
        val peers = peerTransliterations("unit_ai")
        assertTrue("i" in peers)
        assertTrue("a" in peers)
        assertEquals(11, peers.size)
    }

    @Test
    fun combinedLettersPairUpToo() {
        val family = CurriculumRepository.getFamily(1)!!   // க் and க, கா, கி ...
        val kaa = family.uyirmei.first { it.transliteration == "kaa" }
        val peers = SoundSiblings.comparisonPeers(kaa, CurriculumRepository.poolFor(kaa)).map { it.transliteration }
        assertFalse("ka" in peers)
        assertFalse("kaa" in peers)
        assertEquals(10, peers.size)
    }

    @Test
    fun consonantsWithPulliHaveNoTwin() {
        assertEquals(17, peerTransliterations("unit_ik").size)
    }

    @Test
    fun everyLetterHasEnoughSiblingsForTheComparison() {
        for (unit in CurriculumRepository.ALL_UNITS) {
            val peers = SoundSiblings.comparisonPeers(unit, CurriculumRepository.poolFor(unit))
            assertTrue("${unit.id} (${unit.text}) has only ${peers.size} peers", peers.size >= AcousticSoundMatcher.MIN_PEERS)
            assertFalse("${unit.id} is its own sibling", peers.any { it.id == unit.id })
        }
    }
}
