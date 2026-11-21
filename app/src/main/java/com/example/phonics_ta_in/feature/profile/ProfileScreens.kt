package com.example.phonics_ta_in.feature.profile

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phonics_ta_in.core.profile.ChildProfile
import com.example.phonics_ta_in.core.profile.ProfileLogic
import com.example.phonics_ta_in.core.progress.ProgressLogic
import com.example.phonics_ta_in.core.progress.ProgressStore

private val PURPLE = Color(0xFF6750A4)

/**
 * "Who is learning?" page. Shown at launch when there are two or more profiles (or none yet,
 * in which case it invites the parent to create the first one).
 *
 * [onCancel] is only given when a profile is already active (the parent chose "Switch profile"),
 * so there is somewhere to go back to.
 */
@Composable
fun ProfileSelectScreen(
    profiles: List<ChildProfile>,
    onSelect: (String) -> Unit,
    onCreateFirst: (name: String, avatar: String) -> Unit,
    onCancel: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val today = ProgressLogic.today()
    // Each card shows that child's own stars and streak
    val progressById = remember(profiles) {
        profiles.associate { it.id to ProgressStore(context, it.id).load() }
    }
    var showCreate by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF8FD))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "யார் கற்கிறார்கள்?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = PURPLE
        )
        Text(
            text = "Who is learning today?",
            fontSize = 14.sp,
            color = Color(0xFF49454F)
        )
        Spacer(modifier = Modifier.height(20.dp))

        if (profiles.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEADDFF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("👋", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "வரவேற்கிறோம்!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF21005D)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Create a profile so each child's stars, streak and progress are saved separately.",
                        fontSize = 14.sp,
                        color = Color(0xFF49454F)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { showCreate = true }) {
                        Text("சுயவிவரத்தை உருவாக்கு (Create profile)")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(profiles, key = { it.id }) { profile ->
                    val state = progressById[profile.id]
                    ProfileCard(
                        profile = profile,
                        stars = state?.totalStars ?: 0,
                        streak = state?.let { ProgressLogic.displayStreak(it, today) } ?: 0,
                        onClick = { onSelect(profile.id) }
                    )
                }
            }
        }

        if (onCancel != null) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onCancel) {
                Text("திரும்பு (Back)")
            }
        }
    }

    if (showCreate) {
        ProfileEditorDialog(
            initial = null,
            existing = profiles,
            onSave = { name, avatar ->
                showCreate = false
                onCreateFirst(name, avatar)
            },
            onDismiss = { showCreate = false }
        )
    }
}

@Composable
private fun ProfileCard(
    profile: ChildProfile,
    stars: Int,
    streak: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = Color(0xFFEADDFF)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(profile.avatar, fontSize = 44.sp)
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = profile.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D1B20),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFFFE0B2)) {
                    Text(
                        text = "🔥 $streak",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFEADDFF)) {
                    Text(
                        text = "⭐ $stars",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF21005D),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Create a profile ([initial] = null) or edit one. Names must be unique; Save stays disabled
 * until the name is valid.
 */
@Composable
fun ProfileEditorDialog(
    initial: ChildProfile?,
    existing: List<ChildProfile>,
    onSave: (name: String, avatar: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var avatar by remember { mutableStateOf(initial?.avatar ?: ProfileLogic.AVATARS.first()) }
    val error = ProfileLogic.validateName(name, existing, initial?.id)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (initial == null) "புதிய சுயவிவரம் (New profile)" else "சுயவிவரத்தைத் திருத்து (Edit profile)",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(ProfileLogic.MAX_NAME_LENGTH) },
                    singleLine = true,
                    label = { Text("பெயர் (Child's name)") },
                    isError = error == ProfileLogic.NameError.DUPLICATE,
                    supportingText = {
                        if (error == ProfileLogic.NameError.DUPLICATE) {
                            Text("This name is already used by another profile")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("படம் (Picture)", fontSize = 13.sp, color = Color(0xFF49454F))
                    ProfileLogic.AVATARS.chunked(4).forEach { rowAvatars ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowAvatars.forEach { emoji ->
                                val selected = emoji == avatar
                                Surface(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clickable { avatar = emoji },
                                    shape = CircleShape,
                                    color = if (selected) Color(0xFFEADDFF) else Color(0xFFF3EDF7),
                                    border = if (selected) BorderStroke(2.dp, PURPLE) else null
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(emoji, fontSize = 28.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name.trim(), avatar) },
                enabled = error == null
            ) {
                Text("சேமி (Save)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ரத்து (Cancel)")
            }
        }
    )
}
