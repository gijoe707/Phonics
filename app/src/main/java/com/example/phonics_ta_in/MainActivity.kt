package com.example.phonics_ta_in

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.phonics_ta_in.feature.curriculum.ConsonantFamilyScreen
import com.example.phonics_ta_in.feature.curriculum.ConsonantsScreen
import com.example.phonics_ta_in.feature.curriculum.LearningPathScreen
import com.example.phonics_ta_in.feature.curriculum.LessonScreen
import com.example.phonics_ta_in.feature.curriculum.VocabularySpeechScreen
import com.example.phonics_ta_in.feature.curriculum.VowelsScreen
import com.example.phonics_ta_in.feature.readaloud.CustomTextReaderScreen
import com.example.phonics_ta_in.feature.readaloud.ReadAloudScreen
import com.example.phonics_ta_in.ui.theme.PhonicstaINTheme

enum class AppScreen {
    LEARNING_PATH,
    VOWELS,             // grid of 12 vowels + ஃ
    CONSONANTS,         // grid of 18 consonants
    CONSONANT_FAMILY,   // one consonant expanded: க், க, கா, கி ...
    LESSON,             // exercise set for ONE letter
    READ_ALOUD,
    CUSTOM_READER,      // paste your own text and read along
    VOCABULARY_SPEECH
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhonicstaINTheme {
                TamilPhonicsApp()
            }
        }
    }
}

@Composable
fun TamilPhonicsApp() {
    // Navigation state survives rotation; the lesson itself still restarts.
    var currentScreen by rememberSaveable { mutableStateOf(AppScreen.LEARNING_PATH) }
    var activeUnitId by rememberSaveable { mutableStateOf<String?>(null) }
    var activeFamilyIndex by rememberSaveable { mutableStateOf(1) }
    // Where "close lesson" / back from a lesson returns to (the vowels page or a consonant's page)
    var lessonReturnScreen by rememberSaveable { mutableStateOf(AppScreen.LEARNING_PATH) }

    // Persisted state for Custom Text Reader & Read Aloud practice
    var customDraftText by rememberSaveable { mutableStateOf("") }
    var customReadingText by rememberSaveable { mutableStateOf<String?>(null) }
    var customReadingPosition by rememberSaveable { mutableIntStateOf(0) }
    var storyReadingPosition by rememberSaveable { mutableIntStateOf(0) }

    var userXp by remember { mutableIntStateOf(120) }
    var streakDays by remember { mutableIntStateOf(4) }

    // System back goes up one level instead of leaving the app
    BackHandler(enabled = currentScreen != AppScreen.LEARNING_PATH) {
        currentScreen = when (currentScreen) {
            AppScreen.CONSONANT_FAMILY -> AppScreen.CONSONANTS
            AppScreen.LESSON -> lessonReturnScreen
            else -> AppScreen.LEARNING_PATH
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        when (currentScreen) {
            AppScreen.LEARNING_PATH -> {
                LearningPathScreen(
                    currentXp = userXp,
                    currentStreak = streakDays,
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
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppScreen.LESSON -> {
                val unitId = activeUnitId ?: "unit_a"
                LessonScreen(
                    unitId = unitId,
                    onCloseLesson = {
                        currentScreen = lessonReturnScreen
                    },
                    onLessonCompleted = { xpEarned, _ ->
                        userXp += xpEarned
                        currentScreen = lessonReturnScreen
                    },
                    modifier = Modifier.padding(innerPadding)
                )
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
        }
    }
}
