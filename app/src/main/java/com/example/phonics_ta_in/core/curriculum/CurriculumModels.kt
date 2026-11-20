package com.example.phonics_ta_in.core.curriculum

enum class UnitKind { UYIR, MEI, UYIRMEI, AYTHAM }

/**
 * Single-letter word with explicit traditional Tamil meaning (ஓரெழுத்து ஒருமொழி)
 * e.g. ஆ = Cow, கை = Hand, தீ = Fire, கோ = King/Cow
 */
data class SingleLetterWord(
    val letter: String,
    val meaningTamil: String,
    val meaningEnglish: String,
    val transliteration: String
)

/**
 * Vocabulary word starting with specific vowel or consonant
 * e.g. அ -> அம்மா (Mother), ஆ -> ஆடு (Goat), க -> கப்பல் (Ship)
 */
data class StartingSoundWord(
    val wordTamil: String,
    val wordEnglish: String,
    val startingLetter: String,
    val transliteration: String,
    val syllables: List<String> = emptyList(),
    /** Key of the programmatically drawn picture (see WordIllustration), e.g. "crow", "mother_baby" */
    val illustration: String = ""
)

data class PhonicsUnit(
    val id: String,
    val text: String,
    val kind: UnitKind,
    val transliteration: String,
    val phonicsSound: String, // e.g. "/a/" or "/aa/" or "/ka/"
    val keywordTamil: String,
    val keywordEnglish: String,
    val audioKey: String,
    val singleLetterWord: SingleLetterWord? = null,
    val startingWords: List<StartingSoundWord> = emptyList(),
    val description: String = ""
)

enum class ExerciseType {
    PRECURSOR_HEAR_SOUND,       // Listen to precursor isolated sound
    LISTENING_SKILL_TEST,       // Skill 1: Listening test (hear sound -> pick letter)
    READING_SKILL_TEST,         // Skill 2: Reading test (see letter -> pick sound)
    WRITING_SKILL_TEST,         // Skill 3: Writing/Tracing test
    SPEECH_PRECURSOR_TEST,      // Skill 4: Speech test (isolated letter sound)
    SINGLE_LETTER_MEANING_TEST, // Single-letter word meaning (ஓரெழுத்து ஒருமொழி e.g. கை=Hand, ஆ=Cow)
    WORD_VOCABULARY_SPEECH_TEST, // Vocabulary word speech test (e.g. அம்மா, ஆடு, கப்பல்)
    HEAR_AND_SEE,
    LISTEN_AND_TAP_LETTER,
    LISTEN_AND_TAP_PICTURE,
    TRACE_LETTER,
    SAY_LETTER
}

data class ExerciseOption(
    val id: String,
    val text: String,
    val subtext: String = "",
    val isCorrect: Boolean = false
)

/**
 * Normalized 0.0 to 1.0 point for letter stroke path tracing templates
 */
data class NormalizedPoint(val x: Float, val y: Float)

data class LessonExercise(
    val id: String,
    val type: ExerciseType,
    val targetUnit: PhonicsUnit,
    val promptText: String,
    val options: List<ExerciseOption> = emptyList(),
    val templateStrokes: List<List<NormalizedPoint>> = emptyList(),
    val targetWord: StartingSoundWord? = null,
    val singleLetterWord: SingleLetterWord? = null
)

data class LetterGroup(
    val id: String,
    val groupNumber: Int,
    val titleTamil: String,
    val titleEnglish: String,
    val units: List<PhonicsUnit>,
    val isUnlocked: Boolean = true
)

/**
 * One consonant and everything built from it: the mei (e.g. க்) plus its 12 uyirmei forms
 * (க, கா, கி, கீ, கு, கூ, கெ, கே, கை, கொ, கோ, கௌ) = 13 letters, each with its own exercise set.
 *
 * @param index 1-based position of the consonant in the Tamil alphabet order (க் = 1 ... ன் = 18)
 */
data class ConsonantFamily(
    val index: Int,
    val mei: PhonicsUnit,
    val uyirmei: List<PhonicsUnit>
) {
    /** The mei first, then the 12 uyirmei forms in vowel order. */
    val units: List<PhonicsUnit> get() = listOf(mei) + uyirmei
}
