package com.example.phonics_ta_in.core.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.phonics_ta_in.core.curriculum.PhonicsUnit
import java.text.Normalizer
import com.example.phonics_ta_in.core.text.TamilTextSegmenter
import com.example.phonics_ta_in.core.text.TamilTransliteration

/** How an isolated-sound attempt was accepted. */
enum class SoundMatch {
    NONE,           // rejected
    WORD_FALLBACK,  // accepted leniently: the child said a word containing/starting with the sound
    EXACT           // accepted strictly: only the bare sound was heard
}

sealed interface SpeechState {
    object Idle : SpeechState
    object Listening : SpeechState
    object Processing : SpeechState
    data class Recognized(val candidates: List<String>, val topMatch: String, val confidence: Float) : SpeechState
    data class Error(val message: String) : SpeechState
}

/**
 * @param isolatedSound  true when the screen tests a single letter sound (not a word). This tunes the
 *                       recognizer for very short utterances instead of dictation-style speech.
 * @param biasingStrings phrases the recognizer should prefer (e.g. the target letter itself). This is what
 *                       stops the recognizer from "autocorrecting" a lone "அ" into a common word like "அம்மா".
 */
class SpeechRecognizerEngine(
    private val context: Context,
    private val isolatedSound: Boolean = false,
    private val biasingStrings: List<String> = emptyList(),
    private val onStateChanged: (SpeechState) -> Unit
) : RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    init {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@SpeechRecognizerEngine)
            }
        } else {
            onStateChanged(SpeechState.Error("Speech recognition not available on this device/emulator. Use self-check mode."))
        }
    }

    fun startListening() {
        if (isListening) return
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ta-IN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ta-IN")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)

            if (isolatedSound) {
                // Short-utterance tuning: keep the free-form model (known to return candidates on devices)
                // but don't wait seconds after a short "a", and bias towards the bare letter.
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1200L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1000L)
                if (biasingStrings.isNotEmpty()) {
                    // API 33+ (minSdk is 33)
                    putStringArrayListExtra(RecognizerIntent.EXTRA_BIASING_STRINGS, ArrayList(biasingStrings))
                }
            } else {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1500L)
            }
        }
        try {
            speechRecognizer?.startListening(intent)
            isListening = true
            onStateChanged(SpeechState.Listening)
        } catch (e: Exception) {
            onStateChanged(SpeechState.Error(e.message ?: "Failed to start microphone"))
        }
    }

    fun stopListening() {
        if (isListening) {
            try {
                speechRecognizer?.stopListening()
            } catch (_: Exception) {}
            isListening = false
            onStateChanged(SpeechState.Idle)
        }
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
    }

    // --- RecognitionListener Callbacks ---
    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {
        isListening = false
        onStateChanged(SpeechState.Processing)
    }

    override fun onError(error: Int) {
        isListening = false
        val message = when (error) {
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please speak clearly or tap 'I Said It!'"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout. Please try again."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Mic busy. Retrying..."
            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network issue. Use self-check mode below."
            SpeechRecognizer.ERROR_CLIENT -> "Speech recognizer client error. Use self-check mode."
            else -> "Speech error ($error). Use self-check mode below."
        }
        onStateChanged(SpeechState.Error(message))
    }

    override fun onResults(results: Bundle?) {
        isListening = false
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: emptyList<String>()
        val scores = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)

        val topMatch = matches.firstOrNull() ?: ""
        val confidence = scores?.firstOrNull() ?: 0.8f

        Log.d(TAG, "onResults: candidates=$matches, scores=${scores?.toList()}")

        onStateChanged(SpeechState.Recognized(candidates = matches, topMatch = topMatch, confidence = confidence))
    }

    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}

    companion object {
        private const val TAG = "SpeechRecognizer"

        /** A child may repeat the sound ("a a a"); more tokens/letters than this is treated as a word/sentence. */
        private const val MAX_REPEATS = 3

        private val NON_LETTER_REGEX = Regex("[^\\p{L}\\p{M}\\s]+")
        private val WHITESPACE_REGEX = Regex("\\s+")
        private val ELONGATION_REGEX = Regex("(.)\\1{2,}")

        /**
         * Spellings the (English side of the) recognizer commonly uses for a bare sound.
         * Only sound-like spellings belong here, never real words ("and", "am", "mom" ...),
         * otherwise a spoken word would pass an isolated-sound test.
         * The unit's own transliteration and phonicsSound are always accepted in addition.
         */
        private val LATIN_SOUND_SPELLINGS: Map<String, Set<String>> = mapOf(
            "a" to setOf("ah", "uh"),
            "aa" to setOf("aah", "ahh"),
            "i" to setOf("ih"),
            "ii" to setOf("ee", "eeh"),
            "u" to setOf("uh"),
            "uu" to setOf("ooh"),
            "e" to setOf("eh"),
            "ee" to setOf("ey", "ay"),
            "ai" to setOf("eye", "aye"),
            "o" to setOf("oh"),
            "oo" to setOf("ooh", "oh"),
            "au" to setOf("ow", "ou"),
            "k" to setOf("kk"),
            "ka" to setOf("kah"),
            "ng" to setOf("ngg"),
            "nga" to setOf("ngah"),
            "ch" to setOf("chh"),
            "cha" to setOf("chah"),
            "t" to setOf("tt"),
            "ta" to setOf("tah"),
            "th" to setOf("thh"),
            "tha" to setOf("thah"),
            "p" to setOf("pp"),
            "pa" to setOf("pah"),
            "m" to setOf("mm"),
            "ma" to setOf("mah"),
            "y" to setOf("yy"),
            "ya" to setOf("yah"),
            "r" to setOf("rr"),
            "ra" to setOf("rah"),
            "l" to setOf("ll"),
            "la" to setOf("lah"),
            "v" to setOf("vv"),
            "va" to setOf("vah")
        )

        /**
         * Evaluates speech candidates strictly for an ISOLATED letter sound.
         *
         * A candidate passes only if EVERYTHING the recognizer heard is the target sound itself
         * (optionally repeated up to [MAX_REPEATS] times, e.g. "அ", "அ அ", "அஅ", "aa", "ah").
         *
         * Words that merely START with the sound (அம்மா, அப்பா, ஆப்பம், "am", ...) are rejected —
         * previously first-grapheme, keyword/starting-word, vowel-sign and prefix-transliteration
         * matching made those pass while the bare sound was never required.
         */
        fun isIsolatedSoundMatch(candidates: List<String>, unit: PhonicsUnit): Boolean {
            if (candidates.isEmpty()) return false

            val target = Normalizer.normalize(unit.text.trim(), Normalizer.Form.NFC)
            val tamilForms = acceptedTamilForms(target)
            val latinForms = acceptedLatinForms(unit)

            Log.d(TAG, "isIsolatedSoundMatch: target='$target' tamilForms=$tamilForms latinForms=$latinForms")
            Log.d(TAG, "  candidates: $candidates")

            for (cand in candidates) {
                val tokens = tokenize(cand)
                if (tokens.isEmpty()) continue
                if (tokens.size > MAX_REPEATS) {
                    Log.d(TAG, "  reject (too many tokens, looks like a phrase): '$cand'")
                    continue
                }
                if (tokens.all { isSingleSoundToken(it, tamilForms, latinForms) }) {
                    Log.d(TAG, "  MATCH isolated sound: '$cand' tokens=$tokens")
                    return true
                }
                Log.d(TAG, "  reject (not just the target sound): '$cand' tokens=$tokens")
            }

            Log.d(TAG, "  → FAILED: no candidate was the bare sound '$target'")
            return false
        }

        /**
         * Two-tier evaluation used by the letter-sound activity:
         *  1. EXACT         - only the bare sound was heard (strict check above).
         *  2. WORD_FALLBACK - recognizer returned a word containing/starting with the sound (lenient rules).
         *  3. NONE          - neither.
         * The fallback exists because Android's recognizer often cannot output a lone vowel/consonant.
         */
        fun evaluateIsolatedSound(candidates: List<String>, unit: PhonicsUnit): SoundMatch {
            if (isIsolatedSoundMatch(candidates, unit)) return SoundMatch.EXACT
            if (isLenientSoundMatch(candidates, unit)) return SoundMatch.WORD_FALLBACK
            return SoundMatch.NONE
        }

        /**
         * Lenient fallback rules:
         * 1. first Tamil grapheme of any word equals the target
         * 2. candidate/word equals the unit's keyword or one of its starting words
         * 3. a word contains an explicit vowel sign equal to the target vowel (UYIR, except அ)
         * 4. transliteration prefix match and phonetic approximations
         */
        private fun isLenientSoundMatch(candidates: List<String>, unit: PhonicsUnit): Boolean {
            if (candidates.isEmpty()) return false

            val targetText = Normalizer.normalize(unit.text.trim(), Normalizer.Form.NFC)
            val targetTranslit = unit.transliteration.trim().lowercase()
            val phonicsSound = unit.phonicsSound.replace("/", "").trim().lowercase()
            val keywordTamil = Normalizer.normalize(unit.keywordTamil.trim(), Normalizer.Form.NFC)
            val keywordEnglish = unit.keywordEnglish.trim().lowercase()
            val startingWordsTamil: Set<String> = unit.startingWords.map {
                Normalizer.normalize(it.wordTamil, Normalizer.Form.NFC)
            }.toSet()
            val startingWordsEnglish = unit.startingWords.map { it.wordEnglish.lowercase() }.toSet()

            for (cand in candidates) {
                val candStr = Normalizer.normalize(cand.trim(), Normalizer.Form.NFC)
                val candLower = candStr.lowercase()

                if (candStr == targetText || candLower == targetTranslit || candLower == phonicsSound) return true
                if (candStr == keywordTamil || candLower == keywordEnglish
                    || candStr in startingWordsTamil || candLower in startingWordsEnglish) return true

                val words = candStr.split(" ").map { it.trim() }.filter { it.isNotEmpty() }
                for (word in words) {
                    val wordNfc = Normalizer.normalize(word, Normalizer.Form.NFC)
                    val wordLower = wordNfc.lowercase()

                    if (wordNfc == targetText) return true
                    if (wordNfc == keywordTamil || wordLower == keywordEnglish
                        || wordNfc in startingWordsTamil || wordLower in startingWordsEnglish) return true

                    val wordGraphemes = TamilTextSegmenter.segmentGraphemes(wordNfc)
                    val firstGrapheme = wordGraphemes.firstOrNull()?.text
                    if (firstGrapheme != null && firstGrapheme == targetText) return true

                    if (isPhoneticApprox(wordLower, targetTranslit)) return true

                    val wordTranslit = TamilTransliteration.transliterateWord(wordGraphemes).lowercase()
                    if (isStrictTransliterationMatch(wordTranslit, targetTranslit)) return true

                    if (unit.kind == com.example.phonics_ta_in.core.curriculum.UnitKind.UYIR && targetText != "அ") {
                        if (wordGraphemes.any { g -> g.vowelSign == targetText && g.text.length > 1 }) return true
                    }
                }
            }
            Log.d(TAG, "  lenient fallback: no match for $candidates")
            return false
        }

        private fun isStrictTransliterationMatch(candTranslit: String, targetTranslit: String): Boolean {
            if (candTranslit.isEmpty() || targetTranslit.isEmpty()) return false
            if (!candTranslit.startsWith(targetTranslit)) return false
            val afterPrefix = candTranslit.removePrefix(targetTranslit)
            if (afterPrefix.isEmpty()) return true
            return when (targetTranslit) {
                "a"  -> afterPrefix.first() != 'a'
                "i"  -> afterPrefix.first() != 'i'
                "u"  -> afterPrefix.first() != 'u'
                "e"  -> afterPrefix.first() != 'e'
                "o"  -> afterPrefix.first() != 'o'
                "k", "ch", "th", "p", "m", "y", "r", "l", "v" -> afterPrefix.first() != 'a'
                "t"  -> afterPrefix.first() != 'a' && afterPrefix.first() != 'h'
                else -> true
            }
        }

        private fun isPhoneticApprox(candLower: String, targetTranslit: String): Boolean {
            return when (targetTranslit) {
                "a"   -> candLower in setOf("a", "ah", "uh", "aah", "um", "and")
                "aa"  -> candLower in setOf("aa", "aah", "ah ah", "aw")
                "k"   -> candLower in setOf("k", "ik", "kk", "kay")
                "ka"  -> candLower in setOf("ka", "kah", "car", "k")
                else  -> false
            }
        }

        /** NFC, drop zero-width joiners, turn punctuation into spaces, lowercase, split on whitespace. */
        private fun tokenize(raw: String): List<String> {
            val cleaned = Normalizer.normalize(raw, Normalizer.Form.NFC)
                .replace("\u200B", "")
                .replace("\u200C", "")
                .replace("\u200D", "")
                .replace(NON_LETTER_REGEX, " ")
                .lowercase()
                .trim()
            if (cleaned.isEmpty()) return emptyList()
            return cleaned.split(WHITESPACE_REGEX).filter { it.isNotEmpty() }
        }

        /**
         * Tamil graphemes that count as "the target sound".
         *  - the target itself
         *  - for a consonant with pulli ("க்") also the bare stem ("க"): that is how the recognizer writes a spoken "k"
         *  - for a bare consonant stem ("க") also its pulli form ("க்")
         */
        private fun acceptedTamilForms(target: String): Set<String> {
            val forms = mutableSetOf(target)
            if (target.endsWith(TamilTextSegmenter.PULLI)) {
                forms += target.removeSuffix(TamilTextSegmenter.PULLI)
            } else if (target in TamilTextSegmenter.ALL_CONSONANT_STEMS) {
                forms += target + TamilTextSegmenter.PULLI
            }
            return forms
        }

        private fun acceptedLatinForms(unit: PhonicsUnit): Set<String> {
            val translit = unit.transliteration.trim().lowercase()
            val sound = unit.phonicsSound.replace("/", "").trim().lowercase()
            val forms = mutableSetOf<String>()
            if (translit.isNotEmpty()) forms += translit
            if (sound.isNotEmpty()) forms += sound
            LATIN_SOUND_SPELLINGS[translit]?.let { forms += it }
            LATIN_SOUND_SPELLINGS[sound]?.let { forms += it }
            return forms
        }

        private fun isSingleSoundToken(
            token: String,
            tamilForms: Set<String>,
            latinForms: Set<String>
        ): Boolean {
            val hasTamil = token.any { it in '\u0B80'..'\u0BFF' }
            if (hasTamil) {
                // e.g. "அ", "அஅ" (elongated / repeated). Every grapheme must be the target sound.
                val graphemes = TamilTextSegmenter.segmentGraphemes(token)
                return graphemes.isNotEmpty() &&
                        graphemes.size <= MAX_REPEATS &&
                        graphemes.all { it.text in tamilForms }
            }
            // Latin: "aaaa" (stretched sound) is collapsed to "aa" before comparing
            val collapsed = token.replace(ELONGATION_REGEX, "$1$1")
            return token in latinForms || collapsed in latinForms
        }

        /**
         * Evaluates speech candidates against target PhonicsUnit (text, transliteration, keywords).
         */
        fun isUnitMatch(candidates: List<String>, unit: PhonicsUnit): Boolean {
            if (candidates.isEmpty()) return false

            val targetText = unit.text.trim()
            val targetTransliteration = unit.transliteration.trim().lowercase()
            val keywordTamil = unit.keywordTamil.trim()
            val keywordEnglish = unit.keywordEnglish.trim().lowercase()

            for (cand in candidates) {
                val candidateStr = cand.trim()
                val candidateLower = candidateStr.lowercase()

                // 1. Direct text or transliteration match
                if (candidateStr == targetText || candidateLower == targetTransliteration) {
                    return true
                }

                // 2. Transliterated candidate match
                val candGraphemes = TamilTextSegmenter.segmentGraphemes(candidateStr)
                val candTransliterated = TamilTransliteration.transliterateWord(candGraphemes).lowercase()
                if (candTransliterated == targetTransliteration) {
                    return true
                }

                // 3. Keyword match (e.g. "அன்னம்" or "swan" for 'அ', "ஆடு" or "goat" for 'ஆ')
                if (candidateStr == keywordTamil || candidateLower == keywordEnglish) {
                    return true
                }

                // 4. Candidate starts with target letter (e.g. "அன்னம்" starts with 'அ')
                val firstGrapheme = candGraphemes.firstOrNull()?.text
                if (firstGrapheme == targetText) {
                    return true
                }

                // 5. Short vowel sound English approximations ("a", "aa", "ka", "ah", "uh")
                if (targetTransliteration == "a" && (candidateLower == "a" || candidateLower == "ah" || candidateLower == "uh")) return true
                if (targetTransliteration == "aa" && (candidateLower == "aa" || candidateLower == "aah")) return true
                if (targetTransliteration == "ka" && (candidateLower == "ka" || candidateLower == "kah" || candidateLower == "k")) return true
            }

            // 6. Generic grapheme edit distance match on top candidates
            return candidates.any { isMatch(it, targetText, tolerance = 0.60f) }
        }

        /**
         * Compares child spoken text against target Tamil word using grapheme cluster edit distance.
         */
        fun isMatch(spoken: String, target: String, tolerance: Float = 0.70f): Boolean {
            if (spoken.isBlank() || target.isBlank()) return false
            val spokenGraphemes = TamilTextSegmenter.segmentGraphemes(spoken).map { it.text }
            val targetGraphemes = TamilTextSegmenter.segmentGraphemes(target).map { it.text }

            if (spokenGraphemes == targetGraphemes) return true

            val distance = graphemeEditDistance(spokenGraphemes, targetGraphemes)
            val maxLen = maxOf(spokenGraphemes.size, targetGraphemes.size)
            val similarity = 1.0f - (distance.toFloat() / maxLen.toFloat())

            return similarity >= tolerance
        }

        private fun graphemeEditDistance(a: List<String>, b: List<String>): Int {
            val dp = Array(a.size + 1) { IntArray(b.size + 1) }
            for (i in 0..a.size) dp[i][0] = i
            for (j in 0..b.size) dp[0][j] = j

            for (i in 1..a.size) {
                for (j in 1..b.size) {
                    val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                    dp[i][j] = minOf(
                        dp[i - 1][j] + 1,
                        dp[i][j - 1] + 1,
                        dp[i - 1][j - 1] + cost
                    )
                }
            }
            return dp[a.size][b.size]
        }
    }
}
