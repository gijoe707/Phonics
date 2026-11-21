package com.example.phonics_ta_in.feature.parent

import com.example.phonics_ta_in.core.curriculum.CurriculumRepository

enum class MasteryStatus {
    MASTERED,      // Green (>= 80%)
    IN_PROGRESS,   // Yellow (50-79%)
    NEEDS_PRACTICE // Red (< 50%)
}

data class LetterMasteryItem(
    val unitId: String,
    val letter: String,
    val transliteration: String,
    val category: String, // "Uyir", "Mei", "Uyirmei"
    val accuracyPercentage: Int,
    val attemptsCount: Int,
    val status: MasteryStatus
)

data class DailyUsageStat(
    val dayName: String,
    val minutesSpent: Int,
    val isToday: Boolean = false
)

/**
 * One entry per letter (all 247), built from the lessons the ACTIVE profile has finished.
 * The app does not track per-answer accuracy yet, so a letter is either mastered or needs practice.
 */
fun buildMasteryGrid(completedUnitIds: Set<String>): List<LetterMasteryItem> =
    CurriculumRepository.ALL_UNITS.map { unit ->
        val isMastered = completedUnitIds.contains(unit.id)
        LetterMasteryItem(
            unitId = unit.id,
            letter = unit.text,
            transliteration = unit.transliteration,
            category = unit.kind.name,
            accuracyPercentage = if (isMastered) 95 else 40,
            attemptsCount = if (isMastered) 5 else 2,
            status = if (isMastered) MasteryStatus.MASTERED else MasteryStatus.NEEDS_PRACTICE
        )
    }
