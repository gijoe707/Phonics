package com.example.phonics_ta_in

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.phonics_ta_in.core.audio.BackgroundMusicPlayer
import com.example.phonics_ta_in.core.curriculum.LessonCategories
import com.example.phonics_ta_in.core.profile.ProfileLogic
import com.example.phonics_ta_in.core.profile.ProfileStore
import com.example.phonics_ta_in.ui.nav.AppDrawerContent
import com.example.phonics_ta_in.ui.nav.AppTopBar
import kotlinx.coroutines.launch
import com.example.phonics_ta_in.core.progress.ProgressLogic
import com.example.phonics_ta_in.core.progress.ProgressState
import com.example.phonics_ta_in.core.progress.ProgressStore
import com.example.phonics_ta_in.feature.curriculum.ConsonantFamilyScreen
import com.example.phonics_ta_in.feature.curriculum.ConsonantsScreen
import com.example.phonics_ta_in.feature.curriculum.LearningPathScreen
import com.example.phonics_ta_in.feature.curriculum.LessonScreen
import com.example.phonics_ta_in.feature.curriculum.VocabularySpeechScreen
import com.example.phonics_ta_in.feature.curriculum.VowelsScreen
import com.example.phonics_ta_in.feature.curriculum.TestYourKnowledgeScreen
import com.example.phonics_ta_in.feature.licenses.LicensesScreen
import com.example.phonics_ta_in.feature.parent.ParentDashboardScreen
import com.example.phonics_ta_in.feature.profile.ProfileSelectScreen
import com.example.phonics_ta_in.feature.readaloud.CustomTextReaderScreen
import com.example.phonics_ta_in.feature.readaloud.ReadAloudScreen
import com.example.phonics_ta_in.ui.theme.PhonicstaINTheme

enum class AppScreen {
    PROFILE_SELECT,     // "who is learning?" - shown at launch when there are 0 or 2+ profiles
    LEARNING_PATH,
    VOWELS,             // grid of 12 vowels + ஃ
    CONSONANTS,         // grid of 18 consonants
    CONSONANT_FAMILY,   // one consonant expanded: க், க, கா, கி ...
    LESSON,             // exercise set for ONE letter
    READ_ALOUD,
    CUSTOM_READER,      // paste your own text and read along
    VOCABULARY_SPEECH,
    TEST_KNOWLEDGE,     // test speech + meaning based on 1 to 7+ letter word levels
    PARENT_DASHBOARD,   // parent/teacher analytics dashboard, profile management
    LICENSES            // third-party licenses & credits (music)
}

class MainActivity : ComponentActivity() {
    private lateinit var backgroundMusic: BackgroundMusicPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        backgroundMusic = BackgroundMusicPlayer(this)
        setContent {
            PhonicstaINTheme {
                TamilPhonicsApp(backgroundMusic = backgroundMusic)
            }
        }
    }

    // Music only plays while the app is on screen
    override fun onStart() {
        super.onStart()
        backgroundMusic.setAppVisible(true)
    }

    override fun onStop() {
        backgroundMusic.setAppVisible(false)
        super.onStop()
    }

    override fun onDestroy() {
        backgroundMusic.release()
        super.onDestroy()
    }
}

