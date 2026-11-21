package com.example.phonics_ta_in.core.progress

import com.example.phonics_ta_in.core.curriculum.CurriculumRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class ProgressLogicTest {

    private val day = LocalDate.of(2026, 11, 16).toEpochDay() // a Monday

    @Test
    fun freshInstallHasNoProgress() {
        val state = ProgressState()
        assertEquals(0, state.totalStars)
        assertEquals(0, ProgressLogic.displayStreak(state, day))
        assertEquals(0, ProgressLogic.minutesToday(state, day))
        assertTrue(state.completedUnitIds.isEmpty())
        assertTrue(state.genuineCompletedUnitIds.isEmpty())
        assertEquals(15, state.dailyGoalMinutes)
        assertFalse(ProgressLogic.vowelsComplete(state.genuineCompletedUnitIds))
        assertFalse(ProgressLogic.consonantsComplete(state.genuineCompletedUnitIds))
        assertTrue(ProgressLogic.completedFamilyIndexes(state.genuineCompletedUnitIds).isEmpty())
    }

    // --- Stars ---

    @Test
    fun starsDependOnWrongAnswers() {
        assertEquals(3, ProgressLogic.starsFor(0, false))
        assertEquals(2, ProgressLogic.starsFor(1, false))
        assertEquals(2, ProgressLogic.starsFor(2, false))
        assertEquals(1, ProgressLogic.starsFor(3, false))
        assertEquals(1, ProgressLogic.starsFor(10, false))
    }

    @Test
    fun selfCheckCapsStarsAtTwo() {
        assertEquals(2, ProgressLogic.starsFor(0, true))
        assertEquals(2, ProgressLogic.starsFor(2, true))
        assertEquals(1, ProgressLogic.starsFor(5, true))
    }

    @Test
    fun lessonAddsItsStarsToTheTotal() {
        var state = ProgressLogic.recordLessonCompleted(ProgressState(), "unit_a", 3, false, day)
        assertEquals(3, state.totalStars)
        state = ProgressLogic.recordLessonCompleted(state, "unit_aa", 2, false, day)
        assertEquals(5, state.totalStars)
    }

    @Test
    fun replayingALessonOnlyKeepsTheBestResult() {
        var state = ProgressLogic.recordLessonCompleted(ProgressState(), "unit_a", 2, false, day)
        state = ProgressLogic.recordLessonCompleted(state, "unit_a", 3, false, day)
        assertEquals(3, state.totalStars)          // improved: 2 -> 3
        state = ProgressLogic.recordLessonCompleted(state, "unit_a", 1, false, day)
        assertEquals(3, state.totalStars)          // a worse replay never lowers it
        state = ProgressLogic.recordLessonCompleted(state, "unit_a", 3, false, day)
        assertEquals(3, state.totalStars)          // and repeating 3 stars never adds more
    }

    @Test
    fun starsAreKeptBetweenOneAndThree() {
        val state = ProgressLogic.recordLessonCompleted(ProgressState(), "unit_a", 99, false, day)
        assertEquals(3, state.totalStars)
        val low = ProgressLogic.recordLessonCompleted(ProgressState(), "unit_i", 0, false, day)
        assertEquals(1, low.totalStars)
    }

    @Test
    fun starsEncodingRoundTrips() {
        val map = mapOf("unit_a" to 3, "unit_uyirmei_1_4" to 1)
        assertEquals(map, ProgressLogic.decodeStars(ProgressLogic.encodeStars(map)))
        assertTrue(ProgressLogic.decodeStars("").isEmpty())
        assertTrue(ProgressLogic.decodeStars("junk,:2,unit_a:").isEmpty())
    }

    // --- Streak ---

    @Test
    fun firstLessonStartsStreakAtOne() {
        val state = ProgressLogic.recordLessonCompleted(ProgressState(), "unit_a", 3, false, day)
        assertEquals(1, ProgressLogic.displayStreak(state, day))
        assertTrue("unit_a" in state.completedUnitIds)
        assertTrue("unit_a" in state.genuineCompletedUnitIds)
    }

    @Test
    fun secondLessonSameDayDoesNotIncreaseStreak() {
        var state = ProgressLogic.recordLessonCompleted(ProgressState(), "unit_a", 3, false, day)
        state = ProgressLogic.recordLessonCompleted(state, "unit_aa", 3, false, day)
        assertEquals(1, ProgressLogic.displayStreak(state, day))
    }

    @Test
    fun consecutiveDaysBuildStreak() {
        var state = ProgressLogic.recordLessonCompleted(ProgressState(), "unit_a", 3, false, day)
        state = ProgressLogic.recordLessonCompleted(state, "unit_aa", 3, false, day + 1)
        state = ProgressLogic.recordLessonCompleted(state, "unit_i", 3, false, day + 2)
        assertEquals(3, ProgressLogic.displayStreak(state, day + 2))
    }

    @Test
    fun streakStillShownTheDayAfterButGoneAfterAMissedDay() {
        val state = ProgressLogic.recordLessonCompleted(ProgressState(), "unit_a", 3, false, day)
        assertEquals(1, ProgressLogic.displayStreak(state, day + 1))
        assertEquals(0, ProgressLogic.displayStreak(state, day + 2))
    }

    @Test
    fun missedDayRestartsStreakAtOne() {
        var state = ProgressLogic.recordLessonCompleted(ProgressState(), "unit_a", 3, false, day)
        state = ProgressLogic.recordLessonCompleted(state, "unit_aa", 3, false, day + 1)
        state = ProgressLogic.recordLessonCompleted(state, "unit_i", 3, false, day + 4)
        assertEquals(1, ProgressLogic.displayStreak(state, day + 4))
    }

    @Test
    fun selfCheckCompletionIsNotGenuine() {
        val state = ProgressLogic.recordLessonCompleted(ProgressState(), "unit_a", 2, true, day)
        assertTrue("unit_a" in state.completedUnitIds)
        assertFalse("unit_a" in state.genuineCompletedUnitIds)
    }

    // --- Study time ---

    @Test
    fun studyTimeAddsUpPerDayAndIsCapped() {
        var state = ProgressLogic.addStudySeconds(ProgressState(), 90, day)
        state = ProgressLogic.addStudySeconds(state, 90, day)
        assertEquals(3, ProgressLogic.minutesToday(state, day))

        val capped = ProgressLogic.addStudySeconds(ProgressState(), 5 * 60 * 60, day)
        assertEquals(ProgressLogic.MAX_LESSON_VISIT_SECONDS / 60, ProgressLogic.minutesToday(capped, day))
    }

    @Test
    fun oldStudyDaysArePruned() {
        var state = ProgressLogic.addStudySeconds(ProgressState(), 120, day)
        state = ProgressLogic.addStudySeconds(state, 120, day + 30)
        assertFalse(state.secondsByEpochDay.containsKey(day))
        assertTrue(state.secondsByEpochDay.containsKey(day + 30))
    }

    @Test
    fun weeklyMinutesRunMondayToSunday() {
        var state = ProgressLogic.addStudySeconds(ProgressState(), 120, day)      // Monday
        state = ProgressLogic.addStudySeconds(state, 300, day + 2)               // Wednesday
        assertEquals(listOf(2, 0, 5, 0, 0, 0, 0), ProgressLogic.weeklyMinutes(state, day + 3)) // asked on Thursday
    }

    @Test
    fun secondsEncodingRoundTrips() {
        val map = mapOf(20000L to 95, 20001L to 600)
        assertEquals(map, ProgressLogic.decodeSeconds(ProgressLogic.encodeSeconds(map)))
        assertTrue(ProgressLogic.decodeSeconds("").isEmpty())
        assertTrue(ProgressLogic.decodeSeconds("garbage,1:").isEmpty())
    }

    // --- Section completion (ticks) ---

    @Test
    fun vowelsTickNeedsEveryVowelAndAytham() {
        val allVowelIds = (CurriculumRepository.VOWEL_UNITS + CurriculumRepository.UNIT_AYTHAM).map { it.id }
        assertTrue(ProgressLogic.vowelsComplete(allVowelIds.toSet()))
        assertFalse(ProgressLogic.vowelsComplete(allVowelIds.drop(1).toSet()))
    }

    @Test
    fun consonantTickNeedsEveryLetterOfThatFamily() {
        val first = CurriculumRepository.CONSONANT_FAMILIES.first()
        val ids = first.units.map { it.id }

        assertEquals(setOf(first.index), ProgressLogic.completedFamilyIndexes(ids.toSet()))
        assertTrue(ProgressLogic.completedFamilyIndexes(ids.drop(1).toSet()).isEmpty())
        assertFalse(ProgressLogic.consonantsComplete(ids.toSet()))

        val everyConsonantId = CurriculumRepository.CONSONANT_FAMILIES.flatMap { f -> f.units.map { it.id } }.toSet()
        assertTrue(ProgressLogic.consonantsComplete(everyConsonantId))
    }
}
