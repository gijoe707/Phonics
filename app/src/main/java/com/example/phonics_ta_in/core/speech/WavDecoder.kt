package com.example.phonics_ta_in.core.speech

/**
 * Minimal RIFF/WAVE reader for the files written by [android.speech.tts.TextToSpeech.synthesizeToFile].
 * Pure Kotlin (no Android classes) so it can be unit tested.
 *
 * Supports 8/16/24-bit PCM and 32-bit float, any channel count (mixed down to mono).
 * Some TTS engines stream their output and leave the 'data' chunk size as 0 or 0xFFFFFFFF in the header,
 * so a missing/oversized length means "everything until the end of the file".
 */
object WavDecoder {

    class Pcm(val samples: FloatArray, val sampleRate: Int)

    fun decode(bytes: ByteArray): Pcm? {
        if (bytes.size < 12 || !hasTag(bytes, 0, "RIFF") || !hasTag(bytes, 8, "WAVE")) return null

        var format = 1
        var channels = 1
        var sampleRate = 0
        var bits = 16
        var dataStart = -1
        var dataLen = 0

        var pos = 12
        while (pos + 8 <= bytes.size) {
            val size = le32(bytes, pos + 4)
            val body = pos + 8
            if (hasTag(bytes, pos, "fmt ") && body + 16 <= bytes.size) {
                format = le16(bytes, body)
                channels = le16(bytes, body + 2)
                sampleRate = le32(bytes, body + 4)
                bits = le16(bytes, body + 14)
                // WAVE_FORMAT_EXTENSIBLE: the real format code is the first 2 bytes of the sub-format GUID
                if (format == 0xFFFE && size >= 26 && body + 26 <= bytes.size) format = le16(bytes, body + 24)
            } else if (hasTag(bytes, pos, "data")) {
                dataStart = body
                dataLen = if (size <= 0 || body.toLong() + size > bytes.size) bytes.size - body else size
                break
            }
            if (size < 0) break
            pos = body + size + (size and 1)
        }

        if (dataStart < 0 || sampleRate <= 0 || channels <= 0) return null
        val bytesPerSample = bits / 8
        if (bytesPerSample !in 1..4) return null
        val isFloat = format == 3
        if (format != 1 && !isFloat) return null
        if (isFloat && bits != 32) return null

        val frameSize = bytesPerSample * channels
        val frames = dataLen / frameSize
        if (frames <= 0) return null

        val out = FloatArray(frames)
        for (f in 0 until frames) {
            var sum = 0f
            for (c in 0 until channels) {
                sum += sampleAt(bytes, dataStart + f * frameSize + c * bytesPerSample, bits, isFloat)
            }
            out[f] = sum / channels
        }
        return Pcm(out, sampleRate)
    }

    private fun sampleAt(b: ByteArray, o: Int, bits: Int, isFloat: Boolean): Float = when {
        isFloat -> java.lang.Float.intBitsToFloat(le32(b, o))
        bits == 8 -> ((b[o].toInt() and 0xFF) - 128) / 128f
        bits == 16 -> ((b[o].toInt() and 0xFF) or (b[o + 1].toInt() shl 8)).toShort() / 32768f
        bits == 24 -> (((b[o].toInt() and 0xFF) or ((b[o + 1].toInt() and 0xFF) shl 8) or (b[o + 2].toInt() shl 16))) / 8388608f
        else -> le32(b, o) / 2147483648f
    }

    private fun hasTag(b: ByteArray, o: Int, tag: String): Boolean {
        if (o + 4 > b.size) return false
        for (i in 0 until 4) if (b[o + i].toInt().toChar() != tag[i]) return false
        return true
    }

    private fun le16(b: ByteArray, o: Int) = (b[o].toInt() and 0xFF) or ((b[o + 1].toInt() and 0xFF) shl 8)

    private fun le32(b: ByteArray, o: Int) =
        (b[o].toInt() and 0xFF) or ((b[o + 1].toInt() and 0xFF) shl 8) or
                ((b[o + 2].toInt() and 0xFF) shl 16) or ((b[o + 3].toInt() and 0xFF) shl 24)
}
