package com.example.phonics_ta_in.feature.parent

import android.content.Intent
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.phonics_ta_in.core.curriculum.CurriculumRepository
import com.example.phonics_ta_in.core.profile.ChildProfile

@Composable
fun ParentDashboardScreen(
    onBack: () -> Unit,
    onOpenLesson: (String) -> Unit,
    completedUnitIds: Set<String>,
    minutesLearnedToday: Int,
    dailyGoalMinutes: Int,
    onDailyGoalChange: (Int) -> Unit,
    currentStreak: Int,
    weeklyMinutes: List<Int> = List(7) { 0 },
    musicEnabled: Boolean = true,
    onMusicEnabledChange: (Boolean) -> Unit = {},
    profiles: List<ChildProfile> = emptyList(),
    activeProfileId: String? = null,
    onCreateProfile: (name: String, avatar: String) -> Unit = { _, _ -> },
    onEditProfile: (id: String, name: String, avatar: String) -> Unit = { _, _, _ -> },
    onDeleteProfile: (id: String) -> Unit = {},
    onSwitchProfile: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeProfile = profiles.firstOrNull { it.id == activeProfileId }
    val profileName = activeProfile?.name ?: "—"

    // Generate mastery grid dynamically based on actual completedUnitIds
    val allMasteryGrid = remember(completedUnitIds) { buildMasteryGrid(completedUnitIds) }

    val weakSounds = remember(allMasteryGrid) {
        allMasteryGrid.filter { it.status == MasteryStatus.NEEDS_PRACTICE }.take(8)
    }

    val totalMastered = completedUnitIds.size.coerceAtMost(CurriculumRepository.ALL_UNITS.size)
    val totalUnits = CurriculumRepository.ALL_UNITS.size
    // Real completion of the course (the app does not track per-answer accuracy yet)
    val completionPercent = if (totalUnits == 0) 0 else (totalMastered * 100) / totalUnits

    var heatMapFilter by remember { mutableStateOf("Needs Practice") }
    var showReportExportDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFF7F5FA)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header: Back, Title & Export
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
                            text = "பெற்றோர் பகுப்பாய்வு",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6750A4)
                        )
                        Text(
                            text = "Live Analytics Dashboard",
                            fontSize = 12.sp,
                            color = Color(0xFF79747E)
                        )
                    }
                }

                IconButton(onClick = { showReportExportDialog = true }) {
                    Icon(
                        Icons.Filled.Share,
                        contentDescription = "Export Report",
                        tint = Color(0xFF6750A4)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Dashboard Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Child Profile Card & Streak Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(52.dp),
                                shape = CircleShape,
                                color = Color(0xFFEADDFF)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(activeProfile?.avatar ?: "👶", fontSize = 28.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = profileName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D1B20)
                                )
                                Text(
                                    text = "Active Student Profile",
                                    fontSize = 13.sp,
                                    color = Color(0xFF49454F)
                                )
                            }
                        }

                        // Streak Badge
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFFE0B2)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🔥", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$currentStreak Days",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE65100),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                // Create / edit / delete / switch child profiles
                ProfilesCard(
                    profiles = profiles,
                    activeProfileId = activeProfileId,
                    onCreate = onCreateProfile,
                    onEdit = onEditProfile,
                    onDelete = onDeleteProfile,
                    onSwitch = onSwitchProfile
                )

                // Metric Summary Grid Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Letters Mastered Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "கற்ற எழுத்துகள்",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = "Mastered",
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "$totalMastered / $totalUnits",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B5E20)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { if (totalUnits == 0) 0f else totalMastered.toFloat() / totalUnits.toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = Color(0xFF2E7D32),
                                trackColor = Color(0xFFA5D6A7)
                            )
                        }
                    }

                    // Weak Sounds Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "கவனிக்க வேண்டும்",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC62828)
                                )
                                Icon(
                                    Icons.Filled.Warning,
                                    contentDescription = "Needs Practice",
                                    tint = Color(0xFFC62828),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${weakSounds.size} Sounds",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB71C1C)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Needs Focus",
                                fontSize = 12.sp,
                                color = Color(0xFFC62828),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Daily Learning Time Line Chart (Weekly Practice Stats)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "இன்றைய பயிற்சி நேரம் (Daily Practice)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D1B20)
                                )
                                Text(
                                    text = "$minutesLearnedToday / $dailyGoalMinutes Minutes Today",
                                    fontSize = 13.sp,
                                    color = Color(0xFF6750A4),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFEADDFF)
                            ) {
                                Text(
                                    text = "$completionPercent% Complete",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF21005D),
                                    modifier = Modifier.padding(
                                        horizontal = 10.dp,
                                        vertical = 6.dp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Weekly Bar Chart Graphic
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                            val todayIndex = java.time.LocalDate.now().dayOfWeek.value - 1
                            val weeklyDays = dayNames.mapIndexed { index, name ->
                                Triple(name, weeklyMinutes.getOrElse(index) { 0 }, index == todayIndex)
                            }
                            val maxMin = (weeklyDays.maxOfOrNull { it.second } ?: 15).coerceAtLeast(15)

                            weeklyDays.forEach { (day, mins, isToday) ->
                                val barHeightFraction = mins.toFloat() / maxMin.toFloat()
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (mins > 0) {
                                        Text(
                                            text = "${mins}m",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF49454F)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .width(20.dp)
                                            .height((70 * barHeightFraction).dp.coerceAtLeast(6.dp))
                                            .background(
                                                if (isToday) Color(0xFF6750A4)
                                                else if (mins > 0) Color(0xFFD0BCFF)
                                                else Color(0xFFE7E0EC),
                                                RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                            )
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = day,
                                        fontSize = 12.sp,
                                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isToday) Color(0xFF6750A4) else Color(
                                            0xFF79747E
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Priority Weak Sounds Focus Section ("Needs Practice / கவனிக்க வேண்டிய ஒலிகள்")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "கவனிக்க வேண்டிய ஒலிகள் (Weak Sounds Priority)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D1B20)
                        )
                        Text(
                            text = "Tap 'Practice 🎯' to start targeted lesson for weak sound:",
                            fontSize = 12.sp,
                            color = Color(0xFF49454F)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (weakSounds.isEmpty()) {
                            Text(
                                "All unlocked sounds are mastered! Great job!",
                                fontSize = 14.sp,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                weakSounds.forEach { item ->
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color(0xFFFFF0F0),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            Color(0xFFFFCDD2)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    modifier = Modifier.size(44.dp),
                                                    shape = CircleShape,
                                                    color = Color(0xFFD32F2F)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = item.letter,
                                                            fontSize = 20.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = "Sound: /${item.transliteration}/",
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF1D1B20)
                                                    )
                                                    Text(
                                                        text = "Status: Needs Practice",
                                                        fontSize = 12.sp,
                                                        color = Color(0xFFD32F2F),
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }

                                            Button(
                                                onClick = { onOpenLesson(item.unitId) },
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                                                modifier = Modifier.height(36.dp)
                                            ) {
                                                Text(
                                                    "Practice 🎯",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 247-Letter Mastery Heat Map Grid
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "எழுத்துகளின் தேர்ச்சி வரைபடம் (Letter Mastery Heat Map)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D1B20)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Filter Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val options = listOf("Needs Practice", "Mastered", "All")
                            items(options) { filter ->
                                FilterChip(
                                    selected = (heatMapFilter == filter),
                                    onClick = { heatMapFilter = filter },
                                    label = { Text(filter, fontSize = 12.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Filtered Heat Map Items
                        val filteredList = remember(heatMapFilter, allMasteryGrid) {
                            when (heatMapFilter) {
                                "Needs Practice" -> allMasteryGrid.filter { it.status == MasteryStatus.NEEDS_PRACTICE }
                                "Mastered" -> allMasteryGrid.filter { it.status == MasteryStatus.MASTERED }
                                else -> allMasteryGrid
                            }
                        }

                        // Compact Heat Map Grid
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            filteredList.take(48).chunked(6).forEach { row ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    row.forEach { item ->
                                        val bg = when (item.status) {
                                            MasteryStatus.MASTERED -> Color(0xFFC8E6C9)
                                            else -> Color(0xFFFFCDD2)
                                        }
                                        val textClr = when (item.status) {
                                            MasteryStatus.MASTERED -> Color(0xFF1B5E20)
                                            else -> Color(0xFFB71C1C)
                                        }

                                        Surface(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(44.dp)
                                                .clickable { onOpenLesson(item.unitId) },
                                            shape = RoundedCornerShape(10.dp),
                                            color = bg
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = item.letter,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = textClr
                                                )
                                            }
                                        }
                                    }
                                    repeat(6 - row.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                // Daily Goal Settings Card (Updates Main Activity goal in real-time)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "தினசரி இலக்கு அமைப்புகள் (Daily Goal Settings)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D1B20)
                        )
                        Text(
                            text = "Set daily learning time goal (updates main screen timer):",
                            fontSize = 12.sp,
                            color = Color(0xFF49454F)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(5, 10, 15, 20).forEach { mins ->
                                val isSelected = (dailyGoalMinutes == mins)
                                Button(
                                    onClick = { onDailyGoalChange(mins) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) Color(0xFF6750A4) else Color(
                                            0xFFF3EDF7
                                        ),
                                        contentColor = if (isSelected) Color.White else Color(
                                            0xFF1D1B20
                                        )
                                    )
                                ) {
                                    Text(
                                        "${mins}m",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Background music: a parent setting that stays as last set
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "பின்னணி இசை (Background Music)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D1B20)
                            )
                            Text(
                                text = "Plays softly on the main page. Your choice is remembered.",
                                fontSize = 12.sp,
                                color = Color(0xFF49454F)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = musicEnabled,
                            onCheckedChange = onMusicEnabledChange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Export Progress Report Dialog
    if (showReportExportDialog) {
        val reportText = """
            📊 Phonics-ta-IN Child Progress Report
            ------------------------------------
            Student Profile: $profileName
            Current Streak: $currentStreak Days
            Letters Mastered: $totalMastered / $totalUnits
            Course Completion: $completionPercent%
            Minutes Learned Today: $minutesLearnedToday / $dailyGoalMinutes min
        """.trimIndent()

        AlertDialog(
            onDismissRequest = { showReportExportDialog = false },
            title = {
                Text(
                    "அறிக்கை பகிர்தல் (Export Report)",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(reportText, fontSize = 13.sp)
            },
            confirmButton = {
                Button(onClick = {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, reportText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Progress Report"))
                    showReportExportDialog = false
                }) {
                    Text("பகிர் (Share Report)")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportExportDialog = false }) {
                    Text("மூடு (Close)")
                }
            }
        )
    }
}
