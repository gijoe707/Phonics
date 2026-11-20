package com.example.phonics_ta_in.feature.curriculum

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.example.phonics_ta_in.core.speech.SpeechRecognizerEngine
import com.example.phonics_ta_in.core.speech.SpeechState

val ALL_VOCABULARY_WORDS = listOf(
    CurriculumRepository.WORD_AMMA,
    CurriculumRepository.WORD_AADU,
    CurriculumRepository.WORD_KAPPAL,
    CurriculumRepository.WORD_KADAL,
    CurriculumRepository.WORD_ANNAM,
    CurriculumRepository.WORD_AALAMARAM,
    CurriculumRepository.WORD_SAKKARAM
)

@Composable
fun VocabularySpeechScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioEngine = remember { AppAudioEngine(context) }

    var wordIndex by remember { mutableIntStateOf(0) }
    val currentWord = ALL_VOCABULARY_WORDS[wordIndex]

    var isListening by remember { mutableStateOf(false) }
    var speechStatusText by remember { mutableStateOf("Tap mic to speak '${currentWord.wordTamil}'") }
    var isPassed by remember { mutableStateOf(false) }

    val speechEngine = remember {
        SpeechRecognizerEngine(context) { speechState ->
            when (speechState) {
                is SpeechState.Listening -> {
                    isListening = true
                    speechStatusText = "Listening... Say '${currentWord.wordTamil}' now"
                }
                is SpeechState.Processing -> {
                    isListening = false
                    speechStatusText = "Evaluating speech..."
                }
                is SpeechState.Recognized -> {
                    isListening = false
                    val isAccurate = speechState.candidates.any { SpeechRecognizerEngine.isMatch(it, currentWord.wordTamil) }
                    if (isAccurate) {
                        isPassed = true
                        speechStatusText = "Great job! Recognized: '${currentWord.wordTamil}'"
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
            speechStatusText = "Microphone permission required for speech practice."
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF8FD))
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "வார்த்தை உச்சரிப்பு (Word Speech)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6750A4)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Word Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clickable { audioEngine.speakTamilText(currentWord.wordTamil) },
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isPassed) Color(0xFFE8F5E9) else Color(0xFFEADDFF)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF6750A4)
                ) {
                    Text(
                        text = "Word ${wordIndex + 1} of ${ALL_VOCABULARY_WORDS.size} | Starts with '${currentWord.startingLetter}'",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = currentWord.wordTamil,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPassed) Color(0xFF2E7D32) else Color(0xFF21005D)
                )

                if (currentWord.syllables.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Syllables: ${currentWord.syllables.joinToString(" - ")}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6750A4)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentWord.wordEnglish,
                    fontSize = 20.sp,
                    color = Color(0xFF49454F)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play word sound", tint = Color(0xFF6750A4))
                    Text("Tap to hear word", fontSize = 16.sp, color = Color(0xFF6750A4))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = speechStatusText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF49454F),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mic & Action controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = {
                    if (wordIndex > 0) {
                        wordIndex--
                        isPassed = false
                        speechStatusText = "Tap mic to speak '${ALL_VOCABULARY_WORDS[wordIndex].wordTamil}'"
                    }
                },
                enabled = wordIndex > 0
            ) {
                Text("Previous")
            }

            // Mic Button
            Surface(
                modifier = Modifier
                    .size(80.dp)
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
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Button(
                onClick = {
                    if (wordIndex < ALL_VOCABULARY_WORDS.size - 1) {
                        wordIndex++
                        isPassed = false
                        speechStatusText = "Tap mic to speak '${ALL_VOCABULARY_WORDS[wordIndex].wordTamil}'"
                    } else {
                        onBack()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
            ) {
                Text(if (wordIndex < ALL_VOCABULARY_WORDS.size - 1) "Next Word" else "Finish")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                isPassed = true
                speechStatusText = "Marked as spoken correctly!"
                audioEngine.speakTamilText(currentWord.wordTamil)
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("I said it! (Self Check)")
        }
    }
}
