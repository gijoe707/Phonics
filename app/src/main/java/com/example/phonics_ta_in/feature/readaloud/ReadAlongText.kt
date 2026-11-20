package com.example.phonics_ta_in.feature.readaloud

import com.example.phonics_ta_in.core.speech.WordMatching

/**
 * Turns pasted text into reading chunks: one chunk is shown at a time, so long sentences are cut into pieces a child
 * can take in at a glance.
 *
 * Pure Kotlin: no Android classes.
 */
object ReadAlongText {

    /** A chunk never has more words than this. */
    const val MAX_WORDS_PER_SENTENCE = 12

    /** From this many words on, a word that ends with a comma / semicolon / colon may end the chunk. */
    private const val SOFT_BREAK_AFTER = 7

    /** A last chunk shorter than this is joined to the one before it. */
    private const val MIN_TAIL_WORDS = 3

    // whitespace after . ! ? । … (optionally followed by a closing quote or bracket)
    private val SENTENCE_END = Regex("(?<=[.!?।…][\"'”’)]{0,2})\\s+")
    private val SOFT_BREAK_END = Regex("[,;:،]$")
    private val WHITESPACE = Regex("\\s+")

    fun toSentences(text: String): List<ReadAloudSentence> {
        val out = ArrayList<ReadAloudSentence>()
        for (line in text.lines()) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue
            for (piece in trimmed.split(SENTENCE_END)) {
                val words = piece.trim().split(WHITESPACE).filter { it.isNotEmpty() }
                // Nothing that can be read aloud (just "..." or "-"): not worth a card.
                if (words.none { WordMatching.key(it).isNotEmpty() }) continue
                for (chunk in chunk(words)) {
                    out += ReadAloudSentence(
                        id = "custom_${out.size + 1}",
                        text = chunk.joinToString(" "),
                        words = chunk,
                        englishTranslation = ""
                    )
                }
            }
        }
        return out
    }

    private fun chunk(words: List<String>): List<List<String>> {
        if (words.size <= MAX_WORDS_PER_SENTENCE) return listOf(words)
        val chunks = ArrayList<List<String>>()
        var current = ArrayList<String>()
        for (word in words) {
            current += word
            val softBreak = current.size >= SOFT_BREAK_AFTER && SOFT_BREAK_END.containsMatchIn(word)
            if (current.size >= MAX_WORDS_PER_SENTENCE || softBreak) {
                chunks += current
                current = ArrayList()
            }
        }
        if (current.isNotEmpty()) {
            if (current.size < MIN_TAIL_WORDS && chunks.isNotEmpty()) {
                chunks[chunks.lastIndex] = chunks.last() + current
            } else {
                chunks += current
            }
        }
        return chunks
    }
}
