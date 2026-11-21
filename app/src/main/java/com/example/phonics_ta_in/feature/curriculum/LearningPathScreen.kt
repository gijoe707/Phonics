package com.example.phonics_ta_in.feature.curriculum

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LearningPathScreen(
    currentStars: Int,
    currentStreak: Int,
    vowelsCompleted: Boolean = false,
    consonantsCompleted: Boolean = false,
    onOpenVowels: () -> Unit,
    onOpenConsonants: () -> Unit,
    onStartReadAloud: () -> Unit,
    onStartCustomReader: () -> Unit,
    onStartVocabularySpeech: () -> Unit,
    onStartTestKnowledge: () -> Unit,
    onOpenParentDashboard: () -> Unit,
    onOpenLicenses: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF8FD))
            .padding(16.dp)
    ) {
        // Top Header: Streak, XP & Daily Goal
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Streak counter
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFFE0B2)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔥", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("$currentStreak Days", fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                }
            }

            // Title
            Text(
                text = "தமிழ் கற்போம்",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6750A4)
            )

            // XP counter
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFEADDFF)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⭐", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("$currentStars", fontWeight = FontWeight.Bold, color = Color(0xFF21005D))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Learning Path",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1D1B20)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Path Nodes List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Vowels page: 12 uyir + ஃ
            item {
                PathCard(
                    title = "உயிர் எழுத்துகள்",
                    subtitle = "Vowels - 12 sounds + ஃ",
                    icon = "அ",
                    cardColor = Color(0xFFEADDFF),
                    iconBackground = Color(0xFF6750A4),
                    playTint = Color(0xFF6750A4),
                    playDescription = "Open vowels",
                    isCompleted = vowelsCompleted,
                    onClick = onOpenVowels
                )
            }

            // Consonants page: 18 consonants, each expands to 13 sounds (18 x 13 = 234)
            item {
                PathCard(
                    title = "மெய் எழுத்துகள்",
                    subtitle = "Consonants - 18 letters x 13 sounds each",
                    icon = "க",
                    cardColor = Color(0xFFD6EEF8),
                    iconBackground = Color(0xFF0D3B66),
                    playTint = Color(0xFF0D3B66),
                    playDescription = "Open consonants",
                    isCompleted = consonantsCompleted,
                    onClick = onOpenConsonants
                )
            }

            // Word Vocabulary Practice
            item {
                PathCard(
                    title = "வார்த்தை உச்சரிப்பு",
                    subtitle = "Everyday Vocabulary Hub & Speech",
                    icon = "📖",
                    cardColor = Color(0xFFE3F2FD),
                    iconBackground = Color(0xFF1565C0),
                    playTint = Color(0xFF1565C0),
                    playDescription = "Start Vocabulary Speech",
                    onClick = onStartVocabularySpeech
                )
            }

            // Read-Aloud Stories
            item {
                PathCard(
                    title = "கதை வாசித்தல்",
                    subtitle = "Grade 1-3 Stories with Read-Along",
                    icon = "🗣️",
                    cardColor = Color(0xFFEADDFF),
                    iconBackground = Color(0xFF21005D),
                    playTint = Color(0xFF6750A4),
                    playDescription = "Start Stories Read Aloud",
                    onClick = onStartReadAloud
                )
            }

            // Test Your Knowledge Card (Speech + Meaning across Levels 1 to 7+ Letter Words)
            item {
                PathCard(
                    title = "அறிவு சோதனை",
                    subtitle = "Test Speech & Meaning (Levels 1-7+)",
                    icon = "🎯",
                    cardColor = Color(0xFFF3E5F5),
                    iconBackground = Color(0xFF7B1FA2),
                    playTint = Color(0xFF7B1FA2),
                    playDescription = "Start Knowledge Test",
                    onClick = onStartTestKnowledge
                )
            }

            // Read your own pasted text
            item {
                PathCard(
                    title = "உரை வாசிப்பு",
                    subtitle = "Paste your own text and read along",
                    icon = "📝",
                    cardColor = Color(0xFFFFE8D6),
                    iconBackground = Color(0xFFBF360C),
                    playTint = Color(0xFFBF360C),
                    playDescription = "Start reading your own text",
                    onClick = onStartCustomReader
                )
            }

            // Parent / Teacher Analytics Dashboard
            item {
                PathCard(
                    title = "பெற்றோர் பகுதி",
                    subtitle = "Analytics, PIN Gate & Progress Heat Map",
                    icon = "📊",
                    cardColor = Color(0xFFE8F5E9),
                    iconBackground = Color(0xFF2E7D32),
                    playTint = Color(0xFF2E7D32),
                    playDescription = "Open Parent Dashboard",
                    onClick = onOpenParentDashboard
                )
            }

            // Licenses & credits (background music)
            item {
                PathCard(
                    title = "உரிமங்கள்",
                    subtitle = "Licenses & music credits",
                    icon = "©",
                    cardColor = Color(0xFFECEFF1),
                    iconBackground = Color(0xFF455A64),
                    playTint = Color(0xFF455A64),
                    playDescription = "Open licenses",
                    onClick = onOpenLicenses
                )
            }
        }
    }
}

@Composable
private fun PathCard(
    title: String,
    subtitle: String,
    icon: String,
    cardColor: Color,
    iconBackground: Color,
    playTint: Color,
    playDescription: String,
    onClick: () -> Unit,
    isCompleted: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = iconBackground
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = icon,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D1B20)
                        )
                        if (isCompleted) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                modifier = Modifier.size(22.dp),
                                shape = CircleShape,
                                color = Color(0xFF2E7D32)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("✓", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        color = Color(0xFF49454F)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = playDescription,
                        tint = playTint
                    )
                }
            }
        }
    }
}
