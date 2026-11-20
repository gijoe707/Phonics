package com.example.phonics_ta_in.core.curriculum

import com.example.phonics_ta_in.core.text.TamilLetterType
import com.example.phonics_ta_in.core.text.TamilTextSegmenter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurriculumRepositoryTest {

    @Test
    fun alphabetHasAll247Letters() {
        // 12 uyir + 1 aytham + 18 mei + (18 x 12) uyirmei = 247
        assertEquals(12, CurriculumRepository.VOWEL_UNITS.size)
        assertEquals(18, CurriculumRepository.CONSONANT_FAMILIES.size)
        CurriculumRepository.CONSONANT_FAMILIES.forEach { family ->
            assertEquals("uyirmei count for ${family.mei.text}", 12, family.uyirmei.size)
            assertEquals("letters in family ${family.mei.text}", 13, family.units.size)
        }
        assertEquals(247, CurriculumRepository.ALL_UNITS.size)
    }

    @Test
    fun everyLetterHasUniqueIdAndText() {
        val units = CurriculumRepository.ALL_UNITS
        assertEquals("duplicate ids", units.size, units.map { it.id }.toSet().size)
        assertEquals("duplicate letters", units.size, units.map { it.text }.toSet().size)
    }

    @Test
    fun everyLetterHasItsOwnExerciseSet() {
        val allExerciseIds = mutableListOf<String>()
        for (unit in CurriculumRepository.ALL_UNITS) {
            val exercises = CurriculumRepository.getExercisesForUnit(unit.id)
            assertTrue("no exercises for ${unit.text} (${unit.id})", exercises.isNotEmpty())
            exercises.forEach { ex ->
                assertEquals("exercise ${ex.id} targets the wrong letter", unit.id, ex.targetUnit.id)
                // multiple-choice exercises must contain exactly one correct option, and it must be the target
                if (ex.options.isNotEmpty()) {
                    val correct = ex.options.filter { it.isCorrect }
                    assertEquals("correct options in ${ex.id}", 1, correct.size)
                    assertEquals(unit.text, correct.first().text)
                }
                allExerciseIds += ex.id
            }
        }
        assertEquals("duplicate exercise ids", allExerciseIds.size, allExerciseIds.toSet().size)
    }

    @Test
    fun uyirmeiFormsAreSingleGraphemesOfTheirConsonant() {
        for (family in CurriculumRepository.CONSONANT_FAMILIES) {
            for (unit in family.uyirmei) {
                val graphemes = TamilTextSegmenter.segmentGraphemes(unit.text)
                assertEquals("${unit.text} should be one letter", 1, graphemes.size)
                assertEquals(TamilLetterType.UYIRMEI, graphemes[0].type)
                assertEquals(family.mei.text, graphemes[0].baseConsonant)
            }
        }
    }

    @Test
    fun kaFamilyHasAllThirteenFormsInVowelOrder() {
        val ka = CurriculumRepository.getFamily(1)!!
        assertEquals("க்", ka.mei.text)
        assertEquals(
            listOf("க", "கா", "கி", "கீ", "கு", "கூ", "கெ", "கே", "கை", "கொ", "கோ", "கௌ"),
            ka.uyirmei.map { it.text }
        )
        assertEquals(
            listOf("ka", "kaa", "ki", "kii", "ku", "kuu", "ke", "kee", "kai", "ko", "koo", "kau"),
            ka.uyirmei.map { it.transliteration }
        )
    }

    @Test
    fun singleLetterWordsAreAttachedToTheirLetters() {
        // கை = Hand is the 9th form (index 8) of the க family
        assertEquals("Hand", CurriculumRepository.getUnit("unit_uyirmei_1_8")?.singleLetterWord?.meaningEnglish)
        assertEquals("Cow / Bull", CurriculumRepository.UNIT_AA.singleLetterWord?.meaningEnglish)
    }
}
