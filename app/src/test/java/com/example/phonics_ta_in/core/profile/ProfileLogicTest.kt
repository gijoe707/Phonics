package com.example.phonics_ta_in.core.profile

import com.example.phonics_ta_in.core.profile.ProfileLogic.NameError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileLogicTest {

    private val anbu = ChildProfile("id-1", "அன்பு", "🦁")
    private val mia = ChildProfile("id-2", "Mia", "🐼")

    // --- Names ---

    @Test
    fun blankNameIsRejected() {
        assertEquals(NameError.EMPTY, ProfileLogic.validateName("", emptyList()))
        assertEquals(NameError.EMPTY, ProfileLogic.validateName("   ", emptyList()))
    }

    @Test
    fun overlongNameIsRejected() {
        val tooLong = "a".repeat(ProfileLogic.MAX_NAME_LENGTH + 1)
        assertEquals(NameError.TOO_LONG, ProfileLogic.validateName(tooLong, emptyList()))
        assertNull(ProfileLogic.validateName("a".repeat(ProfileLogic.MAX_NAME_LENGTH), emptyList()))
    }

    @Test
    fun duplicateNamesAreRejectedIgnoringCaseAndSpaces() {
        assertEquals(NameError.DUPLICATE, ProfileLogic.validateName("mia", listOf(anbu, mia)))
        assertEquals(NameError.DUPLICATE, ProfileLogic.validateName("  MIA ", listOf(anbu, mia)))
        assertNull(ProfileLogic.validateName("Ravi", listOf(anbu, mia)))
    }

    @Test
    fun aProfileCanKeepItsOwnNameWhileBeingEdited() {
        assertNull(ProfileLogic.validateName("Mia", listOf(anbu, mia), ignoreId = mia.id))
        assertEquals(NameError.DUPLICATE, ProfileLogic.validateName("Mia", listOf(anbu, mia), ignoreId = anbu.id))
    }

    // --- Creating ---

    @Test
    fun newProfilesGetUniqueIdsAndTrimmedNames() {
        val a = ProfileLogic.newProfile("  Ravi ", "🐯")
        val b = ProfileLogic.newProfile("Ravi", "🐯")
        assertEquals("Ravi", a.name)
        assertNotEquals(a.id, b.id)
    }

    // --- Launch behaviour ---

    @Test
    fun launchSkipsThePickerOnlyForExactlyOneProfile() {
        assertNull(ProfileLogic.autoSelectId(emptyList()))
        assertEquals(anbu.id, ProfileLogic.autoSelectId(listOf(anbu)))
        assertNull(ProfileLogic.autoSelectId(listOf(anbu, mia)))
    }

    // --- Storage ---

    @Test
    fun profilesRoundTripThroughStorage() {
        val tricky = ChildProfile("id-3", "Sam, Jr: the \"2nd\" + more", "🦜")
        val profiles = listOf(anbu, mia, tricky)
        assertEquals(profiles, ProfileLogic.decode(ProfileLogic.encode(profiles)))
    }

    @Test
    fun emptyOrDamagedStorageGivesNoProfiles() {
        assertTrue(ProfileLogic.decode("").isEmpty())
        assertTrue(ProfileLogic.decode("junk,a:b,%%%:%%%:%%%").isEmpty())
    }

    @Test
    fun oneDamagedRecordDoesNotHideTheGoodOnes() {
        val good = ProfileLogic.encode(listOf(mia))
        assertEquals(listOf(mia), ProfileLogic.decode("junk,$good"))
    }
}
