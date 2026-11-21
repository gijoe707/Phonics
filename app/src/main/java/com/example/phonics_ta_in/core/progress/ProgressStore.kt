package com.example.phonics_ta_in.core.progress

import android.content.Context
import android.content.SharedPreferences
import com.example.phonics_ta_in.core.curriculum.CurriculumRepository
import java.time.LocalDate

/**
 * Everything the app knows about ONE child's real progress. A new profile starts at zero:
 * no stars, no streak, no completed letters.
 */
data class ProgressState(
    /** Best stars (1-3) earned in each lesson. Replaying a lesson can only improve it, never add more. */
    val starsByUnit: Map<String, Int> = emptyMap(),
    /** Consecutive days with at least one finished lesson, as of [lastActiveEpochDay]. */
    val streakDays: Int = 0,
    val lastActiveEpochDay: Long = NEVER,
    /** Every lesson the child finished (including ones finished with the "I said it" self-check). */
    val completedUnitIds: Set<String> = emptySet(),
    /** Lessons finished without the self-check button. These get the green tick. */
    val genuineCompletedUnitIds: Set<String> = emptySet(),
    /** Seconds spent inside lessons, per epoch day. Only the most recent days are kept. */
    val secondsByEpochDay: Map<Long, Int> = emptyMap(),
    val dailyGoalMinutes: Int = 15
) {
    /** Total stars shown in the header: the sum of the best result of every lesson. */
    val totalStars: Int get() = starsByUnit.values.sum()

    companion object {
        const val NEVER = -1L
    }
}

/** Pure functions over [ProgressState] so the rules can be unit tested without Android. */
object ProgressLogic {
    private const val KEEP_DAYS = 14

    /** One lesson visit never counts for more than this, so a lesson left open in the background doesn't inflate the time. */
    const val MAX_LESSON_VISIT_SECONDS = 10 * 60

    fun today(): Long = LocalDate.now().toEpochDay()

    /**
     * Stars for a finished lesson:
     *  3 = no wrong answers, 2 = one or two wrong answers, 1 = more than that.
     * Using the "I said it" self-check caps the result at 2 stars.
     */
    fun starsFor(wrongAnswers: Int, usedSelfCheck: Boolean): Int {
        val fromAnswers = when {
            wrongAnswers <= 0 -> 3
            wrongAnswers <= 2 -> 2
            else -> 1
        }
        return if (usedSelfCheck) minOf(fromAnswers, 2) else fromAnswers
    }

    /** The streak to show: it is only alive if the child was active today or yesterday. */
    fun displayStreak(state: ProgressState, today: Long): Int {
        if (state.lastActiveEpochDay == ProgressState.NEVER) return 0
        return if (today - state.lastActiveEpochDay <= 1) state.streakDays else 0
    }

    fun recordLessonCompleted(
        state: ProgressState,
        unitId: String,
        stars: Int,
        usedSelfCheck: Boolean,
        today: Long
    ): ProgressState {
        val newStreak = if (state.lastActiveEpochDay == today) {
            maxOf(state.streakDays, 1)
        } else {
            displayStreak(state, today) + 1
        }
        val bestStars = maxOf(state.starsByUnit[unitId] ?: 0, stars.coerceIn(1, 3))
        return state.copy(
            starsByUnit = state.starsByUnit + (unitId to bestStars),
            streakDays = newStreak,
            lastActiveEpochDay = today,
            completedUnitIds = state.completedUnitIds + unitId,
            genuineCompletedUnitIds =
                if (usedSelfCheck) state.genuineCompletedUnitIds else state.genuineCompletedUnitIds + unitId
        )
    }

    fun addStudySeconds(state: ProgressState, seconds: Int, today: Long): ProgressState {
        val capped = seconds.coerceAtMost(MAX_LESSON_VISIT_SECONDS)
        if (capped <= 0) return state
        val updated = (state.secondsByEpochDay + (today to ((state.secondsByEpochDay[today] ?: 0) + capped)))
            .filterKeys { it > today - KEEP_DAYS }
        return state.copy(secondsByEpochDay = updated)
    }

    fun minutesToday(state: ProgressState, today: Long): Int =
        (state.secondsByEpochDay[today] ?: 0) / 60

    /** Minutes for Monday..Sunday of the week that contains [today]. */
    fun weeklyMinutes(state: ProgressState, today: Long): List<Int> {
        val dayOfWeek = LocalDate.ofEpochDay(today).dayOfWeek.value // Monday = 1
        val monday = today - (dayOfWeek - 1)
        return (0..6).map { (state.secondsByEpochDay[monday + it] ?: 0) / 60 }
    }

    // --- Completion of whole sections (drives the ticks on the main screens) ---

    /** All 12 vowels plus the aytham letter are finished. */
    fun vowelsComplete(genuineCompletedUnitIds: Set<String>): Boolean =
        (CurriculumRepository.VOWEL_UNITS + CurriculumRepository.UNIT_AYTHAM)
            .all { it.id in genuineCompletedUnitIds }

    /** Consonant families (by index) whose mei and all 12 uyirmei letters are finished. */
    fun completedFamilyIndexes(genuineCompletedUnitIds: Set<String>): Set<Int> =
        CurriculumRepository.CONSONANT_FAMILIES
            .filter { family -> family.units.all { it.id in genuineCompletedUnitIds } }
            .map { it.index }
            .toSet()

