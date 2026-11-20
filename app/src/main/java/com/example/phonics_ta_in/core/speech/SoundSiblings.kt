package com.example.phonics_ta_in.core.speech

import com.example.phonics_ta_in.core.curriculum.PhonicsUnit

/**
 * Chooses which letters the child's sound is compared against ([AcousticSoundMatcher]).
 *
 * A letter's short/long twin (அ/ஆ, இ/ஈ, க/கா, கி/கீ ...) is left out: the two sound the same apart from how long
 * the vowel is held, so a spectral comparison cannot tell them apart and would randomly reject the right sound
 * whenever the twin happened to score slightly better.
 *
 * Twins are found by transliteration, which the curriculum builds as consonant + vowel (a, aa, ka, kaa, ...):
 * a final doubled vowel (aa ii uu ee oo) collapses to the single one and the other way round. Diphthongs (ai, au) have
 * no twin and are never mistaken for one because the doubled/collapsed spelling must exist in the pool.
 */
object SoundSiblings {

    private val LONG_ENDINGS = listOf("aa", "ii", "uu", "ee", "oo")

    /** Spellings of the length twin of [translit] (the doubled and the collapsed form); the pool decides which exist. */
    internal fun twinSpellings(translit: String): Set<String> {
        val out = mutableSetOf<String>()
        for (long in LONG_ENDINGS) {
            if (translit.endsWith(long)) out += translit.dropLast(1)                 // kaa -> ka
        }
        if (translit.isNotEmpty() && translit.last() in "aiueo") out += translit + translit.last() // ka -> kaa
        return out
    }

    /** [pool] without [unit] itself and without its short/long twin. */
    fun comparisonPeers(unit: PhonicsUnit, pool: List<PhonicsUnit>): List<PhonicsUnit> {
        val twins = twinSpellings(unit.transliteration)
        return pool.filter { it.id != unit.id && it.transliteration !in twins }
    }
}
