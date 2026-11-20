package com.example.phonics_ta_in.core.speech

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.phonics_ta_in.core.audio.AppAudioEngine
import com.example.phonics_ta_in.core.curriculum.PhonicsUnit
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

/**
 * The "model answers": for every letter, the acoustic fingerprint of what Android text-to-speech says for it.
 *
 * Built lazily and cached for the life of the process. Each letter is rendered to a temporary WAV with the very same
 * TTS voice/rate that the lesson plays to the child, decoded, and reduced to MFCC frames ([AcousticSoundMatcher]).
 *
 * Requests are issued ONE AT A TIME (the next only after the previous finished): if the child taps a letter to hear
 * it while references are still being built, [AppAudioEngine.speakTamilText] flushes the TTS queue, which would
 * otherwise cancel every pending reference at once. A letter whose synthesis was interrupted is simply retried by the
 * next [ensure] call.
 */
object ReferenceSoundBank {

    private const val TAG = "ReferenceSoundBank"

    private val fingerprints = ConcurrentHashMap<String, Array<FloatArray>>()
    private val inFlight: MutableSet<String> = ConcurrentHashMap.newKeySet()
    private val worker = Executors.newSingleThreadExecutor { r -> Thread(r, "ReferenceSoundBank") }
    private val mainHandler = Handler(Looper.getMainLooper())

    fun get(unit: PhonicsUnit): Array<FloatArray>? = fingerprints[unit.id]

    /** References already available for [units], keyed by unit id. */
    fun available(units: Collection<PhonicsUnit>): Map<String, Array<FloatArray>> {
        return units
            .mapNotNull { u -> fingerprints[u.id]?.let { fp -> u.id to fp } }
            .toMap()
    }

    /**
     * Makes sure references exist for [units]; the ones that are missing are synthesized in the background, in the
     * given order (put the target letter first). [onProgress] is called on the main thread after each letter.
     */
    fun ensure(
        context: Context,
        audio: AppAudioEngine,
        units: List<PhonicsUnit>,
        onProgress: () -> Unit = {}
    ) {
        val todo = units.filter { !fingerprints.containsKey(it.id) && inFlight.add(it.id) }
        if (todo.isEmpty()) return
        val cacheDir = context.applicationContext.cacheDir

        fun next(index: Int) {
            if (index >= todo.size) return
            val unit = todo[index]
            val file = File(cacheDir, "ref_sound_${unit.id}.wav")
            audio.synthesizeToFile(unit.text, file) { ok ->
                worker.execute {
                    try {
                        if (ok) build(unit, file)
                    } catch (e: Exception) {
                        Log.w(TAG, "reference for '${unit.text}' failed", e)
                    } finally {
                        file.delete()
                        inFlight.remove(unit.id)
                    }
                    mainHandler.post {
                        onProgress()
                        next(index + 1)
                    }
                }
            }
        }
        mainHandler.post { next(0) }
    }

    private fun build(unit: PhonicsUnit, file: File) {
        if (!file.exists() || file.length() < 100) {
            Log.w(TAG, "no audio produced for '${unit.text}'")
            return
        }
        val pcm = WavDecoder.decode(file.readBytes())
        if (pcm == null) {
            Log.w(TAG, "could not decode WAV for '${unit.text}'")
            return
        }
        val fp = AcousticSoundMatcher.fingerprintOf(pcm.samples, pcm.sampleRate)
        if (fp == null) {
            Log.w(TAG, "reference for '${unit.text}' was silent/too short (${pcm.samples.size} samples @ ${pcm.sampleRate})")
            return
        }
        fingerprints[unit.id] = fp
        Log.d(TAG, "reference ready: '${unit.text}' ${fp.size} frames (${pcm.sampleRate} Hz source)")
    }
}
