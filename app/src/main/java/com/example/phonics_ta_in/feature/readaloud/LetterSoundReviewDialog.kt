package com.example.phonics_ta_in.feature.readaloud

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.phonics_ta_in.core.audio.AppAudioEngine
import com.example.phonics_ta_in.core.curriculum.CurriculumRepository
import com.example.phonics_ta_in.core.speech.SpeechRecognizerEngine
import com.example.phonics_ta_in.core.speech.SpeechState
import com.example.phonics_ta_in.core.text.TamilLetterType
import com.example.phonics_ta_in.core.text.TamilTextSegmenter
import com.example.phonics_ta_in.core.text.TamilTransliteration

enum class ReviewMode {
    SPEECH_TEST_1BY1,
    LETTERS_GRID
}

/**
 * Extracts distinct Tamil letters from [text].
 */
fun extractDistinctLetters(text: String): List<String> {
    if (text.isBlank()) return emptyList()
    val graphemes = TamilTextSegmenter.segmentGraphemes(text)
    return graphemes
        .filter { it.type != TamilLetterType.OTHER && it.text.trim().isNotEmpty() }
        .map { it.text.trim() }
        .distinct()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LetterSoundReviewDialog(
    distinctLetters: List<String>,
    onStartReading: () -> Unit,
    onOpenLesson: ((String) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    if (distinctLetters.isEmpty()) {
        onStartReading()
        return
    }

    val context = LocalContext.current
    val audioEngine = remember { AppAudioEngine(context) }

    var reviewMode by remember { mutableStateOf(ReviewMode.SPEECH_TEST_1BY1) }
    var currentIndex by remember { mutableIntStateOf(0) }
    val passedLetters = remember { mutableStateListOf<String>() }

    val currentLetter = distinctLetters[currentIndex.coerceIn(0, distinctLetters.lastIndex)]
    val currentTransliteration = remember(currentLetter) {
        TamilTransliteration.transliterateGrapheme(currentLetter)
    }

    var isListening by remember { mutableStateOf(false) }
    var speechStatusText by remember { mutableStateOf("Tap mic to speak '$currentLetter'") }

    val speechEngine = remember {
        SpeechRecognizerEngine(
            context = context,
            isolatedSound = true,
            biasingStrings = distinctLetters
        ) { speechState ->
            when (speechState) {
                is SpeechState.Listening -> {
                    isListening = true
                    speechStatusText = "Listening... Say '$currentLetter' now"
                }
                is SpeechState.Processing -> {
                    isListening = false
                    speechStatusText = "Evaluating speech..."
                }
                is SpeechState.Recognized -> {
                    isListening = false
                    val matched = speechState.candidates.any { cand ->
                        SpeechRecognizerEngine.isMatch(cand, currentLetter) ||
                            cand.trim() == currentLetter ||
                            cand.contains(currentLetter)
                    }
                    if (matched) {
                        if (!passedLetters.contains(currentLetter)) {
                            passedLetters.add(currentLetter)
                        }
                        speechStatusText = "Correct! Recognized: '$currentLetter'"
                        audioEngine.speakTamilText("சரியாக சொன்னாய்!")
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
            audioEngine.release()
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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFFBF8FD)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Letter Sound Review 🎯",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6750A4)
                        )
                        Text(
                            text = "${distinctLetters.size} distinct letters in your text",
                            fontSize = 13.sp,
                            color = Color(0xFF49454F)
                        )
                    }
                    Button(
                        onClick = onStartReading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                    ) {
                        Text("Skip & Read ➔")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mode Selector Tabs (Speech Test 1-by-1 vs Grid)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF3EDF7), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val is1by1 = reviewMode == ReviewMode.SPEECH_TEST_1BY1
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (is1by1) Color(0xFF6750A4) else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                speechEngine.stopListening()
                                reviewMode = ReviewMode.SPEECH_TEST_1BY1
                            }
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = if (is1by1) Color.White else Color(0xFF49454F),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Speech Test 1-by-1",
                                fontSize = 13.sp,
                                fontWeight = if (is1by1) FontWeight.Bold else FontWeight.Medium,
                                color = if (is1by1) Color.White else Color(0xFF49454F)
                            )
                        }
                    }

                    val isGrid = reviewMode == ReviewMode.LETTERS_GRID
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isGrid) Color(0xFF6750A4) else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                speechEngine.stopListening()
                                reviewMode = ReviewMode.LETTERS_GRID
                            }
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.GridView,
                                contentDescription = null,
                                tint = if (isGrid) Color.White else Color(0xFF49454F),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Letters Grid",
                                fontSize = 13.sp,
                                fontWeight = if (isGrid) FontWeight.Bold else FontWeight.Medium,
                                color = if (isGrid) Color.White else Color(0xFF49454F)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content Views
                when (reviewMode) {
                    ReviewMode.SPEECH_TEST_1BY1 -> {
                        val isCurrentPassed = passedLetters.contains(currentLetter)

                        // Big Card for Current Letter
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clickable { audioEngine.speakTamilText(currentLetter) },
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrentPassed) Color(0xFFE8F5E9) else Color(0xFFEADDFF)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF6750A4)
                                    ) {
                                        Text(
                                            text = "Letter ${currentIndex + 1} of ${distinctLetters.size}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                        )
                                    }
                                    if (isCurrentPassed) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Passed",
                                            tint = Color(0xFF2E7D32)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = currentLetter,
                                    fontSize = 72.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrentPassed) Color(0xFF2E7D32) else Color(0xFF21005D)
                                )

                                if (currentTransliteration.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = currentTransliteration,
                                        fontSize = 20.sp,
                                        color = Color(0xFF6750A4),
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Hear sound",
                                        tint = Color(0xFF6750A4)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Tap to hear sound",
                                        fontSize = 14.sp,
                                        color = Color(0xFF6750A4)
                                    )
                                }

                                val unitId = remember(currentLetter) {
                                    CurriculumRepository.findUnitIdForLetter(currentLetter)
                                }
                                if (unitId != null && onOpenLesson != null) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedButton(onClick = {
                                        speechEngine.stopListening()
                                        onDismiss()
                                        onOpenLesson(unitId)
                                    }) {
                                        Text("Full Practice Lesson 🎯")
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = speechStatusText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF49454F),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Mic & Next/Prev Controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (currentIndex > 0) {
                                        speechEngine.stopListening()
                                        currentIndex--
                                        speechStatusText = "Tap mic to speak '${distinctLetters[currentIndex]}'"
                                    }
                                },
                                enabled = currentIndex > 0
                            ) {
                                Text("Previous")
                            }

                            // Mic Button
                            Surface(
                                modifier = Modifier
                                    .size(76.dp)
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
                                    isCurrentPassed -> Color(0xFF2E7D32)
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
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    if (currentIndex < distinctLetters.size - 1) {
                                        speechEngine.stopListening()
                                        currentIndex++
                                        speechStatusText = "Tap mic to speak '${distinctLetters[currentIndex]}'"
                                    } else {
                                        onStartReading()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                            ) {
                                Text(if (currentIndex < distinctLetters.size - 1) "Next ➔" else "Start Reading")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                if (!passedLetters.contains(currentLetter)) {
                                    passedLetters.add(currentLetter)
                                }
                                speechStatusText = "Marked as correct!"
                                audioEngine.speakTamilText(currentLetter)
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("I said it! (Pass)")
                        }
                    }

                    ReviewMode.LETTERS_GRID -> {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Touch any letter to hear its sound or practise it:",
                                fontSize = 14.sp,
                                color = Color(0xFF49454F),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(4),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                itemsIndexed(distinctLetters) { idx, letter ->
                                    val isPassed = passedLetters.contains(letter)
                                    val translit = remember(letter) { TamilTransliteration.transliterateGrapheme(letter) }

                                    Card(
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .clickable {
                                                audioEngine.speakTamilText(letter)
                                                currentIndex = idx
                                            },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isPassed) Color(0xFFE8F5E9) else Color(0xFFEADDFF)
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = letter,
                                                fontSize = 26.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isPassed) Color(0xFF2E7D32) else Color(0xFF21005D)
                                            )
                                            if (translit.isNotBlank()) {
                                                Text(
                                                    text = translit,
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF6750A4)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = onStartReading,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                            ) {
                                Text("Start Reading Now ➔")
                            }
                        }
                    }
                }
            }
        }
    }
}
