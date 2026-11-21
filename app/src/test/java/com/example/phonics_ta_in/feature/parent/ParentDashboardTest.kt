package com.example.phonics_ta_in.feature.parent

import com.example.phonics_ta_in.core.curriculum.CurriculumRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ParentDashboardTest {

    @Test
    fun masteryGridHasOneEntryPerLetter() {
        val grid = buildMasteryGrid(emptySet())
        assertEquals(CurriculumRepository.ALL_UNITS.size, grid.size)
    }

    @Test
    fun nothingIsMasteredForAProfileWithNoFinishedLessons() {
        val grid = buildMasteryGrid(emptySet())
        assertTrue(grid.all { it.status == MasteryStatus.NEEDS_PRACTICE })
    }

    @Test
    fun onlyFinishedLessonsAreMastered() {
        val finished = CurriculumRepository.ALL_UNITS.take(3).map { it.id }.toSet()
        val grid = buildMasteryGrid(finished)

        assertEquals(finished, grid.filter { it.status == MasteryStatus.MASTERED }.map { it.unitId }.toSet())
        assertEquals(
            CurriculumRepository.ALL_UNITS.size - 3,
            grid.count { it.status == MasteryStatus.NEEDS_PRACTICE }
        )
    }
}
