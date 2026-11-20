package com.example.phonics_ta_in.feature.readaloud

data class ReadAloudSentence(
    val id: String,
    val text: String,
    val words: List<String>,
    val englishTranslation: String
)

data class ReadAloudStory(
    val id: String,
    val titleTamil: String,
    val titleEnglish: String,
    val gradeLevel: String,
    val iconEmoji: String,
    val description: String,
    val sentences: List<ReadAloudSentence>
)

enum class BreakdownMode {
    LETTERS,
    SYLLABLES,
    FULL_WORD
}

data class BreakdownWordState(
    val wordIndex: Int,
    val word: String,
    val mode: BreakdownMode,
    val misses: Int
)
