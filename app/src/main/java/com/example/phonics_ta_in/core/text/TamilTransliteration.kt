package com.example.phonics_ta_in.core.text

/**
 * Phonetic English transliteration helper for Tamil graphemes and words.
 */
object TamilTransliteration {

    private val UYIR_MAP = mapOf(
        "அ" to "a",
        "ஆ" to "aa",
        "இ" to "i",
        "ஈ" to "ii",
        "உ" to "u",
        "ஊ" to "uu",
        "எ" to "e",
        "ஏ" to "ee",
        "ஐ" to "ai",
        "ஒ" to "o",
        "ஓ" to "oo",
        "ஔ" to "au"
    )

    private val MEI_MAP = mapOf(
        "க்" to "k",
        "ங்" to "ng",
        "ச்" to "ch",
        "ஞ்" to "nj",
        "ட்" to "t",
        "ண்" to "n",
        "த்" to "th",
        "ந்" to "nh",
        "ப்" to "p",
        "ம்" to "m",
        "ய்" to "y",
        "ர்" to "r",
        "ல்" to "l",
        "வ்" to "v",
        "ழ்" to "zh",
        "ள்" to "L",
        "ற்" to "R",
        "ன்" to "n",
        "ஜ்" to "j",
        "ஷ்" to "sh",
        "ஸ்" to "s",
        "ஹ்" to "h",
        "க்ஷ்" to "ksh"
    )

    private val VOWEL_SIGN_SUFFIX_MAP = mapOf(
        "அ" to "a",
        "ஆ" to "aa",
        "இ" to "i",
        "ஈ" to "ii",
        "உ" to "u",
        "ஊ" to "uu",
        "எ" to "e",
        "ஏ" to "ee",
        "ஐ" to "ai",
        "ஒ" to "o",
        "ஓ" to "oo",
        "ஔ" to "au",
        "ா" to "aa",
        "ி" to "i",
        "ீ" to "ii",
        "ு" to "u",
        "ூ" to "uu",
        "ெ" to "e",
        "ே" to "ee",
        "ை" to "ai",
        "ொ" to "o",
        "ோ" to "oo",
        "ௌ" to "au"
    )

    fun transliterateGrapheme(cluster: String): String {
        if (cluster == "ஃ") return "ak"

        UYIR_MAP[cluster]?.let { return it }
        MEI_MAP[cluster]?.let { return it }

        // Handle Uyirmei (consonant stem + vowel sign or implicit 'a')
        val firstChar = cluster.firstOrNull()?.toString() ?: ""
        val baseMei = firstChar + TamilTextSegmenter.PULLI
        val consLatin = MEI_MAP[baseMei]

        if (consLatin != null) {
            val signPart = cluster.substring(firstChar.length)
            val vowelLatin = if (signPart.isEmpty()) "a" else VOWEL_SIGN_SUFFIX_MAP[signPart] ?: "a"
            return consLatin + vowelLatin
        }

        return cluster
    }

    /**
     * Transliterates a list of graphemes or a full Tamil string into English phonetics.
     */
    fun transliterateWord(graphemes: List<TamilGrapheme>): String {
        return graphemes.joinToString("") { it.transliteration }
    }
}
