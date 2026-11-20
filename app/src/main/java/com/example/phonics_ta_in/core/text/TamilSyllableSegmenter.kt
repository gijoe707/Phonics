package com.example.phonics_ta_in.core.text

/**
 * Handles phonetic Tamil syllable segmentation (Asai / அசை) for read-aloud
 * and letter-by-letter correction workflows.
 */
object TamilSyllableSegmenter {

    /**
     * Splits Tamil text into phonetic syllables (Asai).
     */
    fun segmentSyllables(text: String): List<String> {
        val graphemes = TamilTextSegmenter.segmentGraphemes(text)
        if (graphemes.isEmpty()) return emptyList()

        val syllables = mutableListOf<StringBuilder>()

        for (grapheme in graphemes) {
            when (grapheme.type) {
                TamilLetterType.MEI -> {
                    // Mei (consonant with pulli) attaches to the current preceding syllable if available
                    if (syllables.isNotEmpty()) {
                        syllables.last().append(grapheme.text)
                    } else {
                        // Edge case: Leading Mei starts its own syllable
                        syllables.add(StringBuilder(grapheme.text))
                    }
                }
                TamilLetterType.AYTHAM -> {
                    // Aytham attaches to the preceding nucleus syllable
                    if (syllables.isNotEmpty()) {
                        syllables.last().append(grapheme.text)
                    } else {
                        syllables.add(StringBuilder(grapheme.text))
                    }
                }
                TamilLetterType.UYIR,
                TamilLetterType.UYIRMEI,
                TamilLetterType.GRANTHA -> {
                    // Nucleus: starts a new syllable
                    syllables.add(StringBuilder(grapheme.text))
                }
                TamilLetterType.OTHER -> {
                    // Non-Tamil or punctuation
                    if (grapheme.text.isNotBlank()) {
                        syllables.add(StringBuilder(grapheme.text))
                    }
                }
            }
        }

        return syllables.map { it.toString() }
    }

    /**
     * Produces a complete [TamilWordSegmentation] breakdown for a word or phrase.
     */
    fun processWord(text: String): TamilWordSegmentation {
        val graphemes = TamilTextSegmenter.segmentGraphemes(text)
        val syllables = segmentSyllables(text)
        val transliteration = TamilTransliteration.transliterateWord(graphemes)

        return TamilWordSegmentation(
            originalText = text,
            graphemes = graphemes,
            syllables = syllables,
            transliteration = transliteration
        )
    }
}
