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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mic
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
import com.example.phonics_ta_in.core.speech.SpeechRecognizerEngine
import com.example.phonics_ta_in.core.speech.SpeechState

data class VocabularyCategory(
    val id: String,
    val titleTamil: String,
    val titleEnglish: String,
    val iconEmoji: String,
    val description: String,
    val items: List<VocabularyItem>
)

data class VocabularyItem(
    val tamil: String,
    val english: String,
    val transliteration: String,
    val emoji: String
)

val VOCABULARY_CATEGORIES = listOf(
    VocabularyCategory(
        id = "numbers",
        titleTamil = "எண்கள்",
        titleEnglish = "Numbers (1 - 10)",
        iconEmoji = "🔢",
        description = "Learn counting and numbers in Tamil",
        items = listOf(
            VocabularyItem("ஒன்று", "One", "onru", "1️⃣"),
            VocabularyItem("இரண்டு", "Two", "irandu", "2️⃣"),
            VocabularyItem("மூன்று", "Three", "moondru", "3️⃣"),
            VocabularyItem("நான்கு", "Four", "naanku", "4️⃣"),
            VocabularyItem("ஐந்து", "Five", "ainthu", "5️⃣"),
            VocabularyItem("ஆறு", "Six", "aaru", "6️⃣"),
            VocabularyItem("ஏழு", "Seven", "eezhu", "7️⃣"),
            VocabularyItem("எட்டு", "Eight", "ettu", "8️⃣"),
            VocabularyItem("ஒன்பது", "Nine", "onpathu", "9️⃣"),
            VocabularyItem("பத்து", "Ten", "paththu", "🔟")
        )
    ),
    VocabularyCategory(
        id = "days",
        titleTamil = "வாரத்தின் நாட்கள்",
        titleEnglish = "Days of the Week",
        iconEmoji = "📅",
        description = "The 7 days of the week in Tamil",
        items = listOf(
            VocabularyItem("ஞாயிறு", "Sunday", "gnayiru", "☀️"),
            VocabularyItem("திங்கள்", "Monday", "thingal", "🌙"),
            VocabularyItem("செவ்வாய்", "Tuesday", "sevvai", "⚡"),
            VocabularyItem("புதன்", "Wednesday", "puthan", "🌱"),
            VocabularyItem("வியாழன்", "Thursday", "viyazhan", "🪐"),
            VocabularyItem("வெள்ளி", "Friday", "velli", "✨"),
            VocabularyItem("சனி", "Saturday", "sani", "🪐")
        )
    ),
    VocabularyCategory(
        id = "months",
        titleTamil = "தமிழ் மாதங்கள்",
        titleEnglish = "Tamil Months",
        iconEmoji = "🗓️",
        description = "The 12 traditional Tamil months",
        items = listOf(
            VocabularyItem("சித்திரை", "Chithirai (Apr-May)", "chithirai", "🌸"),
            VocabularyItem("வைகாசி", "Vaigasi (May-Jun)", "vaigasi", "☀️"),
            VocabularyItem("ஆனி", "Aani (Jun-Jul)", "aani", "🍃"),
            VocabularyItem("ஆடி", "Aadi (Jul-Aug)", "aadi", "🌧️"),
            VocabularyItem("ஆவணி", "Avani (Aug-Sep)", "avani", "🌾"),
            VocabularyItem("புரட்டாசி", "Purattasi (Sep-Oct)", "purattasi", "🍂"),
            VocabularyItem("ஐப்பசி", "Aippasi (Oct-Nov)", "aippasi", "⛅"),
            VocabularyItem("கார்த்திகை", "Karthigai (Nov-Dec)", "karthigai", "🪔"),
            VocabularyItem("மார்கழி", "Margazhi (Dec-Jan)", "margazhi", "❄️"),
            VocabularyItem("தை", "Thai (Jan-Feb)", "thai", "🌾"),
            VocabularyItem("மாசி", "Masi (Feb-Mar)", "masi", "🌼"),
            VocabularyItem("பங்குனி", "Panguni (Mar-Apr)", "panguni", "🌺")
        )
    ),
    VocabularyCategory(
        id = "body_parts",
        titleTamil = "உடலின் உறுப்புகள்",
        titleEnglish = "Body Parts",
        iconEmoji = "👤",
        description = "Common parts of the human body",
        items = listOf(
            VocabularyItem("தலை", "Head", "thalai", "🗣️"),
            VocabularyItem("கண்", "Eye", "kan", "👁️"),
            VocabularyItem("மூக்கு", "Nose", "mookku", "👃"),
            VocabularyItem("வாய்", "Mouth", "vay", "👄"),
            VocabularyItem("காது", "Ear", "kathu", "👂"),
            VocabularyItem("கை", "Hand", "kai", "✋"),
            VocabularyItem("கால்", "Leg", "kal", "🦵")
        )
    ),
    VocabularyCategory(
        id = "family",
        titleTamil = "உறவுகள்",
        titleEnglish = "Family Members",
        iconEmoji = "👨‍👩‍👧‍👦",
        description = "Members of the family",
        items = listOf(
            VocabularyItem("அம்மா", "Mother", "amma", "👩"),
            VocabularyItem("அப்பா", "Father", "appa", "👨"),
            VocabularyItem("தாத்தா", "Grandfather", "thatha", "👴"),
            VocabularyItem("பாட்டி", "Grandmother", "patti", "👵"),
            VocabularyItem("அண்ணா", "Elder Brother", "anna", "👦"),
            VocabularyItem("அக்கா", "Elder Sister", "akka", "👧"),
            VocabularyItem("தம்பி", "Younger Brother", "thambi", "🧒"),
            VocabularyItem("தங்கை", "Younger Sister", "thangai", "👧")
        )
    ),
    VocabularyCategory(
        id = "animals",
        titleTamil = "விலங்குகள் & பறவைகள்",
        titleEnglish = "Animals & Birds",
        iconEmoji = "🐅",
        description = "Common animals and birds",
        items = listOf(
            VocabularyItem("சிங்கம்", "Lion", "singam", "🦁"),
            VocabularyItem("புலி", "Tiger", "puli", "🐅"),
            VocabularyItem("யானை", "Elephant", "yanai", "🐘"),
            VocabularyItem("நாய்", "Dog", "nay", "🐕"),
            VocabularyItem("பூனை", "Cat", "poonai", "🐈"),
            VocabularyItem("காகம்", "Crow", "kagam", "🐦"),
            VocabularyItem("கிளி", "Parrot", "kili", "🦜"),
            VocabularyItem("மயில்", "Peacock", "mayil", "🦚")
        )
    ),
    VocabularyCategory(
        id = "colors",
        titleTamil = "நிறங்கள்",
        titleEnglish = "Colors",
        iconEmoji = "🎨",
        description = "Colors in Tamil",
        items = listOf(
            VocabularyItem("சிவப்பு", "Red", "sivappu", "🔴"),
            VocabularyItem("நீலம்", "Blue", "neelam", "🔵"),
            VocabularyItem("பச்சை", "Green", "pachchai", "🟢"),
            VocabularyItem("மஞ்சள்", "Yellow", "manjal", "🟡"),
            VocabularyItem("கருப்பு", "Black", "karuppu", "⚫"),
            VocabularyItem("வெள்ளை", "White", "vellai", "⚪")
        )
    ),
    VocabularyCategory(
        id = "flowers",
        titleTamil = "மலர்கள்",
        titleEnglish = "Flowers",
        iconEmoji = "🌺",
        description = "Flowers in Tamil",
        items = listOf(
            VocabularyItem("தாமரை", "Lotus", "thamarai", "🪷"),
            VocabularyItem("மல்லிகை", "Jasmine", "malligai", "🌼"),
            VocabularyItem("ரோஜா", "Rose", "roja", "🌹"),
            VocabularyItem("சாமந்தி", "Marigold", "samanthi", "🌻")
        )
    ),
    VocabularyCategory(
        id = "greetings",
        titleTamil = "வணக்கம் & வாழ்த்துகள்",
        titleEnglish = "Greetings & Polite Phrases",
        iconEmoji = "🤝",
        description = "Everyday polite expressions and greetings",
        items = listOf(
            VocabularyItem("வணக்கம்", "Hello / Greetings", "vanakkam", "🙏"),
            VocabularyItem("நன்றி", "Thank you", "nandri", "🤝"),
            VocabularyItem("மன்னிக்கவும்", "Sorry / Excuse me", "mannikkavum", "🙇"),
            VocabularyItem("எப்படி இருக்கிறீர்கள்?", "How are you?", "eppadi irukkireergal?", "👋"),
            VocabularyItem("மீண்டும் சந்திப்போம்", "See you again", "meendum sandhippom", "✨")
        )
    ),
    VocabularyCategory(
        id = "phrases",
        titleTamil = "சொலவடைகள் & பழமொழிகள்",
        titleEnglish = "Wise Sayings & Proverbs",
        iconEmoji = "💡",
        description = "Traditional Tamil wisdom and proverbs",
        items = listOf(
            VocabularyItem("முயற்சி திருவினையாக்கும்", "Perseverance brings success", "muyarchi thiruvinaiyakkum", "🎯"),
            VocabularyItem("அளவுக்கு மிஞ்சினால் அமிர்தமும் நஞ்சு", "Too much of anything is bad", "alavukku minjinal amirthamum nanju", "⚖️"),
            VocabularyItem("அகத்தி தின்னா பகடு அறியும்", "Experience teaches hard lessons", "agathi thinna pagadu ariyum", "📖")
        )
    )
)

