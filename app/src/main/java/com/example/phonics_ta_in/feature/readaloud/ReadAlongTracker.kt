package com.example.phonics_ta_in.feature.readaloud

import com.example.phonics_ta_in.core.speech.WordMatching

enum class WordStatus {
    /** Not read yet. */
    PENDING,

    /** Read correctly (turns green). */
    CORRECT,

    /** Heard something else for this word (turns yellow until it is read correctly). */
    WRONG,

    /** Nothing to say aloud (a number, a dash ...): passed over automatically. */
    SKIPPED
}

sealed interface ReadAlongEvent {
    data class WordCorrect(val index: Int) : ReadAlongEvent

    /** The word at [index] was said differently ([heard]); everything the child says after it is ignored for now. */
    data class WordWrong(val index: Int, val heard: String) : ReadAlongEvent

    /** The last word has been read. */
    object Finished : ReadAlongEvent
}

/**
 * Follows a child reading [words] aloud, word by word, from the streaming recognizer's running transcript.
 *
 * Why this is not just "compare each result": the microphone stays open, so the recognizer keeps sending a growing
 * transcript ("அம்மா", "அம்மா கை", "அம்மா கை தொட்டாள்" ...). The tracker remembers how much of the current transcript
 * it has already used and only looks at what is new.
 *
 *  - A word is accepted as soon as the recognizer has produced it exactly, or - once the recognizer has moved on to
 *    the next word / finished - when it is close enough ([WordMatching.isMatch]).
 *  - A word is only judged WRONG when the recognizer is done with it, never while it may still be half-spoken.
 *  - After a wrong word the tracker is blocked: whatever else is in the transcript is ignored until
 *    [startTranscript] (the microphone was re-opened), so the child is never marked on words spoken after a mistake.
 *  - The recognizer may glue two words together or split one; both are tolerated. Repeating the previous word
 *    ("அம்மா அம்மா கை") is not a mistake either.
 *
 * Pure Kotlin: no Android classes.
 */
