package com.example.phonics_ta_in.core.speech

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

/**
 * An "always on" microphone for reading aloud.
 *
 * Android's [SpeechRecognizer] ends a session whenever the speaker pauses. This class hides that: it asks for
 * partial results (so words are reported while they are being spoken), and whenever a session ends it quietly starts
 * the next one, so the child never has to tap the microphone again. On Android 13+ it also asks the recognizer for a
 * "segmented session", which keeps listening across pauses without any restart at all (recognizers that do not
 * support it ignore the request and the quiet restart takes over).
 *
 * [pause] / [resume] close and re-open the microphone around the moments the app itself is speaking, so the
 * recognizer never hears the app's own voice.
 *
 * Every callback is delivered on the main thread; call every method from the main thread.
 */
class StreamingSpeechListener(
    private val context: Context,
    private val callbacks: Callbacks
) {
    interface Callbacks {
        /** A new transcript begins (new session, or new segment): words of earlier transcripts are not repeated. */
        fun onTranscriptStarted()

        /** What is currently heard in this transcript. [isFinal] is false for partial results. Best guess first. */
        fun onHeard(candidates: List<String>, isFinal: Boolean)

        /** Listening cannot go on (no permission, no recognizer, repeated failures). The listener has stopped. */
        fun onUnavailable(message: String)
    }

    private val main = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null

    /** Changes whenever the recognizer is created or thrown away; callbacks of an old recognizer are ignored. */
    private var generation = 0
    private var wanted = false
    private var paused = false
    private var destroyed = false
    private var failures = 0
    private var quickEnds = 0
    private var sessionStartedAt = 0L
    private var restartTask: Runnable? = null

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private fun muteSystemBeeps() {
        try {
            audioManager?.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0)
            audioManager?.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0)
            audioManager?.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_MUTE, 0)
        } catch (_: Exception) {}
    }

    private fun restoreSystemBeeps() {
        try {
            audioManager?.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_UNMUTE, 0)
            audioManager?.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, 0)
            audioManager?.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_UNMUTE, 0)
        } catch (_: Exception) {}
    }

    /** Start listening and keep listening until [stop]. */
    fun start() {
        if (destroyed) return
        wanted = true
        paused = false
        failures = 0
        quickEnds = 0
        muteSystemBeeps()
        scheduleRestart(0L)
    }

    /** Close the microphone for good. */
    fun stop() {
        wanted = false
        paused = false
        cancelRestart()
        releaseRecognizer()
        restoreSystemBeeps()
    }

    /** Close the microphone for a moment (the app is about to speak). No-op unless [start]ed. */
    fun pause() {
        if (!wanted || paused) return
        paused = true
        cancelRestart()
        releaseRecognizer()
    }

    /** Open the microphone again after [pause], with a fresh transcript. */
    fun resume() {
        if (!wanted || !paused) return
        paused = false
        failures = 0
        quickEnds = 0
        muteSystemBeeps()
        scheduleRestart(RESUME_DELAY_MS)
    }

    fun destroy() {
        destroyed = true
        stop()
    }

    // ---------------------------------------------------------------------------------------------

    private fun scheduleRestart(delayMs: Long) {
        cancelRestart()
        if (destroyed || !wanted || paused) return
        val task = Runnable {
            restartTask = null
            openSession()
        }
        restartTask = task
        main.postDelayed(task, delayMs)
    }

    private fun cancelRestart() {
        restartTask?.let { main.removeCallbacks(it) }
        restartTask = null
    }

    private fun openSession() {
        if (destroyed || !wanted || paused) return
        val rec = recognizer ?: createRecognizer() ?: return
        sessionStartedAt = SystemClock.elapsedRealtime()
        callbacks.onTranscriptStarted()
        muteSystemBeeps()
        try {
            rec.startListening(buildIntent())
        } catch (e: Exception) {
            Log.w(TAG, "startListening failed", e)
            retryOrGiveUp(e.message ?: "Could not start the microphone.")
        }
    }

    private fun createRecognizer(): SpeechRecognizer? {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            giveUp("Speech recognition is not available on this device. Use the \"I said it!\" button.")
            return null
        }
        val rec = try {
            SpeechRecognizer.createSpeechRecognizer(context)
        } catch (e: Exception) {
            Log.w(TAG, "createSpeechRecognizer failed", e)
            giveUp("Could not start speech recognition. Use the \"I said it!\" button.")
            return null
        }
        generation++
        rec.setRecognitionListener(Listener(generation))
        recognizer = rec
        return rec
    }

    private fun releaseRecognizer() {
        generation++
        val rec = recognizer ?: return
        recognizer = null
        muteSystemBeeps()
        try {
            rec.cancel()
        } catch (_: Exception) {
        }
        try {
            rec.destroy()
        } catch (_: Exception) {
        }
    }

    private fun giveUp(message: String) {
        wanted = false
        paused = false
        cancelRestart()
        releaseRecognizer()
        restoreSystemBeeps()
        callbacks.onUnavailable(message)
    }

    private fun retryOrGiveUp(message: String) {
        failures++
        if (failures > MAX_FAILURES) {
            giveUp(message)
            return
        }
        // A recognizer that failed twice in a row is replaced by a fresh one.
        if (failures >= 2) releaseRecognizer()
        scheduleRestart(RETRY_DELAY_MS * failures)
    }

    private fun buildIntent(): Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ta-IN")
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ta-IN")
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2500L)
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
        if (USE_SEGMENTED_SESSION) putExtra(RecognizerIntent.EXTRA_SEGMENTED_SESSION, true)
    }

    private fun candidatesOf(bundle: Bundle?): List<String> =
        bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.filter { it.isNotBlank() } ?: emptyList()

    // ---------------------------------------------------------------------------------------------

    private inner class Listener(private val id: Int) : RecognitionListener {

        private fun live() = id == generation && !destroyed

        override fun onReadyForSpeech(params: Bundle?) {}

        override fun onBeginningOfSpeech() {
            if (!live()) return
            failures = 0
            quickEnds = 0
        }

        override fun onRmsChanged(rmsdB: Float) {}

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {}

        override fun onEvent(eventType: Int, params: Bundle?) {}

        override fun onPartialResults(partialResults: Bundle?) {
            if (!live()) return
            val heard = candidatesOf(partialResults)
            if (heard.isEmpty()) return
            failures = 0
            quickEnds = 0
            callbacks.onHeard(heard, false)
        }

        override fun onResults(results: Bundle?) {
            if (!live()) return
            val heard = candidatesOf(results)
            if (heard.isNotEmpty()) {
                failures = 0
                quickEnds = 0
                Log.d(TAG, "final: $heard")
                callbacks.onHeard(heard, true)
            }
            // The app may have paused or stopped us while handling the words (a wrong word, the end of the text).
            if (live() && wanted && !paused) scheduleRestart(RESTART_DELAY_MS)
        }

        // Segmented sessions (Android 13+): the recognizer keeps listening; each pause ends a segment.
        override fun onSegmentResults(segmentResults: Bundle) {
            if (!live()) return
            val heard = candidatesOf(segmentResults)
            if (heard.isNotEmpty()) {
                failures = 0
                quickEnds = 0
                Log.d(TAG, "segment: $heard")
                callbacks.onHeard(heard, true)
            }
            if (live() && wanted && !paused) callbacks.onTranscriptStarted()
        }

        override fun onEndOfSegmentedSession() {
            if (!live()) return
            if (wanted && !paused) scheduleRestart(RESTART_DELAY_MS)
        }

        override fun onError(error: Int) {
            if (!live()) return
            Log.d(TAG, "error $error")
            when (error) {
                SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                    // Nobody spoke, or nothing usable was heard: normal while reading. Quietly listen again,
                    // unless the sessions end so fast that something is clearly wrong.
                    val lasted = SystemClock.elapsedRealtime() - sessionStartedAt
                    quickEnds = if (lasted < QUICK_END_MS) quickEnds + 1 else 0
                    if (quickEnds >= MAX_QUICK_ENDS) {
                        giveUp("The microphone keeps stopping. Check that no other app is using it.")
                    } else {
                        scheduleRestart(RESTART_DELAY_MS)
                    }
                }
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                    giveUp("Microphone permission is needed.")
                SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED, SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE ->
                    giveUp("Tamil speech recognition is not available on this device. Use the \"I said it!\" button.")
                else ->
                    retryOrGiveUp("Speech recognition stopped (error $error). Use the \"I said it!\" button.")
            }
        }
    }

    companion object {
        private const val TAG = "StreamingSpeech"

        /**
         * Ask the recognizer to keep listening across pauses (Android 13+). If a device's recognizer misbehaves with
         * it, set this to false: the listener then relies on quick restarts only.
         */
        const val USE_SEGMENTED_SESSION = true

        /** Pause before listening again after a session ended. */
        const val RESTART_DELAY_MS = 150L

        /** Pause after the app finished speaking, so the end of its own voice is not picked up. */
        const val RESUME_DELAY_MS = 350L

        private const val RETRY_DELAY_MS = 400L
        private const val MAX_FAILURES = 4

        /** A session that ends without a result within this time was not a real "nobody spoke". */
        private const val QUICK_END_MS = 700L
        private const val MAX_QUICK_ENDS = 8
    }
}
