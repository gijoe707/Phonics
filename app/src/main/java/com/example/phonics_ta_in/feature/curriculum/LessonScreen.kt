package com.example.phonics_ta_in.feature.curriculum

import android.Manifest
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.phonics_ta_in.core.audio.AppAudioEngine
import com.example.phonics_ta_in.core.curriculum.CurriculumRepository
import com.example.phonics_ta_in.core.curriculum.ExerciseType
import com.example.phonics_ta_in.core.curriculum.LessonExercise
import com.example.phonics_ta_in.core.curriculum.UnitKind
import com.example.phonics_ta_in.core.speech.SpeechRecognizerEngine
import com.example.phonics_ta_in.core.speech.AcousticSoundMatcher
import com.example.phonics_ta_in.core.speech.IsolatedSoundRecorder
import com.example.phonics_ta_in.core.speech.ReferenceSoundBank
import com.example.phonics_ta_in.core.speech.SoundMatch
import com.example.phonics_ta_in.core.speech.SoundSiblings
import com.example.phonics_ta_in.core.speech.SpeechState
import com.example.phonics_ta_in.feature.tracing.TracingCanvas

@Composable
fun LessonScreen(
    unitId: String,
    onCloseLesson: () -> Unit,
    onLessonCompleted: (xpEarned: Int, starsEarned: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioEngine = remember { AppAudioEngine(context) }

    DisposableEffect(Unit) {
        onDispose {
            audioEngine.release()
        }
    }

    val exercises = remember(unitId) { CurriculumRepository.getExercisesForUnit(unitId) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var selectedOptionId by remember { mutableStateOf<String?>(null) }
    var isCorrectFeedback by remember { mutableStateOf<Boolean?>(null) }
    var totalXp by remember { mutableIntStateOf(0) }

    if (exercises.isEmpty() || currentIndex >= exercises.size) {
        // Lesson Completion Screen
        LessonCompletionView(
            xpGained = totalXp + 20,
            starsGained = 3,
            onContinue = { onLessonCompleted(totalXp + 20, 3) }
        )
        return
    }

    val currentExercise = exercises[currentIndex]
    val progress = (currentIndex.toFloat()) / exercises.size.toFloat()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF8FD))
            .padding(16.dp)
    ) {
        // Top Bar: Exit button, Progress Bar, Stars
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCloseLesson) {
                Icon(Icons.Default.Close, contentDescription = "Close Lesson", tint = Color.Gray)
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .weight(1f)
                    .height(12.dp)
                    .padding(horizontal = 8.dp),
                color = Color(0xFF6750A4),
                trackColor = Color(0xFFE7E0EC)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = "Stars", tint = Color(0xFFFFB703))
                Spacer(modifier = Modifier.width(4.dp))
                Text("3", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Exercise Content area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (currentExercise.type) {
                ExerciseType.PRECURSOR_HEAR_SOUND, ExerciseType.HEAR_AND_SEE -> {
                    PrecursorSoundView(
                        exercise = currentExercise,
                        onPlaySound = {
                            audioEngine.speakTamilText(currentExercise.targetUnit.text)
                            isCorrectFeedback = true
                        }
                    )
                }

                ExerciseType.LISTENING_SKILL_TEST,
                ExerciseType.READING_SKILL_TEST,
                ExerciseType.LISTEN_AND_TAP_LETTER,
                ExerciseType.LISTEN_AND_TAP_PICTURE -> {
                    ListenAndTapView(
                        exercise = currentExercise,
                        selectedOptionId = selectedOptionId,
                        isCorrectFeedback = isCorrectFeedback,
                        onPlaySound = {
                            audioEngine.speakTamilText(currentExercise.targetUnit.text)
                        },
                        onSelectOption = { option ->
                            selectedOptionId = option.id
                            isCorrectFeedback = option.isCorrect
                            if (option.isCorrect) {
                                audioEngine.speakTamilText(option.text)
                                totalXp += 10
                            }
                        }
                    )
                }

                ExerciseType.WRITING_SKILL_TEST, ExerciseType.TRACE_LETTER -> {
                    TracingCanvas(
                        letterText = currentExercise.targetUnit.text,
                        templateStrokes = currentExercise.templateStrokes,
                        onTraceCompleted = {
                            audioEngine.speakTamilText(currentExercise.targetUnit.text)
                            isCorrectFeedback = true
                            totalXp += 15
                        }
                    )
                }

                ExerciseType.SPEECH_PRECURSOR_TEST, ExerciseType.SAY_LETTER -> {
                    // key() gives each letter a fresh recognizer + state (otherwise the previous letter's
                    // target/passed state is reused when two speech exercises follow each other)
                    androidx.compose.runtime.key(currentExercise.id) {
                        SayLetterView(
                            exercise = currentExercise,
                            audioEngine = audioEngine,
                            onSpeechPass = {
                                isCorrectFeedback = true
                                totalXp += 15
                            }
                        )
                    }
                }

                ExerciseType.SINGLE_LETTER_MEANING_TEST -> {
                    SingleLetterMeaningView(
                        exercise = currentExercise,
                        onPlaySound = {
                            val letter = currentExercise.singleLetterWord?.letter ?: currentExercise.targetUnit.text
                            audioEngine.speakTamilText(letter)
                            isCorrectFeedback = true
                            totalXp += 10
                        }
                    )
                }

                ExerciseType.WORD_VOCABULARY_SPEECH_TEST -> {
                    VocabularyWordSpeechView(
                        exercise = currentExercise,
                        audioEngine = audioEngine,
                        onSpeechPass = {
                            isCorrectFeedback = true
                            totalXp += 20
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val isInfoCard = currentExercise.type == ExerciseType.PRECURSOR_HEAR_SOUND ||
                         currentExercise.type == ExerciseType.HEAR_AND_SEE ||
                         currentExercise.type == ExerciseType.SINGLE_LETTER_MEANING_TEST

        val isContinueEnabled = isInfoCard || isCorrectFeedback == true

        // Bottom Continue Button
        Button(
            onClick = {
                selectedOptionId = null
                isCorrectFeedback = null
                currentIndex++
            },
            enabled = isContinueEnabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6750A4)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Continue", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun HearAndSeeView(
    exercise: LessonExercise,
    onPlaySound: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = exercise.promptText,
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF1D1B20)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Big Letter Display Card
        Card(
            modifier = Modifier
                .size(200.dp)
                .clickable { onPlaySound() },
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = exercise.targetUnit.text,
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF21005D)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play sound", tint = Color(0xFF6750A4))
                        Text("Tap to hear", fontSize = 14.sp, color = Color(0xFF49454F))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Keyword: ${exercise.targetUnit.keywordTamil} (${exercise.targetUnit.keywordEnglish})",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF49454F)
        )
    }
}

@Composable
fun ListenAndTapView(
    exercise: LessonExercise,
    selectedOptionId: String?,
    isCorrectFeedback: Boolean?,
    onPlaySound: () -> Unit,
    onSelectOption: (com.example.phonics_ta_in.core.curriculum.ExerciseOption) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = exercise.promptText,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1D1B20)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Audio Prompt Play Button
        Surface(
            modifier = Modifier
                .size(72.dp)
                .clickable { onPlaySound() },
            shape = CircleShape,
            color = Color(0xFF6750A4),
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = "Play sound prompt",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Options Grid / List
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            exercise.options.forEach { option ->
                val isSelected = selectedOptionId == option.id
                val cardColor = when {
                    isSelected && isCorrectFeedback == true -> Color(0xFFE8F5E9)
                    isSelected && isCorrectFeedback == false -> Color(0xFFFFEBEE)
                    else -> Color.White
                }
                val borderColor = when {
                    isSelected && isCorrectFeedback == true -> Color(0xFF2E7D32)
                    isSelected && isCorrectFeedback == false -> Color(0xFFC62828)
                    else -> Color(0xFFE7E0EC)
                }

                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .clickable { onSelectOption(option) },
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, borderColor),
                    colors = CardDefaults.outlinedCardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = option.text,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D1B20)
                        )
                        Text(
                            text = option.subtext,
                            fontSize = 16.sp,
                            color = Color(0xFF49454F)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LessonCompletionView(
    xpGained: Int,
    starsGained: Int,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFBF8FD))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎉 Lesson Complete! 🎉",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6750A4)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "⭐ $starsGained Stars Earned!", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "⚡ +$xpGained XP", fontSize = 20.sp, color = Color(0xFF21005D))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onContinue,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Back to Path", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PrecursorSoundView(
    exercise: LessonExercise,
    onPlaySound: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFFFE0B2)
        ) {
            Text(
                text = "Precursor Phonics Sound",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE65100),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = exercise.promptText,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1D1B20)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .size(200.dp)
                .clickable { onPlaySound() },
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = exercise.targetUnit.text,
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF21005D)
                    )
                    Text(
                        text = exercise.targetUnit.phonicsSound,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6750A4)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play isolated sound", tint = Color(0xFF6750A4))
                        Text("Tap to hear sound", fontSize = 14.sp, color = Color(0xFF49454F))
                    }
                }
            }
        }
    }
}

