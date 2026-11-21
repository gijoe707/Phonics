package com.example.phonics_ta_in.feature.curriculum

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.phonics_ta_in.core.audio.AppAudioEngine
import com.example.phonics_ta_in.core.curriculum.DictionaryWord
import com.example.phonics_ta_in.core.curriculum.TamilDictionaryRepository
import com.example.phonics_ta_in.core.speech.SpeechRecognizerEngine
import com.example.phonics_ta_in.core.speech.SpeechState

/**
 * Single Activity / Screen for testing word knowledge in ONE continuous test session.
 * Dynamically picks a few random words from each difficulty level (Level 1..7+).
 * Speech Pronunciation and Meaning Comprehension are shown SIMULTANEOUSLY on the card.
 */
@Composable
fun TestYourKnowledgeScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Audio Engine
    val audioEngine = remember { AppAudioEngine(context) }
    DisposableEffect(Unit) {
        onDispose { audioEngine.release() }
    }

    // Permission state for speech recognition
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasMicPermission = isGranted
    }

    // Dynamic Continuous Test Set (picking random words from each difficulty level 1 to 7)
    var testWords by remember {
        mutableStateOf(TamilDictionaryRepository.generateRandomTestSet(wordsPerLevel = 2))
    }

    var currentWordIndex by remember { mutableIntStateOf(0) }
    val currentWord = testWords.getOrNull(currentWordIndex) ?: testWords.first()

    // Test status states for current word
    var speechState by remember { mutableStateOf<SpeechState>(SpeechState.Idle) }
    var speechPassed by remember { mutableStateOf(false) }

    // Speech Engine instance
    var speechEngine by remember { mutableStateOf<SpeechRecognizerEngine?>(null) }
    DisposableEffect(currentWord, hasMicPermission) {
        if (hasMicPermission) {
            val engine = SpeechRecognizerEngine(
                context = context,
                isolatedSound = false,
                biasingStrings = listOf(currentWord.wordTamil),
                onStateChanged = { newState ->
                    speechState = newState
                    if (newState is SpeechState.Recognized) {
                        val matchesTarget = newState.candidates.any { candidate ->
                            candidate.trim().equals(currentWord.wordTamil.trim(), ignoreCase = true) ||
                                    candidate.contains(currentWord.wordTamil)
                        }
                        if (matchesTarget || newState.confidence > 0.5f) {
                            speechPassed = true
                            audioEngine.speakTamilText("மிகச் சிறப்பு!")
                        }
                    }
                }
            )
            speechEngine = engine
        }
        onDispose {
            speechEngine?.stopListening()
            speechEngine = null
        }
    }

    // Meaning Test Choices (1 correct + 3 distractors)
    val meaningChoices = remember(currentWord) {
        val distractors = TamilDictionaryRepository.getDistractorsForWord(currentWord, 3)
        (listOf(currentWord) + distractors).shuffled()
    }
    var selectedMeaningChoice by remember { mutableStateOf<DictionaryWord?>(null) }
    var meaningPassed by remember { mutableStateOf(false) }

    // Score & Progress State
    var totalStars by remember { mutableIntStateOf(0) }
    var isTestComplete by remember { mutableStateOf(false) }

    // Helper to advance to next word or complete test session
    fun advanceToNextWord() {
        speechState = SpeechState.Idle
        speechPassed = false
        selectedMeaningChoice = null
        meaningPassed = false

        if (currentWordIndex < testWords.lastIndex) {
            currentWordIndex++
        } else {
            isTestComplete = true
            audioEngine.speakTamilText("வாழ்த்துகள்! தொடர் சோதனையை வெற்றிகரமாக நிறைவு செய்தீர்கள்!")
        }
    }

    fun restartTestWithNewRandomWords() {
        testWords = TamilDictionaryRepository.generateRandomTestSet(wordsPerLevel = 2)
        currentWordIndex = 0
        speechState = SpeechState.Idle
        speechPassed = false
        selectedMeaningChoice = null
        meaningPassed = false
        isTestComplete = false
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFF7F5FA)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header: Back Button, Title & Stars Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF1D1B20)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "அறிவு சோதனை",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6750A4)
                        )
                        Text(
                            text = "Continuous Random Word Challenge",
                            fontSize = 12.sp,
                            color = Color(0xFF79747E)
                        )
                    }
                }

                // Star Counter
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFFF8E1),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = "Stars",
                            tint = Color(0xFFFFB300),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$totalStars Stars",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Overall Progress Indicator Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "கேள்வி ${currentWordIndex + 1} / ${testWords.size}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF6750A4)
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEADDFF)
                        ) {
                            Text(
                                text = "Level ${currentWord.difficultyLevel} (${currentWord.graphemeCount} Letter Word)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF21005D),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (currentWordIndex + 1).toFloat() / testWords.size.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = Color(0xFF6750A4),
                        trackColor = Color(0xFFE7E0EC)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Main Test Activity Container (Scrollable)
            if (isTestComplete) {
                // Test Session Completion View
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
                        Text(text = "🏆", fontSize = 52.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "சோதனை நிறைவு பெற்றது!",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF21005D),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "அனைத்து நிலைகளின் (Level 1-7+) ரேண்டம் வார்த்தைகளையும் வெற்றிகரமாகப் பயிற்சி செய்தீர்கள்! ஈட்டிய நட்ச்சத்திரங்கள்: $totalStars ⭐️",
                            fontSize = 14.sp,
                            color = Color(0xFF49454F),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { restartTestWithNewRandomWords() },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Restart")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "புதிய ரேண்டம் சோதனை (New Random Test)",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            } else {
                // Active Question View: Both Speech and Meaning rendered simultaneously!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Target Word Display Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (currentWord.illustration.isNotBlank()) {
                                WordIllustration(
                                    key = currentWord.illustration,
                                    modifier = Modifier.size(90.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = currentWord.wordTamil,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D1B20)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                IconButton(
                                    onClick = {
                                        audioEngine.speakTamilText(currentWord.wordTamil)
                                    },
                                    modifier = Modifier
                                        .background(Color(0xFFEADDFF), CircleShape)
                                        .size(40.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Listen to pronunciation",
                                        tint = Color(0xFF6750A4)
                                    )
                                }
                            }

                            Text(
                                text = "Transliteration: /${currentWord.transliteration}/",
                                fontSize = 14.sp,
                                color = Color(0xFF6650a4),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // 1. SPEECH TEST SECTION (Always visible)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (speechPassed) Color(0xFFE8F5E9) else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "1. உச்சரிப்பு சோதனை (Speech Test)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (speechPassed) Color(0xFF2E7D32) else Color(0xFF1D1B20)
                                )
                                if (speechPassed) {
                                    Icon(
                                        Icons.Filled.CheckCircle,
                                        contentDescription = "Passed",
                                        tint = Color(0xFF2E7D32)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "மைக் அழுத்தி '${currentWord.wordTamil}' என்று பேசவும்:",
                                fontSize = 14.sp,
                                color = Color(0xFF49454F)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        if (!hasMicPermission) {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        } else {
                                            audioEngine.stop()
                                            speechState = SpeechState.Listening
                                            speechEngine?.startListening()
                                        }
                                    },
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(
                                            if (speechState is SpeechState.Listening) Color(0xFFE53935)
                                            else if (speechPassed) Color(0xFF2E7D32)
                                            else Color(0xFF6750A4),
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        Icons.Filled.Mic,
                                        contentDescription = "Pronounce Word",
                                        tint = Color.White,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            val statusText = when (speechState) {
                                is SpeechState.Listening -> "கேட்கிறது... பேசவும்!"
                                is SpeechState.Processing -> "சரிபார்க்கிறது..."
                                is SpeechState.Recognized -> {
                                    val r = speechState as SpeechState.Recognized
                                    if (speechPassed) "மிகச் சிறப்பு! (Recognized: ${r.topMatch})"
                                    else "கேட்டது: ${r.topMatch}. மீண்டும் முயலவும்."
                                }
                                is SpeechState.Error -> (speechState as SpeechState.Error).message
                                else -> if (speechPassed) "உச்சரிப்பு ஏற்றுக்கொள்ளப்பட்டது!" else "மைக் அழுத்திப் பேசவும்"
                            }

                            Text(
                                text = statusText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (speechPassed) Color(0xFF2E7D32) else Color(0xFF6750A4),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (!speechPassed) {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = {
                                        speechPassed = true
                                        totalStars += 1
                                        audioEngine.speakTamilText("சரியாக உச்சரித்ததாக ஏற்றுக்கொள்ளப்பட்டது!")
                                    },
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("சரியாகப் பேசினேன் (Self Check)", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    // 2. MEANING TEST SECTION (Always visible!)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (meaningPassed) Color(0xFFE8F5E9) else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "2. பொருள் அறிதல் (Meaning Test)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (meaningPassed) Color(0xFF2E7D32) else Color(0xFF1D1B20)
                                )
                                if (meaningPassed) {
                                    Icon(
                                        Icons.Filled.CheckCircle,
                                        contentDescription = "Passed",
                                        tint = Color(0xFF2E7D32)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "'${currentWord.wordTamil}' என்பதன் சரியான பொருள் எது?",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D1B20)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Multiple Choice Options
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                meaningChoices.forEach { option ->
                                    val isSelected = (selectedMeaningChoice == option)
                                    val isCorrect = (option.wordTamil == currentWord.wordTamil)

                                    val containerBg = when {
                                        meaningPassed && isCorrect -> Color(0xFFC8E6C9)
                                        isSelected && !isCorrect -> Color(0xFFFFCDD2)
                                        isSelected -> Color(0xFFEADDFF)
                                        else -> Color(0xFFF5F5F5)
                                    }

                                    val borderStroke = when {
                                        meaningPassed && isCorrect -> Color(0xFF2E7D32)
                                        isSelected && !isCorrect -> Color(0xFFD32F2F)
                                        else -> Color.Transparent
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable(enabled = !meaningPassed) {
                                                selectedMeaningChoice = option
                                                if (isCorrect) {
                                                    meaningPassed = true
                                                    totalStars += 2
                                                    audioEngine.speakTamilText("சரியான பொருள்!")
                                                } else {
                                                    audioEngine.speakTamilText("தவறு. மீண்டும் முயல்க.")
                                                }
                                            }
                                            .border(2.dp, borderStroke, RoundedCornerShape(16.dp)),
                                        shape = RoundedCornerShape(16.dp),
                                        color = containerBg
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = option.meaningTamil,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1D1B20)
                                                )
                                                Text(
                                                    text = option.meaningEnglish,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF49454F)
                                                )
                                            }
                                            if (meaningPassed && isCorrect) {
                                                Icon(
                                                    Icons.Filled.CheckCircle,
                                                    contentDescription = "Correct",
                                                    tint = Color(0xFF2E7D32)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // NEXT WORD BUTTON (Enabled when meaning is answered or speech & meaning are completed)
                    if (meaningPassed || speechPassed) {
                        Button(
                            onClick = { advanceToNextWord() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                        ) {
                            Text(
                                text = if (currentWordIndex < testWords.lastIndex) "அடுத்த வார்த்தை (Next Word ➔)" else "சோதனையை நிறைவு செய் (Finish Test)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
