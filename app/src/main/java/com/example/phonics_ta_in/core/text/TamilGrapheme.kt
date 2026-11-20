package com.example.phonics_ta_in.core.text

/**
 * Classification of Tamil character / letter types.
 */
enum class TamilLetterType {
    UYIR,       // Pure Vowel (அ, ஆ, இ, ஈ, உ, ஊ, எ, ஏ, ஐ, ஒ, ஓ, ஔ)
    MEI,        // Pure Consonant with Pulli (க், ங், ச், ...)
    UYIRMEI,    // Combined Consonant + Vowel (க, கா, கி, கீ, ...)
    AYTHAM,     // Special letter (ஃ)
    GRANTHA,    // Grantha letters (ஜ, ஷ, ஸ், ஹ, க்ஷ)
    OTHER       // Punctuation, numbers, spaces, non-Tamil characters
}

/**
 * Represents a single grapheme cluster (user-perceived character) in Tamil.
 */
data class TamilGrapheme(
    val text: String,
    val type: TamilLetterType,
    val baseConsonant: String? = null, // e.g. "க்" or "க" for "கா"
    val vowelSign: String? = null,      // e.g. "ஆ" or "ா" for "கா"
    val transliteration: String = ""
)

/**
 * Represents the full segmentation breakdown of a Tamil word or phrase.
 */
data class TamilWordSegmentation(
    val originalText: String,
    val graphemes: List<TamilGrapheme>,
    val syllables: List<String>,
    val transliteration: String
)
