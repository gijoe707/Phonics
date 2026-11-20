package com.example.phonics_ta_in.core.audio

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.io.File
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class AppAudioEngine(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isTtsReady = false
    private var isReleased = false

    /** File-synthesis requests made before the TTS engine finished initialising (main thread only). */
    private val pendingSynthesis = ArrayList<() -> Unit>()
    private val synthesisCallbacks = ConcurrentHashMap<String, (Boolean) -> Unit>()
    private val synthesisCounter = AtomicInteger()

    /** "Tell me when the speech has ended" callbacks of [speakTamilSequenceThen], by utterance id. */
    private val speechCallbacks = ConcurrentHashMap<String, (Boolean) -> Unit>()
    private val speechCounter = AtomicInteger()
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("ta", "IN"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = Locale.getDefault()
            }
            tts?.setSpeechRate(0.85f)
            tts?.setOnUtteranceProgressListener(progressListener)
            isTtsReady = true
            val queued = pendingSynthesis.toList()
            pendingSynthesis.clear()
            queued.forEach { it() }
        } else {
            Log.e("AudioEngine", "TTS Initialization failed")
            val queued = pendingSynthesis.toList()
            pendingSynthesis.clear()
            queued.forEach { it() } // each one reports failure because the engine is not ready
        }
    }

    fun speakTamilText(text: String) {
        if (isTtsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tamil_phonics_tts")
        }
    }

    /**
     * Like [speakTamilText], but [onFinished] tells the caller when the speech is over, so that the microphone can be
     * opened again at exactly that moment.
     */
    fun speakTamilTextThen(text: String, onFinished: (Boolean) -> Unit) {
        speakTamilSequenceThen(listOf(text), 0L, onFinished)
    }

    /**
     * Speaks [parts] one after the other (e.g. the syllables of a word and then the whole word), [pauseMs] of silence
     * between them. Anything that was being spoken is cut off.
     *
     * [onFinished] runs exactly once, on the main thread: true when the last part was spoken to the end, false when
     * the speech was cut off, failed, could not start, or the engine never reported back (safety timeout), so a
     * caller waiting for it can never be stuck.
     */
    fun speakTamilSequenceThen(parts: List<String>, pauseMs: Long, onFinished: (Boolean) -> Unit) {
        val engine = tts
        val clean = parts.filter { it.isNotBlank() }
        if (isReleased || !isTtsReady || engine == null || clean.isEmpty()) {
            mainHandler.post { onFinished(false) }
            return
        }
        var queueMode = TextToSpeech.QUEUE_FLUSH
        var lastId = ""
        var allStarted = true
        clean.forEachIndexed { index, part ->
            val isLast = index == clean.lastIndex
            val id = "say_${speechCounter.incrementAndGet()}"
            if (isLast) {
                lastId = id
                speechCallbacks[id] = onFinished
            }
            if (engine.speak(part, queueMode, null, id) != TextToSpeech.SUCCESS) allStarted = false
            queueMode = TextToSpeech.QUEUE_ADD
            if (!isLast && pauseMs > 0L) {
                engine.playSilentUtterance(pauseMs, TextToSpeech.QUEUE_ADD, "${id}_pause")
            }
        }
        if (!allStarted) {
            finishSpeech(lastId, false)
            return
        }
        val limit = (4_000L + clean.sumOf { it.length } * 350L + pauseMs * clean.size).coerceAtMost(30_000L)
        mainHandler.postDelayed({ finishSpeech(lastId, false) }, limit)
    }

    private fun finishSpeech(id: String, completed: Boolean) {
        val callback = speechCallbacks.remove(id) ?: return
        mainHandler.post { callback(completed) }
    }

    /**
     * Renders [text] with the same voice and speaking rate as [speakTamilText] into a WAV [file] instead of the
     * speaker. Used to build the reference sounds the child's microphone recording is compared against.
     *
     * [onDone] receives true when the file was written, false if synthesis failed or was interrupted
     * (e.g. by [speakTamilText] flushing the queue). It may be called on any thread.
     * Call from the main thread.
     */
    fun synthesizeToFile(text: String, file: File, onDone: (Boolean) -> Unit) {
        if (isReleased) {
            onDone(false)
            return
        }
        val job = job@{
            val engine = tts
            if (!isTtsReady || engine == null) {
                onDone(false)
                return@job
            }
            val id = "synth_${synthesisCounter.incrementAndGet()}"
            synthesisCallbacks[id] = onDone
            val rc = engine.synthesizeToFile(text, Bundle(), file, id)
            if (rc != TextToSpeech.SUCCESS) {
                synthesisCallbacks.remove(id)?.invoke(false)
            }
        }
        if (isTtsReady) job() else pendingSynthesis += job
    }

    @Suppress("OVERRIDE_DEPRECATION")
    private val progressListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {}

        override fun onDone(utteranceId: String?) {
            utteranceId?.let {
                synthesisCallbacks.remove(it)?.invoke(true)
                finishSpeech(it, true)
            }
        }

        override fun onError(utteranceId: String?) {
            utteranceId?.let {
                synthesisCallbacks.remove(it)?.invoke(false)
                finishSpeech(it, false)
            }
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            utteranceId?.let {
                synthesisCallbacks.remove(it)?.invoke(false)
                finishSpeech(it, false)
            }
        }

        override fun onStop(utteranceId: String?, interrupted: Boolean) {
            utteranceId?.let {
                synthesisCallbacks.remove(it)?.invoke(false)
                finishSpeech(it, false)
            }
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun release() {
        isReleased = true
        tts?.stop()
        tts?.shutdown()
        tts = null
        pendingSynthesis.clear()
        val callbacks = synthesisCallbacks.values.toList()
        synthesisCallbacks.clear()
        callbacks.forEach { it(false) }
        val speech = speechCallbacks.values.toList()
        speechCallbacks.clear()
        speech.forEach { callback -> mainHandler.post { callback(false) } }
    }
}
