package com.example.phonics_ta_in.core.speech

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.ByteArrayOutputStream

class WavDecoderTest {

    private fun le16(out: ByteArrayOutputStream, v: Int) {
        out.write(v and 0xFF); out.write((v shr 8) and 0xFF)
    }

    private fun le32(out: ByteArrayOutputStream, v: Int) {
        le16(out, v and 0xFFFF); le16(out, (v shr 16) and 0xFFFF)
    }

    private fun wav(
        samples: ShortArray,
        rate: Int = 22_050,
        channels: Int = 1,
        declaredDataSize: Int? = null
    ): ByteArray {
        val dataBytes = samples.size * 2
        val out = ByteArrayOutputStream()
        out.write("RIFF".toByteArray()); le32(out, 36 + dataBytes); out.write("WAVE".toByteArray())
        out.write("fmt ".toByteArray()); le32(out, 16)
        le16(out, 1); le16(out, channels); le32(out, rate); le32(out, rate * channels * 2); le16(out, channels * 2); le16(out, 16)
        out.write("data".toByteArray()); le32(out, declaredDataSize ?: dataBytes)
        for (s in samples) le16(out, s.toInt())
        return out.toByteArray()
    }

    @Test
    fun decodesMono16BitPcm() {
        val pcm = WavDecoder.decode(wav(shortArrayOf(0, 16384, -16384, 32767), rate = 24_000))
        assertNotNull(pcm)
        assertEquals(24_000, pcm!!.sampleRate)
        assertEquals(4, pcm.samples.size)
        assertEquals(0f, pcm.samples[0], 1e-6f)
        assertEquals(0.5f, pcm.samples[1], 1e-4f)
        assertEquals(-0.5f, pcm.samples[2], 1e-4f)
    }

    @Test
    fun mixesStereoDownToMono() {
        val pcm = WavDecoder.decode(wav(shortArrayOf(16384, -16384, 8192, 8192), channels = 2))!!
        assertEquals(2, pcm.samples.size)
        assertEquals(0f, pcm.samples[0], 1e-4f)
        assertEquals(0.25f, pcm.samples[1], 1e-4f)
    }

    @Test
    fun streamingEnginesLeaveTheDataSizeUnset_theRestOfTheFileIsAudio() {
        val zero = WavDecoder.decode(wav(shortArrayOf(1000, 2000, 3000), declaredDataSize = 0))!!
        assertEquals(3, zero.samples.size)
        val unknown = WavDecoder.decode(wav(shortArrayOf(1000, 2000, 3000), declaredDataSize = -1))!!
        assertEquals(3, unknown.samples.size)
    }

    @Test
    fun rejectsGarbage() {
        assertNull(WavDecoder.decode(ByteArray(0)))
        assertNull(WavDecoder.decode("this is not a wav file at all".toByteArray()))
    }
}
