package com.example.phonics_ta_in.core.text

import java.text.BreakIterator
import java.text.Normalizer
import java.util.Locale

/**
 * Utility for grapheme-aware Tamil text segmentation, character classification,
 * and component extraction.
 */
object TamilTextSegmenter {

    // Vowels (Uyir)
    val UYIR_SET = setOf(
        "அ", "ஆ", "இ", "ஈ", "உ", "ஊ", "எ", "ஏ", "ஐ", "ஒ", "ஓ", "ஔ"
    )

    // Special Aytham letter
    const val AYTHAM = "ஃ"

    // Consonant stems (without Pulli)
    val NATIVE_CONSONANT_STEMS = setOf(
        "க", "ங", "ச", "ஞ", "ட", "ண", "த", "ந", "ப", "ம", "ய", "ர", "ல", "வ", "ழ", "ள", "ற", "ன"
    )

    val GRANTHA_CONSONANT_STEMS = setOf(
        "ஜ", "ஷ", "ஸ", "ஹ", "க்ஷ"
    )

    val ALL_CONSONANT_STEMS = NATIVE_CONSONANT_STEMS + GRANTHA_CONSONANT_STEMS

    // Pulli (Virama) symbol
    const val PULLI = "்"

    // Map of combining vowel signs to their full Uyir equivalent
    val VOWEL_SIGN_MAP = mapOf(
        "ா" to "ஆ",
        "ி" to "இ",
        "ீ" to "ஈ",
        "ு" to "உ",
        "ூ" to "ஊ",
        "ெ" to "எ",
        "ே" to "ஏ",
        "ை" to "ஐ",
        "ொ" to "ஒ",
        "ோ" to "ஓ",
        "ௌ" to "ஔ"
    )

    /**
     * Splits Tamil text into grapheme clusters (individual visual Tamil letters).
     */
    fun segmentGraphemes(text: String): List<TamilGrapheme> {
        if (text.isBlank()) return emptyList()

        val normalized = Normalizer.normalize(text, Normalizer.Form.NFC)
        val iterator = BreakIterator.getCharacterInstance(Locale("ta", "IN"))
        iterator.setText(normalized)

        val graphemes = mutableListOf<TamilGrapheme>()
        var start = iterator.first()
        var end = iterator.next()

        while (end != BreakIterator.DONE) {
            val clusterText = normalized.substring(start, end)
            if (clusterText.isNotBlank()) {
                graphemes.add(classifyGrapheme(clusterText))
            }
            start = end
            end = iterator.next()
        }

        return graphemes
    }

    /**
     * Classifies a single Tamil grapheme cluster string into a [TamilGrapheme].
     */
    fun classifyGrapheme(cluster: String): TamilGrapheme {
        if (cluster == AYTHAM) {
            return TamilGrapheme(
                text = cluster,
                type = TamilLetterType.AYTHAM,
                transliteration = TamilTransliteration.transliterateGrapheme(cluster)
            )
        }

        if (UYIR_SET.contains(cluster)) {
            return TamilGrapheme(
                text = cluster,
                type = TamilLetterType.UYIR,
                vowelSign = cluster,
                transliteration = TamilTransliteration.transliterateGrapheme(cluster)
            )
        }

        // Check if ends with Pulli (Pure Consonant / Mei)
        if (cluster.endsWith(PULLI)) {
            val stem = cluster.removeSuffix(PULLI)
            val isGrantha = GRANTHA_CONSONANT_STEMS.contains(stem)
            return TamilGrapheme(
                text = cluster,
                type = if (isGrantha) TamilLetterType.GRANTHA else TamilLetterType.MEI,
                baseConsonant = cluster,
                vowelSign = null,
                transliteration = TamilTransliteration.transliterateGrapheme(cluster)
            )
        }

        // Check if Uyirmei or Grantha combined form
        val firstChar = cluster.firstOrNull()?.toString() ?: ""
        if (ALL_CONSONANT_STEMS.contains(firstChar)) {
            val isGrantha = GRANTHA_CONSONANT_STEMS.contains(firstChar)
            val baseMei = firstChar + PULLI

            // Extract vowel sign if present, else implicit 'அ'
            val signPart = cluster.substring(firstChar.length)
            val vowelFull = VOWEL_SIGN_MAP[signPart] ?: "அ"

            return TamilGrapheme(
                text = cluster,
                type = if (isGrantha) TamilLetterType.GRANTHA else TamilLetterType.UYIRMEI,
                baseConsonant = baseMei,
                vowelSign = vowelFull,
                transliteration = TamilTransliteration.transliterateGrapheme(cluster)
            )
        }

        return TamilGrapheme(
            text = cluster,
            type = TamilLetterType.OTHER,
            transliteration = cluster
        )
    }
}
