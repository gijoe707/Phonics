package com.example.phonics_ta_in.feature.readaloud

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadAlongTextTest {

    private fun words(text: String) = ReadAlongText.toSentences(text).map { it.words }

    @Test
    fun splitsAtSentenceEnds_andKeepsThePunctuationOnTheWords() {
        val result = words("அம்மா கை தொட்டாள். காகம் தண்ணீர் குடித்தது!")
        assertEquals(
            listOf(listOf("அம்மா", "கை", "தொட்டாள்."), listOf("காகம்", "தண்ணீர்", "குடித்தது!")),
            result
        )
    }

    @Test
    fun everyLineIsItsOwnSentence() {
        assertEquals(
            listOf(listOf("ஆடு", "புல்", "தின்றது"), listOf("காகம்", "பறந்தது")),
            words("ஆடு புல் தின்றது\r\n\r\n  காகம் பறந்தது  \n")
        )
    }

    @Test
    fun aClosingQuoteDoesNotHideTheSentenceEnd() {
        assertEquals(2, words("அவன் \"வந்தான்.\" இவள் சென்றாள்").size)
    }

    @Test
    fun piecesWithNothingToReadAreDropped() {
        assertEquals(listOf(listOf("அம்மா")), words("...\n-\nஅம்மா"))
        assertTrue(words("   \n\n").isEmpty())
        assertTrue(words("").isEmpty())
    }

    @Test
    fun numbersStayInTheTextAsWords() {
        assertEquals(listOf(listOf("அம்மா", "10", "கை")), words("அம்மா 10 கை"))
    }

    @Test
    fun longSentencesAreCutIntoChunksAndNoWordIsLost() {
        val original = List(30) { "சொல்${it + 1}" }
        val chunks = words(original.joinToString(" "))
        assertTrue(chunks.size >= 3)
        assertTrue(chunks.all { it.size <= ReadAlongText.MAX_WORDS_PER_SENTENCE + 2 })
        assertEquals(original, chunks.flatten())
    }

    @Test
    fun aCommaAfterEnoughWordsIsAGoodPlaceToCut() {
        val text = (1..8).joinToString(" ") { "அ$it" } + ", " + (9..14).joinToString(" ") { "அ$it" }
        val chunks = words(text)
        assertEquals(2, chunks.size)
        assertEquals("அ8,", chunks[0].last())
    }

    @Test
    fun aTinyTailIsJoinedToTheChunkBeforeIt() {
        val chunks = words((1..13).joinToString(" ") { "சொல்$it" })
        assertEquals(1, chunks.size)
        assertEquals(13, chunks[0].size)
    }

    @Test
    fun sentencesGetSequentialUniqueIds() {
        val ids = ReadAlongText.toSentences("ஒன்று. இரண்டு. மூன்று.").map { it.id }
        assertEquals(listOf("custom_1", "custom_2", "custom_3"), ids)
    }

    @Test
    fun sentenceTextIsTheWordsJoined() {
        val s = ReadAlongText.toSentences("அம்மா  கை").single()
        assertEquals("அம்மா கை", s.text)
        assertEquals("", s.englishTranslation)
    }
}
