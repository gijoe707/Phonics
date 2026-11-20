package com.example.phonics_ta_in.core.speech

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WordMatchingTest {

    @Test
    fun keyKeepsOnlyLetters() {
        assertEquals("அம்மா", WordMatching.key("அம்மா,"))
        assertEquals("hello", WordMatching.key(" Hello! "))
        assertEquals("", WordMatching.key("10"))
        assertEquals("", WordMatching.key("..."))
        assertEquals("", WordMatching.key("-"))
    }

    @Test
    fun tokenizeSplitsOnSpacesAndPunctuation() {
        assertEquals(listOf("அம்மா", "கை", "தொட்டாள்"), WordMatching.tokenize("அம்மா, கை  தொட்டாள்."))
        assertEquals(listOf("அம்மா", "கை"), WordMatching.tokenize("அம்மா-கை"))
        assertTrue(WordMatching.tokenize("").isEmpty())
        assertTrue(WordMatching.tokenize("...").isEmpty())
    }

    @Test
    fun similarityCountsTamilLettersNotCharacters() {
        assertEquals(1f, WordMatching.similarity("அம்மா", "அம்மா"), 1e-6f)
        // தொட்டா = 3 letters, தொட்டாள் = 4 letters: one letter missing
        assertEquals(0.75f, WordMatching.similarity("தொட்டா", "தொட்டாள்"), 1e-6f)
        assertEquals(0f, WordMatching.similarity("பூனை", "கை"), 1e-6f)
        assertEquals(0f, WordMatching.similarity("", "கை"), 1e-6f)
    }

    @Test
    fun isMatchIsLenientForLongWordsAndStrictForShortOnes() {
        assertTrue(WordMatching.isMatch("தொட்டா", "தொட்டாள்"))
        assertTrue(WordMatching.isMatch("அம்மா,", "அம்மா"))
        assertFalse(WordMatching.isMatch("அப்பா", "அம்மா"))
        assertFalse(WordMatching.isMatch("கா", "கை"))
        assertFalse(WordMatching.isMatch("", "கை"))
        assertFalse(WordMatching.isMatch("கை", "-"))
    }

    @Test
    fun isExactNeedsTheSameLetters() {
        assertTrue(WordMatching.isExact("அம்மா.", "அம்மா"))
        assertFalse(WordMatching.isExact("தொட்டா", "தொட்டாள்"))
        assertFalse(WordMatching.isExact("", ""))
    }
}
