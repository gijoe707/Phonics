package com.example.phonics_ta_in.core.speech

import com.example.phonics_ta_in.core.text.TamilTextSegmenter
import java.text.Normalizer

/**
 * Word-level comparison of what the recognizer heard with the word the child was supposed to read.
 *
 * Pure Kotlin (no Android classes) so it can be unit-tested on the JVM. The leniency mirrors
 * [SpeechRecognizerEngine.isMatch]: grapheme-level edit distance, 70 % similarity.
 */
object WordMatching {

    const val DEFAULT_TOLERANCE = 0.70f

    private val NOT_LETTER = Regex("[^\\p{L}\\p{M}]+")
    private val WHITESPACE = Regex("\\s+")

    /**
     * The letters (and combining marks) of [word] only: NFC, lower case, no punctuation, digits or spaces.
     * Empty for something that cannot be read aloud as a word ("-", "10", "...").
     */
    fun key(word: String): String =
        NOT_LETTER.replace(Normalizer.normalize(word, Normalizer.Form.NFC), "").lowercase()

    /** Splits recognizer output into words; punctuation separates words. */
    fun tokenize(text: String): List<String> {
        val spaced = NOT_LETTER.replace(Normalizer.normalize(text, Normalizer.Form.NFC), " ").lowercase().trim()
        if (spaced.isEmpty()) return emptyList()
        return spaced.split(WHITESPACE).filter { it.isNotEmpty() }
    }

    /** 1.0 = same graphemes, 0.0 = nothing in common (edit distance over Tamil letters, not UTF-16 chars). */
    fun similarity(a: String, b: String): Float {
        val ga = graphemes(a)
        val gb = graphemes(b)
        if (ga.isEmpty() || gb.isEmpty()) return 0f
        if (ga == gb) return 1f
        val distance = editDistance(ga, gb)
        return 1f - distance.toFloat() / maxOf(ga.size, gb.size).toFloat()
    }

    /** Close enough to count as the same word (used once the recognizer has finished the word). */
    fun isMatch(spoken: String, target: String, tolerance: Float = DEFAULT_TOLERANCE): Boolean {
        val s = key(spoken)
        val t = key(target)
        if (s.isEmpty() || t.isEmpty()) return false
        return similarity(s, t) >= tolerance
    }

    /** Identical letters (used while the recognizer may still be in the middle of the word). */
    fun isExact(spoken: String, target: String): Boolean {
        val s = key(spoken)
        return s.isNotEmpty() && s == key(target)
    }

    private fun graphemes(s: String): List<String> = TamilTextSegmenter.segmentGraphemes(s).map { it.text }

    private fun editDistance(a: List<String>, b: List<String>): Int {
        val dp = Array(a.size + 1) { IntArray(b.size + 1) }
        for (i in 0..a.size) dp[i][0] = i
        for (j in 0..b.size) dp[0][j] = j
        for (i in 1..a.size) {
            for (j in 1..b.size) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
            }
        }
        return dp[a.size][b.size]
    }
}