@Composable
fun SingleLetterMeaningView(
    exercise: LessonExercise,
    onPlaySound: () -> Unit
) {
    val singleWord = exercise.singleLetterWord ?: exercise.targetUnit.singleLetterWord

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFE8F5E9)
        ) {
            Text(
                text = "ஓரெழுத்து ஒருமொழி (Single-Letter Word)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = exercise.promptText,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1D1B20)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { onPlaySound() },
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = singleWord?.letter ?: exercise.targetUnit.text,
                    fontSize = 90.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF21005D)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "பொருள் (Meaning): ${singleWord?.meaningTamil ?: ""}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1D1B20)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = singleWord?.meaningEnglish ?: "",
                    fontSize = 20.sp,
                    color = Color(0xFF49454F)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play sound", tint = Color(0xFF6750A4))
                    Text("Tap to hear word", fontSize = 16.sp, color = Color(0xFF6750A4))
                }
            }
        }
    }
}

@Composable
fun VocabularyWordSpeechView(
    exercise: LessonExercise,
    audioEngine: AppAudioEngine,
    onSpeechPass: () -> Unit
) {
    val context = LocalContext.current
    val word = exercise.targetWord ?: exercise.targetUnit.startingWords.firstOrNull()
    val targetWordText = word?.wordTamil ?: exercise.targetUnit.keywordTamil

    var isListening by remember { mutableStateOf(false) }
    var speechStatusText by remember { mutableStateOf("Tap the mic and say '$targetWordText'") }
    var isPassed by remember { mutableStateOf(false) }

    val speechEngine = remember {
        SpeechRecognizerEngine(context) { speechState ->
            when (speechState) {
                is SpeechState.Listening -> {
                    isListening = true
                    speechStatusText = "Listening... Say '$targetWordText' now"
                }
                is SpeechState.Processing -> {
                    isListening = false
                    speechStatusText = "Evaluating speech..."
                }
                is SpeechState.Recognized -> {
                    isListening = false
                    val isAccurate = speechState.candidates.any { SpeechRecognizerEngine.isMatch(it, targetWordText) }
                    if (isAccurate) {
                        isPassed = true
                        speechStatusText = "Great job! Recognized: '$targetWordText'"
                        audioEngine.speakTamilText("சரியாக சொன்னாய்!")
                        onSpeechPass()
                    } else {
                        speechStatusText = "Heard '${speechState.topMatch}'. Try again!"
                    }
                }
                is SpeechState.Error -> {
                    isListening = false
                    speechStatusText = speechState.message
                }
                else -> {
                    isListening = false
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            speechEngine.destroy()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            speechEngine.startListening()
        } else {
            speechStatusText = "Microphone permission required for speech test."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFFE3F2FD)
        ) {
            Text(
                text = "Vocabulary Speech Practice",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = exercise.promptText,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1D1B20)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { audioEngine.speakTamilText(targetWordText) },
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isPassed) Color(0xFFE8F5E9) else Color(0xFFF3EDF7)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = targetWordText,
                    fontSize = 54.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPassed) Color(0xFF2E7D32) else Color(0xFF1D1B20)
                )

                if (word != null && word.syllables.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Syllables: ${word.syllables.joinToString(" - ")}",
                        fontSize = 18.sp,
                        color = Color(0xFF6750A4)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = word?.wordEnglish ?: exercise.targetUnit.keywordEnglish,
                    fontSize = 18.sp,
                    color = Color(0xFF49454F)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = speechStatusText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF49454F)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Mic Button
        Surface(
            modifier = Modifier
                .size(88.dp)
                .clickable {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED

                    if (isListening) {
                        speechEngine.stopListening()
                    } else if (hasPermission) {
                        speechEngine.startListening()
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
            shape = CircleShape,
            color = when {
                isPassed -> Color(0xFF2E7D32)
                isListening -> Color(0xFFC62828)
                else -> Color(0xFF6750A4)
            },
            shadowElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = "Microphone",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                isPassed = true
                speechStatusText = "Marked as spoken correctly!"
                audioEngine.speakTamilText(targetWordText)
                onSpeechPass()
            }
        ) {
            Text("I said it! (Self Check)", fontSize = 16.sp)
        }
    }
}