class ReadAlongTracker(
    words: List<String>,
    initialPosition: Int = 0,
    private val tolerance: Float = WordMatching.DEFAULT_TOLERANCE
) {
    private val keys: List<String> = words.map { WordMatching.key(it) }
    private val status = MutableList(words.size) { WordStatus.PENDING }

    /** Index of the next word to read (== [size] when everything has been read). */
    var position: Int = initialPosition.coerceIn(0, keys.size)
        private set

    /** Words of the current transcript that have been used up already. */
    private var consumed = 0

    /** Number of words in the latest transcript seen (used by [markCurrentRead]). */
    private var seenTokens = 0

    /** True after a wrong word, until the next transcript starts. */
    var isBlocked: Boolean = false
        private set

    val size: Int get() = keys.size
    val isFinished: Boolean get() = position >= keys.size
    val statuses: List<WordStatus> get() = status.toList()

    init {
        for (i in 0 until position) {
            status[i] = WordStatus.CORRECT
        }
        skipUnreadable()
    }

    /** The recognizer started a new transcript (new session, or new segment): nothing of it has been used yet. */
    fun startTranscript() {
        consumed = 0
        seenTokens = 0
        isBlocked = false
    }

    /** The child tapped "I said it": accept the current word without listening. */
    fun markCurrentRead(): List<ReadAlongEvent> {
        if (isFinished) return emptyList()
        val index = position
        status[index] = WordStatus.CORRECT
        position = index + 1
        skipUnreadable()
        // Whatever is already in the running transcript belongs to the words before this one.
        consumed = maxOf(consumed, seenTokens)
        val events = ArrayList<ReadAlongEvent>()
        events += ReadAlongEvent.WordCorrect(index)
        if (isFinished) events += ReadAlongEvent.Finished
        return events
    }

    /** Back to the first word, nothing read. */
    fun reset() {
        for (i in status.indices) status[i] = WordStatus.PENDING
        position = 0
        startTranscript()
        skipUnreadable()
    }

    /**
     * Feed what the recognizer currently hears.
     *
     * @param candidates the recognizer's alternatives, best first. Partial results usually have just one.
     * @param lastWordComplete false while the recognizer may still be in the middle of the last word (partial
     *                         result); true for a final result, or when the transcript has not changed for a while.
     */
    fun accept(candidates: List<String>, lastWordComplete: Boolean): List<ReadAlongEvent> {
        if (candidates.isEmpty()) return emptyList()
        seenTokens = WordMatching.tokenize(candidates[0]).size
        if (isFinished || isBlocked) return emptyList()

        // Only a finished utterance has trustworthy alternatives; a partial one is judged on its best guess.
        val pool = if (lastWordComplete) candidates else candidates.take(1)
        val plans = pool.map { plan(WordMatching.tokenize(it), lastWordComplete) }
            .filter { it.correct.isNotEmpty() || it.wrongIndex >= 0 }
        if (plans.isEmpty()) return emptyList()

        // Most words read correctly wins; on a tie the alternative without a mistake; then the recognizer's order.
        var best = plans[0]
        for (candidate in plans.drop(1)) {
            val better = candidate.correct.size > best.correct.size ||
                (candidate.correct.size == best.correct.size && best.wrongIndex >= 0 && candidate.wrongIndex < 0)
            if (better) best = candidate
        }
        return apply(best)
    }

    // ---------------------------------------------------------------------------------------------

    private class Plan(
        val correct: List<Int>,
        val wrongIndex: Int,
        val heard: String,
        val consumed: Int
    )

    private fun plan(tokens: List<String>, lastWordComplete: Boolean): Plan {
        val correct = ArrayList<Int>()
        var p = position
        var i = consumed
        var lastCorrect = previousRead(position)
        var wrongIndex = -1
        var heard = ""

        while (i < tokens.size) {
            p = nextReadable(p)
            if (p >= keys.size) break

            val token = tokens[i]
            val tokenDone = i < tokens.size - 1 || lastWordComplete

            // How could this token be read? Take the best fit, so that a glued "அம்மாகை" is not taken for a
            // slightly wrong "அம்மா", and a plain "அம்மா" is not taken for a badly glued "அம்மாகை".
            var kind = 0
            var best = -1f
            // 1. the word itself
            val direct = score(token, keys[p], tokenDone)
            if (direct > best) { best = direct; kind = 1 }
            // 2. the recognizer split the word in two ("தண் ணீர்")
            if (i + 1 < tokens.size) {
                val joinedDone = i + 1 < tokens.size - 1 || lastWordComplete
                val split = score(token + tokens[i + 1], keys[p], joinedDone)
                if (split > best) { best = split; kind = 2 }
            }
            // 3. the recognizer glued this word to the next one ("அம்மாகை")
            val q = nextReadable(p + 1)
            if (q < keys.size) {
                val glued = score(token, keys[p] + keys[q], tokenDone)
                if (glued > best) { best = glued; kind = 3 }
            }
            if (kind == 1) {
                correct += p; lastCorrect = p; p++; i++
                continue
            }
            if (kind == 2) {
                correct += p; lastCorrect = p; p++; i += 2
                continue
            }
            if (kind == 3) {
                correct += p; correct += q; lastCorrect = q; p = q + 1; i++
                continue
            }
            // 4. the child said the previous word again: not a mistake
            if (lastCorrect >= 0 && score(token, keys[lastCorrect], tokenDone) >= 0f) {
                i++
                continue
            }
            // 5. something else. Only judge it when the recognizer is done with the word.
            if (tokenDone) {
                wrongIndex = p
                heard = token
                i++
            }
            break
        }
        return Plan(correct, wrongIndex, heard, i)
    }

    private fun apply(plan: Plan): List<ReadAlongEvent> {
        val events = ArrayList<ReadAlongEvent>()
        for (index in plan.correct) {
            for (k in position until index) if (keys[k].isEmpty()) status[k] = WordStatus.SKIPPED
            status[index] = WordStatus.CORRECT
            position = index + 1
            events += ReadAlongEvent.WordCorrect(index)
        }
        skipUnreadable()
        consumed = plan.consumed
        if (plan.wrongIndex >= 0) {
            status[plan.wrongIndex] = WordStatus.WRONG
            isBlocked = true
            events += ReadAlongEvent.WordWrong(plan.wrongIndex, plan.heard)
        }
        if (isFinished) events += ReadAlongEvent.Finished
        return events
    }

    /**
     * How well [spoken] fits [target]: 0..1, or -1 when it does not fit. While the recognizer may still be in the
     * middle of the word ([wordDone] false) only identical letters fit; afterwards close is good enough.
     */
    private fun score(spoken: String, target: String, wordDone: Boolean): Float {
        val s = WordMatching.key(spoken)
        val t = WordMatching.key(target)
        if (s.isEmpty() || t.isEmpty()) return -1f
        if (!wordDone) return if (s == t) 1f else -1f
        val similarity = WordMatching.similarity(s, t)
        return if (similarity >= tolerance) similarity else -1f
    }

    /** First readable word at or after [from]; [size] when there is none. */
    private fun nextReadable(from: Int): Int {
        var k = from
        while (k < keys.size && keys[k].isEmpty()) k++
        return k
    }

    /** The last word before [before] that was read (not skipped), or -1. */
    private fun previousRead(before: Int): Int {
        var k = before - 1
        while (k >= 0 && status[k] != WordStatus.CORRECT) k--
        return k
    }

    private fun skipUnreadable() {
        while (position < keys.size && keys[position].isEmpty()) {
            status[position] = WordStatus.SKIPPED
            position++
        }
    }
}
