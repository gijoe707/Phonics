package com.example.phonics_ta_in.feature.readaloud

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.phonics_ta_in.core.audio.AppAudioEngine
import com.example.phonics_ta_in.core.speech.StreamingSpeechListener
import com.example.phonics_ta_in.core.speech.WordMatching
import com.example.phonics_ta_in.core.text.TamilSyllableSegmenter

enum class MicState {
    /** Not listening. */
    OFF,

    /** Listening, the child just reads. */
    LISTENING,

    /** The microphone is closed for a moment because the helper is speaking. It re-opens by itself. */
    HELPING
}

/**
 * Runs one reading session (the built-in story or a pasted text) over all [sentences] as one stream of words:
 *
 *  - the microphone is opened once and stays open while the child reads (see [StreamingSpeechListener]);
 *  - every word the child gets right turns green, with no spoken feedback;
 *  - when a word is said wrongly it turns yellow, the microphone closes, the helper says that word (the only time
 *    the app speaks on its own), and the microphone opens again by itself for another try.
 *
 * All state the screen shows is Compose state; call everything on the main thread.
 */
class ReadAlongController(
    context: Context,
    private val audio: AppAudioEngine,
    private val sentences: List<ReadAloudSentence>,
    initialPosition: Int = 0,
    private val onPositionChanged: ((Int) -> Unit)? = null
) : StreamingSpeechListener.Callbacks {

    /** Every word of every sentence, in reading order. */
    val words: List<String> = sentences.flatMap { it.words }

    private val starts: IntArray = IntArray(sentences.size).also { out ->
        var next = 0
        sentences.forEachIndexed { i, s -> out[i] = next; next += s.words.size }
    }

    private val tracker = ReadAlongTracker(words, initialPosition)
    private val handler = Handler(Looper.getMainLooper())
    private val listener = StreamingSpeechListener(context, this)

    var statuses: List<WordStatus> by mutableStateOf(tracker.statuses)
        private set

    /** Index of the next word to read. */
    var position: Int by mutableIntStateOf(tracker.position)
        private set

    var currentSentence: Int by mutableIntStateOf(sentenceOf(minOf(tracker.position, words.lastIndex)))
        private set

    var micState: MicState by mutableStateOf(MicState.OFF)
        private set

    var message: String by mutableStateOf(IDLE_MESSAGE)
        private set

    var finished: Boolean by mutableStateOf(tracker.isFinished)
        private set

    /** Breakdown pop-up state for a word tapped by the user. */
    var activeBreakdownWord: BreakdownWordState? by mutableStateOf(null)
        private set

    /** Word being read out by the helper during [readSentenceAloud], or -1. */
    var demoWord: Int by mutableIntStateOf(-1)
        private set

    val sentenceCount: Int get() = sentences.size

    fun startIndexOf(sentence: Int): Int = starts[sentence]

    private var micWanted = false
    private var released = false
    private var pendingSpeech = 0
    private var lastHeard: List<String> = emptyList()
    private val missCounts = HashMap<Int, Int>()

    /** The transcript stood still for a while: whatever the last word is, the child has finished saying it. */
    private val settleTask = Runnable {
        if (!released && micState == MicState.LISTENING && lastHeard.isNotEmpty()) {
            applyHeard(lastHeard, complete = true)
        }
    }

    // ---- microphone ---------------------------------------------------------------------------

    fun startMic() {
        if (released || finished) return
        micWanted = true
        micState = MicState.LISTENING
        message = LISTENING_MESSAGE
        listener.start()
    }

    fun stopMic() {
        if (!micWanted && micState == MicState.OFF) return
        micWanted = false
        handler.removeCallbacks(settleTask)
        listener.stop()
        micState = MicState.OFF
        message = IDLE_MESSAGE
    }

    fun showMessage(text: String) {
        message = text
    }

    override fun onTranscriptStarted() {
        if (released) return
        handler.removeCallbacks(settleTask)
        lastHeard = emptyList()
        tracker.startTranscript()
    }

    override fun onHeard(candidates: List<String>, isFinal: Boolean) {
        if (released || micState != MicState.LISTENING) return
        handler.removeCallbacks(settleTask)
        lastHeard = candidates
        Log.d(TAG, "heard final=$isFinal $candidates (next: '${words.getOrNull(tracker.position)}')")
        applyHeard(candidates, complete = isFinal)
        if (!isFinal && micState == MicState.LISTENING && !tracker.isBlocked) {
            handler.postDelayed(settleTask, SETTLE_MS)
        }
    }

    override fun onUnavailable(message: String) {
        if (released) return
        micWanted = false
        micState = MicState.OFF
        this.message = message
    }

    // ---- words --------------------------------------------------------------------------------

    private fun applyHeard(candidates: List<String>, complete: Boolean) {
        handleEvents(tracker.accept(candidates, complete))
    }

    /** "I said it!": accept the current word without listening (also the way on when speech recognition is missing). */
    fun markCurrentWordRead() {
        if (released || finished) return
        if (activeBreakdownWord?.wordIndex == position) {
            activeBreakdownWord = null
        }
        handleEvents(tracker.markCurrentRead())
    }

    private fun handleEvents(events: List<ReadAlongEvent>) {
        if (events.isEmpty()) return
        publish()
        for (event in events) {
            when (event) {
                is ReadAlongEvent.WordWrong -> helpWith(event.index, event.heard)
                is ReadAlongEvent.Finished -> finishReading()
                is ReadAlongEvent.WordCorrect -> {}
            }
        }
    }

    private fun publish() {
        statuses = tracker.statuses
        position = tracker.position
        currentSentence = sentenceOf(minOf(tracker.position, words.lastIndex))
        finished = tracker.isFinished
        onPositionChanged?.invoke(tracker.position)
    }

    private fun sentenceOf(word: Int): Int {
        var s = starts.lastIndex
        while (s > 0 && starts[s] > word) s--
        return s
    }

    private fun finishReading() {
        micWanted = false
        handler.removeCallbacks(settleTask)
        listener.stop()
        micState = MicState.OFF
        message = ""
    }

    /** Start again from the first word. */
    fun reset() {
        if (released) return
        micWanted = false
        handler.removeCallbacks(settleTask)
        listener.stop()
        tracker.reset()
        missCounts.clear()
        demoWord = -1
        activeBreakdownWord = null
        micState = MicState.OFF
        message = IDLE_MESSAGE
        publish()
    }

    // ---- breakdown dialog & tapping ----------------------------------------------------------

    /** The child tapped a word: open breakdown dialog based on attempt count (letters -> syllables -> full word). */
    fun onWordTapped(index: Int) {
        if (released || index !in words.indices) return
        val word = words[index]
        val misses = missCounts[index] ?: 0
        val mode = when {
            misses == 1 -> BreakdownMode.LETTERS
            misses == 2 -> BreakdownMode.SYLLABLES
            misses >= 3 -> BreakdownMode.FULL_WORD
            else -> BreakdownMode.LETTERS
        }
        activeBreakdownWord = BreakdownWordState(index, word, mode, misses)
    }

    fun closeBreakdownDialog() {
        activeBreakdownWord = null
    }

    fun setBreakdownMode(mode: BreakdownMode) {
        val current = activeBreakdownWord ?: return
        activeBreakdownWord = current.copy(mode = mode)
    }

    /** Speak a letter sound, syllable sound, or full word sound when tapped by the user. */
    fun playSoundPart(text: String) {
        if (released || text.isBlank()) return
        val spoken = WordMatching.key(text).ifEmpty { text }
        if (micWanted) {
            handler.removeCallbacks(settleTask)
            listener.pause()
            micState = MicState.HELPING
            message = LISTEN_MESSAGE
        }
        pendingSpeech++
        audio.speakTamilSequenceThen(listOf(spoken), 0L) { onSpeechEnded() }
    }

    /** Speak syllables sequence when requested by user tap. */
    fun playSyllablesSequence(syllables: List<String>) {
        if (released || syllables.isEmpty()) return
        val parts = syllables.map { WordMatching.key(it).ifEmpty { it } }
        if (micWanted) {
            handler.removeCallbacks(settleTask)
            listener.pause()
            micState = MicState.HELPING
            message = LISTEN_MESSAGE
        }
        pendingSpeech++
        audio.speakTamilSequenceThen(parts, SYLLABLE_PAUSE_MS) { onSpeechEnded() }
    }

    /** The word at [index] was said wrongly: record miss count; keep mic listening continuously without prompting. */
    private fun helpWith(index: Int, heard: String) {
        Log.d(TAG, "wrong: expected '${words[index]}', heard '$heard'")
        val misses = (missCounts[index] ?: 0) + 1
        missCounts[index] = misses
        message = LISTENING_MESSAGE
    }

    /** The child tapped a word: say it. */
    fun speakWord(index: Int) {
        if (released || index !in words.indices) return
        val spoken = WordMatching.key(words[index]).ifEmpty { words[index] }
        speakWithMicClosed(listOf(spoken), 0L, LISTEN_MESSAGE)
    }

    /** The speaker button: read a whole sentence to the child, word by word, highlighting each word. */
    fun readSentenceAloud(sentence: Int) {
        if (released || sentence !in sentences.indices) return
        val first = starts[sentence]
        val last = first + sentences[sentence].words.size - 1
        if (micWanted) {
            handler.removeCallbacks(settleTask)
            listener.pause()
            micState = MicState.HELPING
            message = LISTEN_MESSAGE
        }
        pendingSpeech++

        fun next(i: Int) {
            if (released) return
            if (i > last) {
                demoWord = -1
                onSpeechEnded()
                return
            }
            val spoken = WordMatching.key(words[i])
            if (spoken.isEmpty()) {
                next(i + 1)
                return
            }
            demoWord = i
            audio.speakTamilSequenceThen(listOf(spoken), 0L) { completed ->
                if (completed) {
                    next(i + 1)
                } else {
                    demoWord = -1
                    onSpeechEnded()
                }
            }
        }
        next(first)
    }

    private fun speakWithMicClosed(parts: List<String>, pauseMs: Long, status: String) {
        if (released) return
        if (micWanted) {
            handler.removeCallbacks(settleTask)
            listener.pause()
            micState = MicState.HELPING
            message = status
        }
        pendingSpeech++
        audio.speakTamilSequenceThen(parts, pauseMs) { onSpeechEnded() }
    }

    private fun onSpeechEnded() {
        if (pendingSpeech > 0) pendingSpeech--
        if (pendingSpeech == 0) resumeMicIfWanted()
    }

    private fun resumeMicIfWanted() {
        if (released || finished || !micWanted) return
        micState = MicState.LISTENING
        message = LISTENING_MESSAGE
        listener.resume()
    }

    fun release() {
        released = true
        micWanted = false
        handler.removeCallbacksAndMessages(null)
        listener.destroy()
    }

    companion object {
        private const val TAG = "ReadAlong"

        /** No change in the transcript for this long = the child has finished the last word. */
        const val SETTLE_MS = 1300L

        /** From this many misses of the same word on, the helper says it syllable by syllable first. */
        const val SYLLABLES_AFTER_MISSES = 2
        const val SYLLABLE_PAUSE_MS = 450L

        const val IDLE_MESSAGE = "Tap the microphone, then just read"
        const val LISTENING_MESSAGE = "Listening... keep reading"
        const val HELPING_MESSAGE = "Listen, then say it again"
        const val LISTEN_MESSAGE = "Listen..."
    }
}
