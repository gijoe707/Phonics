package com.example.phonics_ta_in.nav

import com.example.phonics_ta_in.AppScreen
import com.example.phonics_ta_in.ui.nav.AppMenu
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppMenuTest {

    @Test
    fun everyJumpableScreenIsInTheMenu() {
        // A lesson needs a letter chosen first, and a consonant's page is reached via Consonants
        val notDirectlyReachable = setOf(AppScreen.LESSON, AppScreen.CONSONANT_FAMILY)
        val inMenu = AppMenu.allDestinations.map { it.screen }.toSet()
        val missing = AppScreen.values().filter { it !in notDirectlyReachable && it !in inMenu }
        assertTrue("Screens missing from the menu: $missing", missing.isEmpty())
    }

    @Test
    fun noScreenAppearsTwice() {
        val screens = AppMenu.allDestinations.map { it.screen }
        assertEquals(screens.size, screens.toSet().size)
    }

    @Test
    fun sectionIdsAreUnique() {
        val ids = AppMenu.sections.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun consonantPageHighlightsConsonantsInLettersSection() {
        assertEquals(AppScreen.CONSONANTS, AppMenu.highlightFor(AppScreen.CONSONANT_FAMILY))
        assertEquals("letters", AppMenu.sectionIdFor(AppScreen.CONSONANT_FAMILY))
    }

    @Test
    fun homeAndLessonsHaveNoSection() {
        assertNull(AppMenu.sectionIdFor(AppScreen.LEARNING_PATH))
        assertNull(AppMenu.sectionIdFor(AppScreen.LESSON))
        assertNull(AppMenu.highlightFor(AppScreen.LESSON))
    }

    @Test
    fun licensesLiveUnderParentsAndInfo() {
        assertEquals("parents", AppMenu.sectionIdFor(AppScreen.LICENSES))
    }
}
