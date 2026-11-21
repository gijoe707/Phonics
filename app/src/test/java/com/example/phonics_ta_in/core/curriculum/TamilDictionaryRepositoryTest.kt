package com.example.phonics_ta_in.core.curriculum

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TamilDictionaryRepositoryTest {

    @Test
    fun testWordGraphemeCountAndLevelClassification() {
        val wordAa = DictionaryWord("ஆ", "பசு", "Cow", "aa")
        assertEquals(1, wordAa.graphemeCount)
        assertEquals(1, wordAa.difficultyLevel)

        val wordKan = DictionaryWord("கண்", "விழி", "Eye", "kan")
        assertEquals(2, wordKan.graphemeCount)
        assertEquals(2, wordKan.difficultyLevel)

        val wordAmmaa = DictionaryWord("அம்மா", "தாய்", "Mother", "ammaa")
        assertEquals(3, wordAmmaa.graphemeCount)
        assertEquals(3, wordAmmaa.difficultyLevel)

        val wordKappal = DictionaryWord("கப்பல்", "கலம்", "Ship", "kappal")
        assertEquals(4, wordKappal.graphemeCount)
        assertEquals(4, wordKappal.difficultyLevel)

        val wordAalamaram = DictionaryWord("ஆலமரம்", "மரம்", "Banyan Tree", "aalamaram")
        assertEquals(5, wordAalamaram.graphemeCount)
        assertEquals(5, wordAalamaram.difficultyLevel)

        val wordTholaikkaatchi = DictionaryWord("தொலைக்காட்சி", "டிவி", "Television", "tholaikkaatchi")
        assertEquals(6, wordTholaikkaatchi.graphemeCount)
        assertEquals(6, wordTholaikkaatchi.difficultyLevel)

        val wordNatchaththiram = DictionaryWord("நட்சத்திரம்", "விண்மீன்", "Star", "natchaththiram")
        assertEquals(7, wordNatchaththiram.graphemeCount)
        assertEquals(7, wordNatchaththiram.difficultyLevel)
    }

    @Test
    fun testGetWordsByLevel() {
        val level1Words = TamilDictionaryRepository.getWordsByLevel(1)
        assertTrue(level1Words.isNotEmpty())
        assertTrue(level1Words.all { it.difficultyLevel == 1 })

        val level2Words = TamilDictionaryRepository.getWordsByLevel(2)
        assertTrue(level2Words.isNotEmpty())
        assertTrue(level2Words.all { it.difficultyLevel == 2 })

        val level3Words = TamilDictionaryRepository.getWordsByLevel(3)
        assertTrue(level3Words.isNotEmpty())
        assertTrue(level3Words.all { it.difficultyLevel == 3 })
    }

    @Test
    fun testGetDistractorsForWord() {
        val target = TamilDictionaryRepository.ALL_WORDS.first()
        val distractors = TamilDictionaryRepository.getDistractorsForWord(target, 3)

        assertEquals(3, distractors.size)
        assertFalse(distractors.any { it.wordTamil == target.wordTamil })
    }

    @Test
    fun testGenerateRandomTestSet() {
        val testSet = TamilDictionaryRepository.generateRandomTestSet(wordsPerLevel = 2)
        // 7 difficulty levels * 2 words per level = 14 words
        assertEquals(14, testSet.size)

        // Verify progression contains words from levels 1 through 7
        val levelsPresent = testSet.map { it.difficultyLevel }.toSet()
        assertTrue(levelsPresent.containsAll(listOf(1, 2, 3, 4, 5, 6, 7)))
    }
}
