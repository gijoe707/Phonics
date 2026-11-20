package com.example.phonics_ta_in.feature.readaloud

import com.example.phonics_ta_in.feature.readaloud.ReadAlongEvent.Finished
import com.example.phonics_ta_in.feature.readaloud.ReadAlongEvent.WordCorrect
import com.example.phonics_ta_in.feature.readaloud.ReadAlongEvent.WordWrong
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadAlongTrackerTest {

    private val sentence = listOf("அம்மா", "கை", "தொட்டாள்")

    private fun tracker(words: List<String> = sentence) = ReadAlongTracker(words)

    @Test
    fun wordsTurnGreenOneByOneAsTheyAreSpoken() {
        val t = tracker()
        assertEquals(listOf(WordCorrect(0)), t.accept(listOf("அம்மா"), lastWordComplete = false))
        assertEquals(listOf(WordCorrect(1)), t.accept(listOf("அம்மா கை"), lastWordComplete = false))
        assertEquals(listOf(WordCorrect(2), Finished), t.accept(listOf("அம்மா கை தொட்டாள்"), lastWordComplete = true))
        assertEquals(List(3) { WordStatus.CORRECT }, t.statuses)
        assertTrue(t.isFinished)
    }

    @Test
    fun theLastWordIsNotJudgedWhileItMayStillBeHalfSpoken() {
        val t = tracker()
        // "தொட்டா" is only close to "தொட்டாள்": not enough while the child may still be saying it ...
        assertEquals(
            listOf(WordCorrect(0), WordCorrect(1)),
            t.accept(listOf("அம்மா கை தொட்டா"), lastWordComplete = false)
        )
        assertEquals(WordStatus.PENDING, t.statuses[2])
        // ... but good enough once the recognizer is done with it
        assertEquals(listOf(WordCorrect(2), Finished), t.accept(listOf("அம்மா கை தொட்டா"), lastWordComplete = true))
    }

    @Test
    fun aWrongWordIsYellow_andEverythingSaidAfterItIsIgnored() {
        val t = tracker()
        // while "பூனை" is still the last word it may be unfinished: no verdict yet
        assertEquals(listOf(WordCorrect(0)), t.accept(listOf("அம்மா பூனை"), lastWordComplete = false))
        assertEquals(WordStatus.PENDING, t.statuses[1])

        // the child went on to the next word, so "பூனை" was a whole word and it was not "கை"
        assertEquals(
            listOf(WordWrong(1, "பூனை")),
            t.accept(listOf("அம்மா பூனை தொட்டாள்"), lastWordComplete = false)
        )
        assertEquals(WordStatus.WRONG, t.statuses[1])
        assertEquals(WordStatus.PENDING, t.statuses[2])   // the word spoken after the mistake does not count
        assertTrue(t.isBlocked)
        assertEquals(1, t.position)

        // more speech from the same transcript is ignored
        assertTrue(t.accept(listOf("அம்மா பூனை தொட்டாள் கை"), lastWordComplete = true).isEmpty())
        assertEquals(WordStatus.PENDING, t.statuses[2])
    }

    @Test
    fun afterTheHelperTheSameWordIsRetried_andYellowTurnsGreen() {
        val t = tracker()
        t.accept(listOf("அம்மா பூனை தொட்டாள்"), lastWordComplete = false)
        assertEquals(WordStatus.WRONG, t.statuses[1])

        t.startTranscript()   // the microphone was opened again
        assertFalse(t.isBlocked)
        assertEquals(listOf(WordCorrect(1)), t.accept(listOf("கை"), lastWordComplete = false))
        assertEquals(WordStatus.CORRECT, t.statuses[1])
        assertEquals(listOf(WordCorrect(2), Finished), t.accept(listOf("கை தொட்டாள்"), lastWordComplete = true))
    }

    @Test
    fun aFinishedResultMayBeWrongWithoutAFollowingWord() {
        val t = tracker()
        assertTrue(t.accept(listOf("பூனை"), lastWordComplete = false).isEmpty())
        assertEquals(listOf(WordWrong(0, "பூனை")), t.accept(listOf("பூனை"), lastWordComplete = true))
    }

    @Test
    fun aSimilarShortWordIsNotAccepted() {
        val t = tracker()
        t.accept(listOf("அம்மா"), lastWordComplete = false)
        assertEquals(listOf(WordWrong(1, "கா")), t.accept(listOf("அம்மா கா"), lastWordComplete = true))
    }

    @Test
    fun alternativesOfAFinishedResultAreAllConsidered() {
        val t = tracker(listOf("அம்மா", "கை"))
        val events = t.accept(listOf("அம்மா பூனை", "அம்மா கை"), lastWordComplete = true)
        assertEquals(listOf(WordCorrect(0), WordCorrect(1), Finished), events)
    }

    @Test
    fun aPartialResultIsJudgedOnItsBestGuessOnly() {
        val t = tracker(listOf("அம்மா", "கை"))
        // the second alternative would be right, but partial alternatives are not trusted
        val events = t.accept(listOf("அம்மா பூனை தொட்டாள்", "அம்மா கை"), lastWordComplete = false)
        assertEquals(listOf(WordCorrect(0), WordWrong(1, "பூனை")), events)
    }

    @Test
    fun wordsGluedTogetherByTheRecognizerCountAsBoth() {
        val t = tracker(listOf("அம்மா", "கை"))
        assertEquals(listOf(WordCorrect(0), WordCorrect(1), Finished), t.accept(listOf("அம்மாகை"), lastWordComplete = true))
    }

    @Test
    fun aPlainWordIsNotMistakenForTwoGluedWords() {
        val t = tracker(listOf("அம்மா", "கை"))
        // "அம்மா" is 75 % similar to the glued "அம்மாகை", but 100 % to the first word
        assertEquals(listOf(WordCorrect(0)), t.accept(listOf("அம்மா"), lastWordComplete = true))
        assertEquals(WordStatus.PENDING, t.statuses[1])
    }

    @Test
    fun aWordSplitByTheRecognizerCountsAsOneWord() {
        val t = tracker(listOf("தண்ணீர்", "குடித்தது"))
        val events = t.accept(listOf("தண் ணீர் குடித்தது"), lastWordComplete = true)
        assertEquals(listOf(WordCorrect(0), WordCorrect(1), Finished), events)
    }

    @Test
    fun sayingThePreviousWordAgainIsNotAMistake() {
        val t = tracker()
        val events = t.accept(listOf("அம்மா அம்மா கை"), lastWordComplete = true)
        assertEquals(listOf(WordCorrect(0), WordCorrect(1)), events)
        assertFalse(t.isBlocked)
    }

    @Test
    fun numbersAndDashesAreSkippedAutomatically() {
        val t = tracker(listOf("அம்மா", "10", "கை", "-"))
        val events = t.accept(listOf("அம்மா கை"), lastWordComplete = true)
        assertEquals(listOf(WordCorrect(0), WordCorrect(2), Finished), events)
        assertEquals(
            listOf(WordStatus.CORRECT, WordStatus.SKIPPED, WordStatus.CORRECT, WordStatus.SKIPPED),
            t.statuses
        )
    }

    @Test
    fun leadingUnreadableWordsAreSkippedRightAway() {
        val t = tracker(listOf("1.", "அம்மா"))
        assertEquals(1, t.position)
        assertEquals(WordStatus.SKIPPED, t.statuses[0])
    }

    @Test
    fun aTranscriptThatGetsShorterIsNotHeldAgainstTheChild() {
        val t = tracker()
        t.accept(listOf("அம்மா கை"), lastWordComplete = false)
        // the recognizer re-wrote its guess as one word: nothing new to judge, nothing to undo
        assertTrue(t.accept(listOf("அம்மாகை"), lastWordComplete = false).isEmpty())
        assertEquals(WordStatus.CORRECT, t.statuses[0])
        assertEquals(WordStatus.CORRECT, t.statuses[1])
    }

    @Test
    fun aNewTranscriptStartsCountingFromItsFirstWord() {
        val t = tracker()
        t.accept(listOf("அம்மா"), lastWordComplete = false)
        t.startTranscript()
        assertEquals(listOf(WordCorrect(1)), t.accept(listOf("கை"), lastWordComplete = false))
    }

    @Test
    fun markCurrentReadAcceptsTheWordWithoutListening() {
        val t = tracker(listOf("அம்மா", "கை"))
        assertEquals(listOf(WordCorrect(0)), t.markCurrentRead())
        assertEquals(listOf(WordCorrect(1), Finished), t.markCurrentRead())
        assertTrue(t.markCurrentRead().isEmpty())
    }

    @Test
    fun markCurrentReadDoesNotLetOldSpeechBeCountedForTheNextWord() {
        val t = tracker(listOf("அம்மா", "கை"))
        t.accept(listOf("பூனை"), lastWordComplete = false)   // still unsure: not judged
        t.markCurrentRead()
        // "பூனை" was said before the button was pressed: it must not be held against "கை"
        assertEquals(listOf(WordCorrect(1), Finished), t.accept(listOf("பூனை கை"), lastWordComplete = true))
    }

    @Test
    fun nothingIsProcessedAfterTheLastWord() {
        val t = tracker(listOf("கை"))
        t.accept(listOf("கை"), lastWordComplete = true)
        assertTrue(t.isFinished)
        assertTrue(t.accept(listOf("கை பூனை"), lastWordComplete = true).isEmpty())
    }

    @Test
    fun resetStartsOver() {
        val t = tracker()
        t.accept(listOf("அம்மா பூனை தொட்டாள்"), lastWordComplete = false)
        t.reset()
        assertEquals(0, t.position)
        assertFalse(t.isBlocked)
        assertEquals(List(3) { WordStatus.PENDING }, t.statuses)
        assertEquals(listOf(WordCorrect(0)), t.accept(listOf("அம்மா"), lastWordComplete = false))
    }

    @Test
    fun emptyOrSilentInputDoesNothing() {
        val t = tracker()
        assertTrue(t.accept(emptyList(), lastWordComplete = true).isEmpty())
        assertTrue(t.accept(listOf(""), lastWordComplete = true).isEmpty())
        assertTrue(t.accept(listOf("..."), lastWordComplete = true).isEmpty())
        assertEquals(0, t.position)
    }
}
