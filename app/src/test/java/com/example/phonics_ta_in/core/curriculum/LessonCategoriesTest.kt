package com.example.phonics_ta_in.core.curriculum

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LessonCategoriesTest {

    private val vowelPage = CurriculumRepository.VOWEL_UNITS + CurriculumRepository.UNIT_AYTHAM
    private val firstFamily = CurriculumRepository.CONSONANT_FAMILIES.first()

    @Test
    fun vowelLessonsShareTheVowelsPage() {
        assertEquals(vowelPage.map { it.id }, LessonCategories.unitsFor(CurriculumRepository.UNIT_A.id).map { it.id })
        assertEquals(vowelPage.map { it.id }, LessonCategories.unitsFor(CurriculumRepository.UNIT_AYTHAM.id).map { it.id })
    }

    @Test
    fun consonantLessonsShareTheirFamily() {
        val expected = firstFamily.units.map { it.id }
        assertEquals(13, expected.size)
        // the mei and every uyirmei of that consonant are one category
        firstFamily.units.forEach { unit ->
            assertEquals(expected, LessonCategories.unitsFor(unit.id).map { it.id })
        }
        // ...and it is separate from the next consonant
        val secondFamilyUnit = CurriculumRepository.CONSONANT_FAMILIES[1].mei.id
        assertNotEquals(expected, LessonCategories.unitsFor(secondFamilyUnit).map { it.id })
    }

    @Test
    fun unknownLessonHasNoCategory() {
        assertTrue(LessonCategories.unitsFor("nope").isEmpty())
        assertNull(LessonCategories.nextUnitId("nope"))
        assertNull(LessonCategories.randomUnitId("nope", emptySet()))
    }

    @Test
    fun nextFollowsTeachingOrderAndStopsAtTheEnd() {
        val ids = vowelPage.map { it.id }
        assertEquals(ids[1], LessonCategories.nextUnitId(ids[0]))
        assertNull(LessonCategories.nextUnitId(ids.last()))

        val familyIds = firstFamily.units.map { it.id }
        assertEquals(familyIds[1], LessonCategories.nextUnitId(familyIds[0]))
        assertNull(LessonCategories.nextUnitId(familyIds.last()))
    }

    @Test
    fun walkingNextVisitsEveryLessonOfTheCategoryOnce() {
        val visited = mutableListOf(firstFamily.units.first().id)
        while (true) {
            visited += LessonCategories.nextUnitId(visited.last()) ?: break
        }
        assertEquals(firstFamily.units.map { it.id }, visited)
    }

    @Test
    fun randomNeverReturnsTheCurrentLessonAndStaysInTheCategory() {
        val current = firstFamily.units[3].id
        val allowed = firstFamily.units.map { it.id }.toSet() - current
        repeat(50) { seed ->
            val picked = LessonCategories.randomUnitId(current, emptySet(), Random(seed))
            assertTrue("picked $picked", picked in allowed)
        }
    }

    @Test
    fun randomPrefersLessonsNotFinishedYet() {
        val ids = firstFamily.units.map { it.id }
        val current = ids[0]
        val onlyUnfinished = ids[7]
        val finished = ids.toSet() - onlyUnfinished // everything else (including current) is done
        repeat(20) { seed ->
            assertEquals(onlyUnfinished, LessonCategories.randomUnitId(current, finished, Random(seed)))
        }
    }

    @Test
    fun randomStillWorksWhenEverythingIsFinished() {
        val ids = firstFamily.units.map { it.id }
        val picked = LessonCategories.randomUnitId(ids[0], ids.toSet(), Random(1))
        assertTrue(picked != null && picked != ids[0] && picked in ids)
    }
}
