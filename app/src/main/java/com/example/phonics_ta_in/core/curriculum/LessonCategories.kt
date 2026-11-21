package com.example.phonics_ta_in.core.curriculum

import kotlin.random.Random

/**
 * Which lessons belong together, matching the pages the child opens them from:
 *  - the Vowels page: the 12 vowels plus ஃ
 *  - a consonant's page: the mei plus its 12 uyirmei letters (13 lessons)
 * "Next" and "Random" after a lesson stay inside that group.
 */
object LessonCategories {

    /** The lessons in the same category as [unitId], in teaching order (includes [unitId] itself). */
    fun unitsFor(unitId: String): List<PhonicsUnit> {
        val vowelsPage = CurriculumRepository.VOWEL_UNITS + CurriculumRepository.UNIT_AYTHAM
        if (vowelsPage.any { it.id == unitId }) return vowelsPage

        val family = CurriculumRepository.CONSONANT_FAMILIES
            .firstOrNull { f -> f.units.any { it.id == unitId } }
        return family?.units ?: emptyList()
    }

    /** The lesson after [unitId] in its category, or null when it is the last one. */
    fun nextUnitId(unitId: String): String? {
        val units = unitsFor(unitId)
        val position = units.indexOfFirst { it.id == unitId }
        return if (position in 0 until units.lastIndex) units[position + 1].id else null
    }

    /**
     * A random OTHER lesson from the same category. Lessons the child has not finished properly yet
     * ([genuineCompletedUnitIds]) are picked first; if everything is finished, any other lesson is picked.
     * Returns null when the category has no other lesson.
     */
    fun randomUnitId(
        unitId: String,
        genuineCompletedUnitIds: Set<String>,
        random: Random = Random.Default
    ): String? {
        val others = unitsFor(unitId).filter { it.id != unitId }
        if (others.isEmpty()) return null
        val unfinished = others.filter { it.id !in genuineCompletedUnitIds }
        return (unfinished.ifEmpty { others }).random(random).id
    }
}