@Composable
fun VocabularySpeechScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioEngine = remember { AppAudioEngine(context) }
    var selectedCategory by remember { mutableStateOf<VocabularyCategory?>(null) }

    var activeItemIndex by remember { mutableIntStateOf(0) }
    var isListening by remember { mutableStateOf(false) }
    var speechStatusText by remember { mutableStateOf("") }
    var isPassed by remember { mutableStateOf(false) }

    val currentCategory = selectedCategory
    val currentItem = currentCategory?.items?.getOrNull(activeItemIndex)

    val speechEngine = remember(currentItem) {
        if (currentItem == null) null else {
            SpeechRecognizerEngine(context) { speechState ->
                when (speechState) {
                    is SpeechState.Listening -> {
                        isListening = true
                        speechStatusText = "Listening... Say '${currentItem.tamil}'"
                    }
                    is SpeechState.Processing -> {
                        isListening = false
                        speechStatusText = "Evaluating speech..."
                    }
                    is SpeechState.Recognized -> {
                        isListening = false
                        val matched = speechState.candidates.any {
                            SpeechRecognizerEngine.isMatch(it, currentItem.tamil) || it.contains(currentItem.tamil)
                        }
                        if (matched) {
                            isPassed = true
                            speechStatusText = "Great job! Recognized: '${currentItem.tamil}'"
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
    }

    DisposableEffect(Unit) {
        onDispose {
            audioEngine.release()
            speechEngine?.destroy()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            speechEngine?.startListening()
        } else {
            speechStatusText = "Microphone permission required."
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
            IconButton(onClick = {
                if (selectedCategory != null) {
                    selectedCategory = null
                    speechEngine?.destroy()
                } else {
                    onBack()
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentCategory?.titleTamil ?: "சொற்களஞ்சியம் (Vocabulary Hub)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6750A4)
                )
                Text(
                    text = currentCategory?.titleEnglish ?: "Explore everyday Tamil topics & practice speech",
                    fontSize = 13.sp,
                    color = Color(0xFF49454F)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (currentCategory == null) {
            // Category Grid Hub
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(VOCABULARY_CATEGORIES, key = { it.id }) { cat ->
                    CategoryCard(
                        category = cat,
                        onClick = {
                            selectedCategory = cat
                            activeItemIndex = 0
                            isPassed = false
                            speechStatusText = "Tap mic to speak '${cat.items.first().tamil}'"
                        }
                    )
                }
            }
        } else if (currentItem != null) {
            // Active Item Practice Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clickable { audioEngine.speakTamilText(currentItem.tamil) },
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF6750A4)
                        ) {
                            Text(
                                text = "${currentItem.emoji} Item ${activeItemIndex + 1} of ${currentCategory.items.size}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                        if (isPassed) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    val tamilFontSize = if (currentCategory.id == "phrases" || currentItem.tamil.length > 12) 24.sp else 48.sp
                    Text(
                        text = currentItem.tamil,
                        fontSize = tamilFontSize,
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = if (isPassed) Color(0xFF2E7D32) else Color(0xFF21005D)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentItem.transliteration,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF6750A4)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentItem.english,
                        fontSize = 18.sp,
                        color = Color(0xFF49454F)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = Color(0xFF6750A4))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tap to hear pronunciation", fontSize = 15.sp, color = Color(0xFF6750A4))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = speechStatusText.ifBlank { "Tap mic to speak '${currentItem.tamil}'" },
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF49454F),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Controls Row: Prev, Mic, Next
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        if (activeItemIndex > 0) {
                            speechEngine?.stopListening()
                            activeItemIndex--
                            isPassed = false
                            speechStatusText = "Tap mic to speak '${currentCategory.items[activeItemIndex].tamil}'"
                        }
                    },
                    enabled = activeItemIndex > 0
                ) {
                    Text("Previous")
                }

                Surface(
                    modifier = Modifier
                        .size(80.dp)
                        .clickable {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            if (isListening) {
                                speechEngine?.stopListening()
                            } else if (hasPermission) {
                                speechEngine?.startListening()
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
                        if (activeItemIndex < currentCategory.items.size - 1) {
                            speechEngine?.stopListening()
                            activeItemIndex++
                            isPassed = false
                            speechStatusText = "Tap mic to speak '${currentCategory.items[activeItemIndex].tamil}'"
                        } else {
                            selectedCategory = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                ) {
                    Text(if (activeItemIndex < currentCategory.items.size - 1) "Next ➔" else "Finish")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    isPassed = true
                    speechStatusText = "Marked as spoken correctly!"
                    audioEngine.speakTamilText(currentItem.tamil)
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("I said it! (Self Check)")
            }
        }
    }
}

@Composable
private fun CategoryCard(
    category: VocabularyCategory,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = category.iconEmoji, fontSize = 40.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = category.titleTamil,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF21005D),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${category.items.size} words",
                fontSize = 12.sp,
                color = Color(0xFF79747E)
            )
        }
    }
}