    fun consonantsComplete(genuineCompletedUnitIds: Set<String>): Boolean =
        completedFamilyIndexes(genuineCompletedUnitIds).size == CurriculumRepository.CONSONANT_FAMILIES.size

    // --- Storage format ---

    fun encodeSeconds(map: Map<Long, Int>): String =
        map.entries.joinToString(",") { "${it.key}:${it.value}" }

    fun decodeSeconds(text: String): Map<Long, Int> =
        text.split(",")
            .mapNotNull { part ->
                val pieces = part.split(":")
                val day = pieces.getOrNull(0)?.toLongOrNull()
                val seconds = pieces.getOrNull(1)?.toIntOrNull()
                if (day != null && seconds != null) day to seconds else null
            }
            .toMap()

    fun encodeStars(map: Map<String, Int>): String =
        map.entries.joinToString(",") { "${it.key}:${it.value}" }

    fun decodeStars(text: String): Map<String, Int> =
        text.split(",")
            .mapNotNull { part ->
                val pieces = part.split(":")
                val unitId = pieces.getOrNull(0)?.takeIf { it.isNotBlank() }
                val stars = pieces.getOrNull(1)?.toIntOrNull()
                if (unitId != null && stars != null) unitId to stars.coerceIn(0, 3) else null
            }
            .toMap()
}

/**
 * Saves and loads one profile's [ProgressState] on the device so progress survives closing the app.
 * Every profile has its own file, so stars, streak and ticks never mix between children.
 */
class ProgressStore(context: Context, profileId: String) {
    private val prefs = prefsFor(context, profileId)

    fun load(): ProgressState = ProgressState(
        starsByUnit = ProgressLogic.decodeStars(prefs.getString(KEY_STARS, "") ?: ""),
        streakDays = prefs.getInt(KEY_STREAK, 0),
        lastActiveEpochDay = prefs.getLong(KEY_LAST_ACTIVE, ProgressState.NEVER),
        completedUnitIds = prefs.getStringSet(KEY_COMPLETED, null)?.toSet() ?: emptySet(),
        genuineCompletedUnitIds = prefs.getStringSet(KEY_GENUINE, null)?.toSet() ?: emptySet(),
        secondsByEpochDay = ProgressLogic.decodeSeconds(prefs.getString(KEY_SECONDS, "") ?: ""),
        dailyGoalMinutes = prefs.getInt(KEY_GOAL, 15)
    )

    fun save(state: ProgressState) {
        prefs.edit()
            .putString(KEY_STARS, ProgressLogic.encodeStars(state.starsByUnit))
            .putInt(KEY_STREAK, state.streakDays)
            .putLong(KEY_LAST_ACTIVE, state.lastActiveEpochDay)
            .putStringSet(KEY_COMPLETED, state.completedUnitIds.toMutableSet())
            .putStringSet(KEY_GENUINE, state.genuineCompletedUnitIds.toMutableSet())
            .putString(KEY_SECONDS, ProgressLogic.encodeSeconds(state.secondsByEpochDay))
            .putInt(KEY_GOAL, state.dailyGoalMinutes)
            .apply()
    }

    companion object {
        /** The single file the app used before profiles existed. */
        private const val LEGACY_PREFS_NAME = "phonics_progress"
        private const val LEGACY_KEY_MUSIC_ENABLED = "music_enabled"

        private const val KEY_STARS = "stars_by_unit"
        private const val KEY_STREAK = "streak_days"
        private const val KEY_LAST_ACTIVE = "last_active_epoch_day"
        private const val KEY_COMPLETED = "completed_unit_ids"
        private const val KEY_GENUINE = "genuine_completed_unit_ids"
        private const val KEY_SECONDS = "seconds_by_day"
        private const val KEY_GOAL = "daily_goal_minutes"

        private fun prefsFor(context: Context, profileId: String): SharedPreferences =
            context.applicationContext.getSharedPreferences(
                "${LEGACY_PREFS_NAME}_$profileId",
                Context.MODE_PRIVATE
            )

        /** Erases everything saved for a deleted profile. */
        fun delete(context: Context, profileId: String) {
            prefsFor(context, profileId).edit().clear().apply()
        }

        /** The parent's music switch as it was saved before it became a device-wide setting. */
        fun legacyMusicEnabled(context: Context): Boolean =
            context.applicationContext
                .getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(LEGACY_KEY_MUSIC_ENABLED, true)

        /**
         * Copies the progress saved before profiles existed into [profileId]. The old file is left
         * untouched. Called once, for the first profile ever created.
         */
        fun copyLegacyInto(context: Context, profileId: String) {
            val legacy = context.applicationContext
                .getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
            val editor = prefsFor(context, profileId).edit()
            for ((key, value) in legacy.all) {
                if (key == LEGACY_KEY_MUSIC_ENABLED) continue
                when (value) {
                    is String -> editor.putString(key, value)
                    is Int -> editor.putInt(key, value)
                    is Long -> editor.putLong(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                    is Set<*> -> editor.putStringSet(key, value.filterIsInstance<String>().toSet())
                }
            }
            editor.apply()
        }
    }
}