@Composable
fun SayLetterView(
    exercise: LessonExercise,
    audioEngine: AppAudioEngine,
    onSpeechPass: () -> Unit
) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var speechStatusText by remember { mutableStateOf("Tap the mic and speak the sound") }
    var isPassed by remember { mutableStateOf(false) }
    // true while the microphone is captured for the sound-match check (as opposed to the word recognizer)
    var isRecordingSound by remember { mutableStateOf(false) }
    // non-null once the sound check handed over to the word recognizer; shown while it listens for a word
    var wordFallbackNote by remember { mutableStateOf<String?>(null) }
    // bumped whenever another reference sound finished building, to refresh the "sound check ready" caption
    var referenceTick by remember { mutableIntStateOf(0) }

    val unit = exercise.targetUnit
    // The sound is judged against how TTS says this letter AND how it says its sibling letters (minus its short/long twin)
    val peerUnits = remember(unit.id) {
        SoundSiblings.comparisonPeers(unit, CurriculumRepository.poolFor(unit))
    }
    val soundRecorder = remember { IsolatedSoundRecorder() }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }

    // Build the TTS reference sounds (this letter first, then its siblings) in the background
    LaunchedEffect(unit.id) {
        ReferenceSoundBank.ensure(context, audioEngine, listOf(unit) + peerUnits) { referenceTick++ }
    }

    val speechEngine = remember {
        SpeechRecognizerEngine(
            context = context,
            isolatedSound = true,
            // Bias the recognizer towards the bare letter so it doesn't turn it into a word (அ -> அம்மா)
            biasingStrings = listOf(exercise.targetUnit.text)
        ) { speechState ->
            when (speechState) {
                is SpeechState.Listening -> {
                    isListening = true
                    val keywordHint = exercise.targetUnit.keywordTamil
                    speechStatusText = wordFallbackNote?.let { note ->
                        if (keywordHint.isNotBlank()) "$note\nSay a word with this sound, e.g. '$keywordHint', now"
                        else "$note\nSay a word with this sound now"
                    } ?: "Listening... Speak '${exercise.targetUnit.text}' now"
                }
                is SpeechState.Processing -> {
                    isListening = false
                    speechStatusText = "Evaluating speech..."
                }
                is SpeechState.Recognized -> {
                    isListening = false
                    val match = SpeechRecognizerEngine.evaluateIsolatedSound(speechState.candidates, exercise.targetUnit)
                    if (match != SoundMatch.NONE) {
                        isPassed = true
                        val heard = speechState.candidates.take(3).joinToString(", ") { "'$it'" }
                        speechStatusText = if (match == SoundMatch.EXACT) {
                            "Great job! Heard: $heard"
                        } else {
                            "Good! Heard: $heard — a word with this sound.\nNow try saying just '${exercise.targetUnit.phonicsSound}'."
                        }
                        audioEngine.speakTamilText("சரியாக சொன்னாய்!")
                        onSpeechPass()
                    } else {
                        // Show top 3 candidates so we can diagnose what ASR returned
                        val topCandidates = speechState.candidates.take(3).joinToString(", ") { "'$it'" }
                        speechStatusText = "Heard: $topCandidates\nSpeak '${exercise.targetUnit.phonicsSound}' clearly or tap 'I Said It!'"
                    }
                }
                is SpeechState.Error -> {
                    isListening = false
                    speechStatusText = speechState.message
                }
                else -> {
                    isListening = false
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            soundRecorder.cancel()
            speechEngine.destroy()
        }
    }

    // --- Sound check: the child's mic recording vs. the sound Android TTS produces for this letter ---
    // The microphone is used by ONE of the two at a time: first the recorder (acoustic comparison), and only if that
    // does not accept the sound, the word recognizer as fallback.

    fun startWordFallback(reason: String) {
        wordFallbackNote = reason
        speechEngine.startListening()
    }

    fun onAcousticResult(result: AcousticSoundMatcher.Result) {
        when (result.verdict) {
            AcousticSoundMatcher.Verdict.MATCH -> {
                isPassed = true
                speechStatusText = "Great job! Your sound matches ${unit.phonicsSound}"
                audioEngine.speakTamilText("சரியாக சொன்னாய்!")
                onSpeechPass()
            }
            AcousticSoundMatcher.Verdict.NO_MATCH ->
                startWordFallback("That didn't match ${unit.phonicsSound} yet.")
            AcousticSoundMatcher.Verdict.NOT_VOICED ->
                startWordFallback("A vowel should be a clear voiced sound.")
            AcousticSoundMatcher.Verdict.TOO_QUIET,
            AcousticSoundMatcher.Verdict.TOO_SHORT ->
                startWordFallback("I couldn't hear that clearly.")
            AcousticSoundMatcher.Verdict.NOT_ENOUGH_REFERENCES ->
                startWordFallback("Sound check isn't available yet.")
        }
    }

    fun startAcousticAttempt() {
        wordFallbackNote = null
        val targetRef = ReferenceSoundBank.get(unit)
        val peerRefs = ReferenceSoundBank.available(peerUnits).values
        if (targetRef == null || peerRefs.size < AcousticSoundMatcher.MIN_PEERS) {
            // Reference sounds are still being built, or TTS cannot render this letter: words only.
            ReferenceSoundBank.ensure(context, audioEngine, listOf(unit) + peerUnits) { referenceTick++ }
            startWordFallback("Sound check isn't available yet.")
            return
        }
        audioEngine.stop() // don't record our own TTS voice
        isListening = true
        isRecordingSound = true
        speechStatusText = "Listening... say just the sound ${unit.phonicsSound}"
        soundRecorder.start(
            onSpeechStarted = { speechStatusText = "Hearing you..." }
        ) { outcome ->
            isRecordingSound = false
            isListening = false
            when (outcome) {
                is IsolatedSoundRecorder.Outcome.Captured -> {
                    speechStatusText = "Comparing your sound..."
                    val samples = outcome.samples
                    Thread {
                        val result = AcousticSoundMatcher.evaluate(
                            recording = samples,
                            target = targetRef,
                            peers = peerRefs,
                            requireVoicing = unit.kind == UnitKind.UYIR // vowels are always voiced; consonants may not be
                        )
                        Log.d("SoundMatch", "'${unit.text}' -> ${result.verdict}: ${result.detail}")
                        mainHandler.post { onAcousticResult(result) }
                    }.start()
                }
                IsolatedSoundRecorder.Outcome.NoSpeech -> startWordFallback("I didn't hear a sound.")
                is IsolatedSoundRecorder.Outcome.Failed -> startWordFallback(outcome.message)
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startAcousticAttempt()
        } else {
            speechStatusText = "Microphone permission required for speech test."
        }
    }

    fun onMicTap() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        when {
            isRecordingSound -> {
                soundRecorder.cancel()
                isRecordingSound = false
                isListening = false
                speechStatusText = "Tap the mic and speak the sound"
            }
            isListening -> speechEngine.stopListening()
            hasPermission -> startAcousticAttempt()
            else -> permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = exercise.promptText,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1D1B20)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Target Letter Card
        Card(
            modifier = Modifier
                .size(180.dp)
                .clickable { audioEngine.speakTamilText(exercise.targetUnit.text) },
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isPassed) Color(0xFFE8F5E9) else Color(0xFFEADDFF)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = exercise.targetUnit.text,
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPassed) Color(0xFF2E7D32) else Color(0xFF21005D)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Listen prompt sound",
                            tint = Color(0xFF6750A4)
                        )
                        Text("Tap to hear", fontSize = 14.sp, color = Color(0xFF49454F))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = speechStatusText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF49454F)
        )

        // referenceTick is read so this caption refreshes as the TTS reference sounds finish building
        val soundCheckReady = referenceTick >= 0 &&
                ReferenceSoundBank.get(unit) != null &&
                ReferenceSoundBank.available(peerUnits).size >= AcousticSoundMatcher.MIN_PEERS
        Text(
            text = if (soundCheckReady) {
                "Sound check ready: say just the sound"
            } else {
                "Getting the sound check ready (saying a word works too)"
            },
            fontSize = 12.sp,
            color = Color(0xFF79747E)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Mic Button
        Surface(
            modifier = Modifier
                .size(88.dp)
                .clickable { onMicTap() },
            shape = CircleShape,
            color = when {
                isPassed -> Color(0xFF2E7D32)
                isListening -> Color(0xFFC62828)
                else -> Color(0xFF6750A4)
            },
            shadowElevation = 6.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = "Microphone",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Self-Check Fallback Button
        OutlinedButton(
            onClick = {
                isPassed = true
                speechStatusText = "Marked as spoken correctly!"
                audioEngine.speakTamilText(exercise.targetUnit.text)
                onSpeechPass()
            }
        ) {
            Text("I said it! (Self Check)", fontSize = 16.sp)
        }

        // Example words that contain this sound, with drawn pictures. Tap a card to hear the word.
        if (exercise.targetUnit.startingWords.isNotEmpty()) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Words with this sound (tap to hear)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF49454F)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                exercise.targetUnit.startingWords.forEach { word ->
                    WordPracticeCard(
                        word = word,
                        onClick = { audioEngine.speakTamilText(word.wordTamil) }
                    )
                }
            }
        }
    }
}
