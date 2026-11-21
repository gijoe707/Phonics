package com.example.phonics_ta_in.feature.parent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phonics_ta_in.core.profile.ChildProfile
import com.example.phonics_ta_in.feature.profile.ProfileEditorDialog

/**
 * Parent section card for managing child profiles: create, edit, delete, and switch.
 * Each profile keeps its own stars, streak and lesson progress.
 */
@Composable
fun ProfilesCard(
    profiles: List<ChildProfile>,
    activeProfileId: String?,
    onCreate: (name: String, avatar: String) -> Unit,
    onEdit: (id: String, name: String, avatar: String) -> Unit,
    onDelete: (id: String) -> Unit,
    onSwitch: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showCreate by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<ChildProfile?>(null) }
    var deleting by remember { mutableStateOf<ChildProfile?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "சுயவிவரங்கள் (Profiles)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1D1B20)
            )
            Text(
                text = "Each child has their own stars, streak and progress.",
                fontSize = 12.sp,
                color = Color(0xFF49454F)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                profiles.forEach { profile ->
                    val isActive = profile.id == activeProfileId
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isActive) Color(0xFFF3EDF7) else Color(0xFFFAFAFA)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, top = 6.dp, bottom = 6.dp, end = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = Color(0xFFEADDFF)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(profile.avatar, fontSize = 22.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = profile.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D1B20),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (isActive) {
                                    Text(
                                        text = "Active now",
                                        fontSize = 12.sp,
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            IconButton(onClick = { editing = profile }) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = "Edit ${profile.name}",
                                    tint = Color(0xFF6750A4)
                                )
                            }
                            IconButton(onClick = { deleting = profile }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Delete ${profile.name}",
                                    tint = Color(0xFFC62828)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showCreate = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("சேர் (Add)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                if (profiles.size > 1) {
                    OutlinedButton(
                        onClick = onSwitch,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("மாற்று (Switch)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showCreate) {
        ProfileEditorDialog(
            initial = null,
            existing = profiles,
            onSave = { name, avatar ->
                showCreate = false
                onCreate(name, avatar)
            },
            onDismiss = { showCreate = false }
        )
    }

    editing?.let { profile ->
        ProfileEditorDialog(
            initial = profile,
            existing = profiles,
            onSave = { name, avatar ->
                editing = null
                onEdit(profile.id, name, avatar)
            },
            onDismiss = { editing = null }
        )
    }

    deleting?.let { profile ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = {
                Text("நீக்கு (Delete) ${profile.name}?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "${profile.name}'s stars, streak and lesson progress will be erased. This cannot be undone.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        deleting = null
                        onDelete(profile.id)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                ) {
                    Text("நீக்கு (Delete)")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleting = null }) {
                    Text("ரத்து (Cancel)")
                }
            }
        )
    }
}
