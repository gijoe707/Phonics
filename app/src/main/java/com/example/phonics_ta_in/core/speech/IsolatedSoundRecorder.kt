package com.example.phonics_ta_in.core.speech

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Records ONE short sound (a child saying a single vowel/consonant) from the microphone as 16 kHz mono PCM.
 *
 * Recording starts immediately, the background noise level is measured for the first ~200 ms, then it waits for the
 * child to speak and stops once there has been about 0.7 s of silence after the sound (or a hard time limit).
 *
 * Must NOT run at the same time as [android.speech.SpeechRecognizer] (both want the microphone). The microphone is
 * always released BEFORE the [Outcome] is delivered, so the caller can start the recognizer fallback straight from
 * the callback. The RECORD_AUDIO permission must already be granted.
 *
 * Callbacks are delivered on the main thread.
 */
class IsolatedSoundRecorder {

    sealed interface Outcome {
        /** 16 kHz mono samples in -1..1, starting ~200 ms before the sound began. */
        class Captured(val samples: FloatArray) : Outcome
        object NoSpeech : Outcome
        class Failed(val message: String) : Outcome
    }

    private val mainHandler = Handler(Looper.getMainLooper())

    @Volatile private var cancelled = false
    @Volatile private var thread: Thread? = null

    val isRecording: Boolean get() = thread?.isAlive == true

    /** Starts recording. [onSpeechStarted] fires (main thread) when the child's voice is first detected. */
    fun start(onSpeechStarted: () -> Unit = {}, onOutcome: (Outcome) -> Unit) {
        if (isRecording) return
        cancelled = false
        val t = Thread({
            val outcome = record(onSpeechStarted)   // microphone is released when this returns
            if (outcome != null) {
                mainHandler.post { if (!cancelled) onOutcome(outcome) }
            }
        }, "IsolatedSoundRecorder")
        thread = t
        t.start()
    }

    /** Stops recording without delivering an outcome. */
    fun cancel() {
        cancelled = true
    }

    /** Returns null when cancelled. */
    @SuppressLint("MissingPermission")
    private fun record(onSpeechStarted: () -> Unit): Outcome? {
        val minBuffer = AudioRecord.getMinBufferSize(
            SoundFingerprint.SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBuffer <= 0) return Outcome.Failed("Microphone is not available on this device.")

        var audioRecord: AudioRecord? = null
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.VOICE_RECOGNITION,
                SoundFingerprint.SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                max(minBuffer, CHUNK * 2 * 8)
            )
            if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                return Outcome.Failed("Could not open the microphone (is another app using it?).")
            }
            audioRecord.startRecording()

            val chunk = ShortArray(CHUNK)
            val all = FloatArray(MAX_SAMPLES)
            var total = 0
            var noiseSum = 0f
            var noiseChunks = 0
            var threshold = MIN_THRESHOLD
            var speechStart = -1
            var lastLoud = -1

            while (!cancelled && total + CHUNK <= MAX_SAMPLES) {
                val read = audioRecord.read(chunk, 0, CHUNK)
                if (read <= 0) return Outcome.Failed("Microphone stopped unexpectedly.")

                var sumSq = 0f
                for (i in 0 until read) {
                    val v = chunk[i] / 32768f
                    all[total + i] = v
                    sumSq += v * v
                }
                val rms = sqrt(sumSq / read)
                total += read

                if (noiseChunks < NOISE_CHUNKS) {
                    // Measure the room's background level before listening for the child.
                    noiseSum += rms
                    noiseChunks++
                    if (noiseChunks == NOISE_CHUNKS) {
                        threshold = (noiseSum / noiseChunks * 3f).coerceIn(MIN_THRESHOLD, MAX_THRESHOLD)
                    }
                    continue
                }

                if (rms >= threshold) {
                    if (speechStart < 0) {
                        speechStart = total - read
                        mainHandler.post { if (!cancelled) onSpeechStarted() }
                    }
                    lastLoud = total
                }

                if (speechStart >= 0 && total - lastLoud >= TRAILING_SILENCE) break
                if (speechStart >= 0 && total - speechStart >= MAX_SPEECH) break
                if (speechStart < 0 && total >= NO_SPEECH_TIMEOUT) break
            }

            if (cancelled) return null
            if (speechStart < 0) return Outcome.NoSpeech

            val from = max(0, speechStart - PRE_ROLL)
            Log.d(TAG, "captured ${(total - from) * 1000 / SoundFingerprint.SAMPLE_RATE} ms, threshold=$threshold")
            return Outcome.Captured(all.copyOfRange(from, total))
        } catch (e: SecurityException) {
            return Outcome.Failed("Microphone permission required.")
        } catch (e: Exception) {
            Log.e(TAG, "recording failed", e)
            return Outcome.Failed(e.message ?: "Recording failed.")
        } finally {
            try {
                audioRecord?.stop()
            } catch (_: Exception) {
            }
            audioRecord?.release()
            thread = null
        }
    }

    private companion object {
        const val TAG = "SoundRecorder"
        const val RATE = SoundFingerprint.SAMPLE_RATE
        const val CHUNK = RATE / 50                   // 20 ms
        const val NOISE_CHUNKS = 10                   // first 200 ms = background-noise estimate
        const val MIN_THRESHOLD = 0.02f
        const val MAX_THRESHOLD = 0.05f
        const val PRE_ROLL = RATE / 5                 // keep 200 ms before the sound
        const val TRAILING_SILENCE = RATE * 7 / 10    // stop 0.7 s after the sound ends
        const val MAX_SPEECH = RATE * 3               // a single sound never needs more than 3 s
        const val NO_SPEECH_TIMEOUT = RATE * 4        // give up after 4 s of nothing
        const val MAX_SAMPLES = RATE * 8
    }
}
