package com.example.phonics_ta_in.core.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TamilTextSegmenterTest {

    @Test
    fun testSingleLetterVowels() {
        val vowels = listOf("அ", "ஆ", "இ", "ஈ", "உ", "ஊ", "எ", "ஏ", "ஐ", "ஒ", "ஓ", "ஔ")
        for (vowel in vowels) {
            val graphemes = TamilTextSegmenter.segmentGraphemes(vowel)
            assertEquals(1, graphemes.size)
            assertEquals(vowel, graphemes[0].text)
            assertEquals(TamilLetterType.UYIR, graphemes[0].type)
            assertEquals(vowel, graphemes[0].vowelSign)
            assertNull(graphemes[0].baseConsonant)
        }
    }

    @Test
    fun testAytham() {
        val graphemes = TamilTextSegmenter.segmentGraphemes("ஃ")
        assertEquals(1, graphemes.size)
        assertEquals("ஃ", graphemes[0].text)
        assertEquals(TamilLetterType.AYTHAM, graphemes[0].type)
        assertEquals("ak", graphemes[0].transliteration)
    }

    @Test
    fun testConsonantsWithPulli() {
        val meiList = listOf("க்", "ங்", "ச்", "ஞ்", "ட்", "ண்", "த்", "ந்", "ப்", "ம்", "ய்", "ர்", "ல்", "வ்", "ழ்", "ள்", "ற்", "ன்")
        for (mei in meiList) {
            val graphemes = TamilTextSegmenter.segmentGraphemes(mei)
            assertEquals(1, graphemes.size)
            assertEquals(mei, graphemes[0].text)
            assertEquals(TamilLetterType.MEI, graphemes[0].type)
            assertEquals(mei, graphemes[0].baseConsonant)
            assertNull(graphemes[0].vowelSign)
        }
    }

    @Test
    fun testUyirmeiVowelSigns() {
        val kaSeries = mapOf(
            "க" to "அ",
            "கா" to "ஆ",
            "கி" to "இ",
            "கீ" to "ஈ",
            "கு" to "உ",
            "கூ" to "ஊ",
            "கெ" to "எ",
            "கே" to "ஏ",
            "கை" to "ஐ",
            "கொ" to "ஒ",
            "கோ" to "ஓ",
            "கௌ" to "ஔ"
        )

        for ((kaForm, expectedVowel) in kaSeries) {
            val graphemes = TamilTextSegmenter.segmentGraphemes(kaForm)
            assertEquals("Testing $kaForm", 1, graphemes.size)
            val g = graphemes[0]
            assertEquals(kaForm, g.text)
            assertEquals(TamilLetterType.UYIRMEI, g.type)
            assertEquals("க்", g.baseConsonant)
            assertEquals(expectedVowel, g.vowelSign)
        }
    }

    @Test
    fun testGranthaLetters() {
        val granthaList = listOf("ஜ்", "ஜ", "ஷ", "ஸ்", "ஹ")
        for (item in granthaList) {
            val graphemes = TamilTextSegmenter.segmentGraphemes(item)
            assertEquals(1, graphemes.size)
            assertEquals(TamilLetterType.GRANTHA, graphemes[0].type)
        }
    }

    @Test
    fun testWordGraphemeSegmentation() {
        val resultAmma = TamilTextSegmenter.segmentGraphemes("அம்மா")
        assertEquals(listOf("அ", "ம்", "மா"), resultAmma.map { it.text })

        val resultTamil = TamilTextSegmenter.segmentGraphemes("தமிழ்")
        assertEquals(listOf("த", "மி", "ழ்"), resultTamil.map { it.text })

        val resultPalli = TamilTextSegmenter.segmentGraphemes("பள்ளிக்கூடம்")
        assertEquals(listOf("ப", "ள்", "ளி", "க்", "கூ", "ட", "ம்"), resultPalli.map { it.text })

        val resultAytham = TamilTextSegmenter.segmentGraphemes("அஃது")
        assertEquals(listOf("அ", "ஃ", "து"), resultAytham.map { it.text })
    }

    @Test
    fun testSyllableSegmentation() {
        assertEquals(listOf("அம்", "மா"), TamilSyllableSegmenter.segmentSyllables("அம்மா"))
        assertEquals(listOf("த", "மிழ்"), TamilSyllableSegmenter.segmentSyllables("தமிழ்"))
        assertEquals(listOf("பள்", "ளிக்", "கூ", "டம்"), TamilSyllableSegmenter.segmentSyllables("பள்ளிக்கூடம்"))
        assertEquals(listOf("பந்", "து"), TamilSyllableSegmenter.segmentSyllables("பந்து"))
        assertEquals(listOf("கா", "கம்"), TamilSyllableSegmenter.segmentSyllables("காகம்"))
        assertEquals(listOf("அஃ", "து"), TamilSyllableSegmenter.segmentSyllables("அஃது"))
        assertEquals(listOf("கை"), TamilSyllableSegmenter.segmentSyllables("கை"))
    }

    @Test
    fun testTransliteration() {
        val amma = TamilSyllableSegmenter.processWord("அம்மா")
        assertEquals("ammaa", amma.transliteration)

        val tamil = TamilSyllableSegmenter.processWord("தமிழ்")
        assertEquals("thamizh", tamil.transliteration)

        val kaagam = TamilSyllableSegmenter.processWord("காகம்")
        assertEquals("kaakam", kaagam.transliteration)
    }
}