@Composable
fun TamilPhonicsApp(backgroundMusic: BackgroundMusicPlayer? = null) {
    val context = LocalContext.current

    // --- Profiles: every child has their own stars, streak and progress ---
    val profileStore = remember { ProfileStore(context) }
    var profiles by remember { mutableStateOf(profileStore.loadProfiles()) }
    // Who is learning. Survives rotation, but NOT a fresh launch: with 2+ profiles the app asks every time.
    // Exactly one profile is entered straight away; none means the picker invites the parent to create one.
    var activeProfileId by rememberSaveable { mutableStateOf<String?>(ProfileLogic.autoSelectId(profiles)) }
    val activeProfile = profiles.firstOrNull { it.id == activeProfileId }

    // Navigation state survives rotation; the lesson itself still restarts.
    var currentScreen by rememberSaveable {
        mutableStateOf(if (activeProfileId == null) AppScreen.PROFILE_SELECT else AppScreen.LEARNING_PATH)
    }
    var activeUnitId by rememberSaveable { mutableStateOf<String?>(null) }
    var activeFamilyIndex by rememberSaveable { mutableStateOf(1) }
    // Where "close lesson" / back from a lesson returns to (the vowels page or a consonant's page)
    var lessonReturnScreen by rememberSaveable { mutableStateOf(AppScreen.LEARNING_PATH) }

    // Persisted state for Custom Text Reader & Read Aloud practice
    var customDraftText by rememberSaveable { mutableStateOf("") }
    var customReadingText by rememberSaveable { mutableStateOf<String?>(null) }
    var customReadingPosition by rememberSaveable { mutableIntStateOf(0) }
    var storyReadingPosition by rememberSaveable { mutableIntStateOf(0) }

    // The child's mute button on the main page. Not saved, so music is back on at every new launch.
    // (The parent's on/off setting is saved separately in the ProfileStore and applies to every profile.)
    var sessionMuted by rememberSaveable { mutableStateOf(false) }
    var musicEnabled by remember { mutableStateOf(profileStore.loadMusicEnabled()) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Real progress of the ACTIVE profile, saved on the device. A new profile starts at zero
    // (no XP, no streak, no ticks). Switching profile loads that child's own file.
    val progressStore = remember(activeProfileId) { activeProfileId?.let { ProgressStore(context, it) } }
    var progress by remember(activeProfileId) { mutableStateOf(progressStore?.load() ?: ProgressState()) }
    fun commit(updated: ProgressState) {
        progress = updated
        progressStore?.save(updated)
    }

    // --- Profile actions ---
    fun selectProfile(id: String) {
        if (id != activeProfileId) {
            // Typed text, reading positions and the open lesson belong to the previous child
            customDraftText = ""
            customReadingText = null
            customReadingPosition = 0
            storyReadingPosition = 0
            activeUnitId = null
            lessonReturnScreen = AppScreen.LEARNING_PATH
        }
        activeProfileId = id
        currentScreen = AppScreen.LEARNING_PATH
    }

    fun createProfile(name: String, avatar: String, openIt: Boolean) {
        val created = profileStore.createProfile(name, avatar)
        profiles = profileStore.loadProfiles()
        if (openIt) selectProfile(created.id)
    }

    fun editProfile(id: String, name: String, avatar: String) {
        profileStore.updateProfile(id, name, avatar)
        profiles = profileStore.loadProfiles()
    }

    fun deleteProfile(id: String) {
        profileStore.deleteProfile(id)
        profiles = profileStore.loadProfiles()
        if (id == activeProfileId) {
            val onlyOne = ProfileLogic.autoSelectId(profiles)
            if (onlyOne != null) {
                selectProfile(onlyOne)
            } else {
                activeProfileId = null
                currentScreen = AppScreen.PROFILE_SELECT
            }
        }
    }

    // Safety net: without an active profile the only place to be is the picker
    LaunchedEffect(activeProfile, currentScreen) {
        if (activeProfile == null && currentScreen != AppScreen.PROFILE_SELECT) {
            currentScreen = AppScreen.PROFILE_SELECT
        }
    }

    val today = ProgressLogic.today()
    val totalStars = progress.totalStars
    val streakDays = ProgressLogic.displayStreak(progress, today)
    val dailyGoalMinutes = progress.dailyGoalMinutes
    val minutesLearnedToday = ProgressLogic.minutesToday(progress, today)
    val weeklyMinutes = ProgressLogic.weeklyMinutes(progress, today)
    val completedUnitIds = progress.completedUnitIds
    val genuineCompletedUnitIds = progress.genuineCompletedUnitIds
    // A section gets its tick on the main screens once every letter inside it is finished
    val vowelsCompleted = remember(genuineCompletedUnitIds) {
        ProgressLogic.vowelsComplete(genuineCompletedUnitIds)
    }
    val completedFamilyIndexes = remember(genuineCompletedUnitIds) {
        ProgressLogic.completedFamilyIndexes(genuineCompletedUnitIds)
    }
    val consonantsCompleted = remember(genuineCompletedUnitIds) {
        ProgressLogic.consonantsComplete(genuineCompletedUnitIds)
    }

    // Real time spent in lessons: counted from opening a lesson until leaving it
    // The timer's onDispose runs long after it was set up, so it must read the LATEST progress,
    // otherwise it would overwrite stars saved in the meantime with an old copy.
    val latestProgress by rememberUpdatedState(progress)
    val screenForTimer = currentScreen
    val unitForTimer = activeUnitId // Next / Random switch lessons without leaving the lesson screen
    DisposableEffect(screenForTimer, unitForTimer) {
        val startedAt = SystemClock.elapsedRealtime()
        onDispose {
            if (screenForTimer == AppScreen.LESSON) {
                val seconds = ((SystemClock.elapsedRealtime() - startedAt) / 1000).toInt()
                commit(ProgressLogic.addStudySeconds(latestProgress, seconds, ProgressLogic.today()))
            }
        }
    }

    // Background music plays on the main (learning path) page and on the profile picker.
    // Both use the same player, so it carries on without a break when a profile is chosen.
    LaunchedEffect(currentScreen, backgroundMusic) {
        backgroundMusic?.setWantedOnScreen(
            currentScreen == AppScreen.LEARNING_PATH || currentScreen == AppScreen.PROFILE_SELECT
        )
    }
    // Music is allowed only if the parent setting is on AND the child has not muted it
    LaunchedEffect(musicEnabled, sessionMuted, backgroundMusic) {
        backgroundMusic?.setAllowed(musicEnabled && !sessionMuted)
    }

    // System back goes up one level instead of leaving the app.
    // On the picker with nobody signed in there is nowhere to go back to, so back leaves the app.
    BackHandler(
        enabled = currentScreen != AppScreen.LEARNING_PATH &&
            !(currentScreen == AppScreen.PROFILE_SELECT && activeProfile == null)
    ) {
        currentScreen = when (currentScreen) {
            AppScreen.CONSONANT_FAMILY -> AppScreen.CONSONANTS
            AppScreen.LESSON -> lessonReturnScreen
            else -> AppScreen.LEARNING_PATH
        }
    }

    // Back closes the menu first, if it is open
    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        // Opened only with the menu button, so swipes never clash with tracing or scrolling
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            AppDrawerContent(
                currentScreen = currentScreen,
                profileName = activeProfile?.name,
                profileAvatar = activeProfile?.avatar,
                onNavigate = { target ->
                    currentScreen = target
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppTopBar(
                onMenuClick = { scope.launch { drawerState.open() } },
                // The picker has no menu: nobody has chosen a profile yet, so nothing else may open
                showMenu = currentScreen != AppScreen.PROFILE_SELECT,
                // No point muting music the parent has switched off
                showMute = (currentScreen == AppScreen.LEARNING_PATH || currentScreen == AppScreen.PROFILE_SELECT) &&
                    musicEnabled,
                isMuted = sessionMuted,
                onToggleMute = { sessionMuted = !sessionMuted }
            )
        }
    ) { innerPadding ->
        when (currentScreen) {
            AppScreen.PROFILE_SELECT -> {
                ProfileSelectScreen(
                    profiles = profiles,
                    onSelect = { id -> selectProfile(id) },
                    onCreateFirst = { name, avatar -> createProfile(name, avatar, openIt = true) },
                    // Only when a child is already active (parent chose "Switch profile")
                    onCancel = if (activeProfile != null) {
                        { currentScreen = AppScreen.LEARNING_PATH }
                    } else null,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppScreen.LEARNING_PATH -> {
                LearningPathScreen(
                    currentStars = totalStars,
                    currentStreak = streakDays,
                    vowelsCompleted = vowelsCompleted,
                    consonantsCompleted = consonantsCompleted,
                    onOpenVowels = {
                        currentScreen = AppScreen.VOWELS
                    },
                    onOpenConsonants = {
                        currentScreen = AppScreen.CONSONANTS
                    },
                    onStartCustomReader = {
                        currentScreen = AppScreen.CUSTOM_READER
                    },
                    onStartReadAloud = {
                        currentScreen = AppScreen.READ_ALOUD
                    },
                    onStartVocabularySpeech = {
                        currentScreen = AppScreen.VOCABULARY_SPEECH
                    },
                    onStartTestKnowledge = {
                        currentScreen = AppScreen.TEST_KNOWLEDGE
                    },
                    onOpenParentDashboard = {
                        currentScreen = AppScreen.PARENT_DASHBOARD
                    },
                    onOpenLicenses = {
                        currentScreen = AppScreen.LICENSES
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppScreen.VOWELS -> {
                VowelsScreen(
                    onBack = {
                        currentScreen = AppScreen.LEARNING_PATH
                    },
                    onOpenUnit = { unitId ->
                        activeUnitId = unitId
                        lessonReturnScreen = AppScreen.VOWELS
                        currentScreen = AppScreen.LESSON
                    },
                    genuineCompletedUnitIds = genuineCompletedUnitIds,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppScreen.CONSONANTS -> {
                ConsonantsScreen(
                    onBack = {
                        currentScreen = AppScreen.LEARNING_PATH
                    },
                    onOpenFamily = { familyIndex ->
                        activeFamilyIndex = familyIndex
                        currentScreen = AppScreen.CONSONANT_FAMILY
                    },
                    completedFamilyIndexes = completedFamilyIndexes,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppScreen.CONSONANT_FAMILY -> {
                ConsonantFamilyScreen(
                    familyIndex = activeFamilyIndex,
                    onBack = {
                        currentScreen = AppScreen.CONSONANTS
                    },
                    onOpenUnit = { unitId ->
                        activeUnitId = unitId
                        lessonReturnScreen = AppScreen.CONSONANT_FAMILY
                        currentScreen = AppScreen.LESSON
                    },
                    genuineCompletedUnitIds = genuineCompletedUnitIds,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppScreen.LESSON -> {
                val unitId = activeUnitId ?: "unit_a"
                // Next / Random stay inside the lesson's own category (vowels page, or that consonant's page)
                val nextUnitId = LessonCategories.nextUnitId(unitId)
                val hasOtherLessons = LessonCategories.unitsFor(unitId).size > 1
                val goNext: () -> Unit = { nextUnitId?.let { activeUnitId = it } }
                val goRandom: () -> Unit = {
                    LessonCategories.randomUnitId(unitId, genuineCompletedUnitIds)?.let { activeUnitId = it }
                }
                // key(unitId) gives every lesson a fresh start when Next / Random switches to another one
                key(unitId) {
                    LessonScreen(
                        unitId = unitId,
                        onCloseLesson = {
                            currentScreen = lessonReturnScreen
                        },
                        onLessonCompleted = { stars, usedSelfCheck ->
                            commit(
                                ProgressLogic.recordLessonCompleted(
                                    state = progress,
                                    unitId = unitId,
                                    stars = stars,
                                    usedSelfCheck = usedSelfCheck,
                                    today = ProgressLogic.today()
                                )
                            )
                        },
                        onNextLesson = if (nextUnitId != null) goNext else null,
                        onRandomLesson = if (hasOtherLessons) goRandom else null,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }

            AppScreen.READ_ALOUD -> {
                ReadAloudScreen(
                    onBack = {
                        currentScreen = AppScreen.LEARNING_PATH
                    },
                    onOpenLesson = { unitId ->
                        activeUnitId = unitId
                        lessonReturnScreen = AppScreen.READ_ALOUD
                        currentScreen = AppScreen.LESSON
                    },
                    readingPosition = storyReadingPosition,
                    onReadingPositionChange = { storyReadingPosition = it },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppScreen.CUSTOM_READER -> {
                CustomTextReaderScreen(
                    onBack = {
                        currentScreen = AppScreen.LEARNING_PATH
                    },
                    onOpenLesson = { unitId ->
                        activeUnitId = unitId
                        lessonReturnScreen = AppScreen.CUSTOM_READER
                        currentScreen = AppScreen.LESSON
                    },
                    draftText = customDraftText,
                    onDraftTextChange = { customDraftText = it },
                    readingText = customReadingText,
                    onReadingTextChange = { customReadingText = it },
                    readingPosition = customReadingPosition,
                    onReadingPositionChange = { customReadingPosition = it },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppScreen.VOCABULARY_SPEECH -> {
                VocabularySpeechScreen(
                    onBack = {
                        currentScreen = AppScreen.LEARNING_PATH
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppScreen.TEST_KNOWLEDGE -> {
                TestYourKnowledgeScreen(
                    onBack = {
                        currentScreen = AppScreen.LEARNING_PATH
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppScreen.PARENT_DASHBOARD -> {
                ParentDashboardScreen(
                    onBack = {
                        currentScreen = AppScreen.LEARNING_PATH
                    },
                    onOpenLesson = { unitId ->
                        activeUnitId = unitId
                        lessonReturnScreen = AppScreen.PARENT_DASHBOARD
                        currentScreen = AppScreen.LESSON
                    },
                    completedUnitIds = completedUnitIds,
                    minutesLearnedToday = minutesLearnedToday,
                    dailyGoalMinutes = dailyGoalMinutes,
                    onDailyGoalChange = { commit(progress.copy(dailyGoalMinutes = it)) },
                    currentStreak = streakDays,
                    weeklyMinutes = weeklyMinutes,
                    musicEnabled = musicEnabled,
                    onMusicEnabledChange = { enabled ->
                        musicEnabled = enabled
                        profileStore.saveMusicEnabled(enabled)
                        // Switching music back on also clears the child's mute
                        if (enabled) sessionMuted = false
                    },
                    profiles = profiles,
                    activeProfileId = activeProfileId,
                    onCreateProfile = { name, avatar -> createProfile(name, avatar, openIt = false) },
                    onEditProfile = { id, name, avatar -> editProfile(id, name, avatar) },
                    onDeleteProfile = { id -> deleteProfile(id) },
                    onSwitchProfile = { currentScreen = AppScreen.PROFILE_SELECT },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppScreen.LICENSES -> {
                LicensesScreen(
                    onBack = {
                        currentScreen = AppScreen.LEARNING_PATH
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
    } // ModalNavigationDrawer
}
