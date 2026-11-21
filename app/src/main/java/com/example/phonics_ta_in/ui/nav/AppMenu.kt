package com.example.phonics_ta_in.ui.nav

import com.example.phonics_ta_in.AppScreen

/** One place the menu can jump to. */
data class MenuDestination(
    val screen: AppScreen,
    val titleTamil: String,
    val titleEnglish: String
)

/** An accordion group in the hamburger menu. */
data class MenuSection(
    val id: String,
    val icon: String,
    val titleTamil: String,
    val titleEnglish: String,
    val items: List<MenuDestination>
)

/** The menu structure. Add new screens here so they are reachable from every page. */
object AppMenu {
    val home = MenuDestination(AppScreen.LEARNING_PATH, "முகப்பு", "Home")

    val sections: List<MenuSection> = listOf(
        MenuSection(
            id = "letters", icon = "🔤", titleTamil = "எழுத்துகள்", titleEnglish = "Letters",
            items = listOf(
                MenuDestination(AppScreen.VOWELS, "உயிர் எழுத்துகள்", "Vowels"),
                MenuDestination(AppScreen.CONSONANTS, "மெய் எழுத்துகள்", "Consonants")
            )
        ),
        MenuSection(
            id = "practice", icon = "🎯", titleTamil = "பயிற்சி", titleEnglish = "Practice",
            items = listOf(
                MenuDestination(AppScreen.VOCABULARY_SPEECH, "வார்த்தை உச்சரிப்பு", "Vocabulary & speech"),
                MenuDestination(AppScreen.TEST_KNOWLEDGE, "அறிவு சோதனை", "Test your knowledge")
            )
        ),
        MenuSection(
            id = "reading", icon = "📖", titleTamil = "வாசிப்பு", titleEnglish = "Reading",
            items = listOf(
                MenuDestination(AppScreen.READ_ALOUD, "கதை வாசித்தல்", "Stories read-aloud"),
                MenuDestination(AppScreen.CUSTOM_READER, "உரை வாசிப்பு", "Read your own text")
            )
        ),
        MenuSection(
            id = "parents", icon = "🛡️", titleTamil = "பெற்றோர் மற்றும் தகவல்", titleEnglish = "Parents & info",
            items = listOf(
                MenuDestination(AppScreen.PARENT_DASHBOARD, "பெற்றோர் பகுதி", "Parent dashboard"),
                MenuDestination(AppScreen.PROFILE_SELECT, "சுயவிவரத்தை மாற்று", "Switch profile"),
                MenuDestination(AppScreen.LICENSES, "உரிமங்கள்", "Licenses")
            )
        )
    )

    /** Every screen the menu can open. */
    val allDestinations: List<MenuDestination> = listOf(home) + sections.flatMap { it.items }

    /** The menu entry to highlight while [screen] is showing (a consonant's page counts as Consonants). */
    fun highlightFor(screen: AppScreen): AppScreen? = when (screen) {
        AppScreen.CONSONANT_FAMILY -> AppScreen.CONSONANTS
        AppScreen.LESSON -> null
        else -> screen
    }

    /** The accordion section that holds [screen], or null for Home and for lessons. */
    fun sectionIdFor(screen: AppScreen): String? {
        val target = highlightFor(screen) ?: return null
        return sections.firstOrNull { section -> section.items.any { it.screen == target } }?.id
    }
}
