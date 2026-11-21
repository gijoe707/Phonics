package com.example.phonics_ta_in.ui.nav

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phonics_ta_in.AppScreen

private val PURPLE = Color(0xFF6750A4)

/**
 * Slim bar shown on every screen: the hamburger button on the left and, on the main page,
 * the music mute button on the right.
 */
@Composable
fun AppTopBar(
    onMenuClick: () -> Unit,
    showMenu: Boolean = true,
    showMute: Boolean,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFFFBF8FD))
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(48.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showMenu) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Filled.Menu, contentDescription = "Open menu", tint = PURPLE)
            }
        } else {
            // Keep the bar's height and the mute button on the right
            Spacer(modifier = Modifier.width(48.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
        if (showMute) {
            IconButton(onClick = onToggleMute) {
                Icon(
                    imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff
                    else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = if (isMuted) "Unmute music" else "Mute music",
                    tint = PURPLE
                )
            }
        }
    }
}

/**
 * The slide-out menu. Sections are an accordion (one open at a time); tapping an entry
 * jumps straight to that screen.
 */
@Composable
fun AppDrawerContent(
    currentScreen: AppScreen,
    profileName: String? = null,
    profileAvatar: String? = null,
    onNavigate: (AppScreen) -> Unit
) {
    val highlighted = AppMenu.highlightFor(currentScreen)
    var expandedId by remember { mutableStateOf(AppMenu.sectionIdFor(currentScreen)) }
    // Open the section that holds the page the child is on
    LaunchedEffect(currentScreen) { expandedId = AppMenu.sectionIdFor(currentScreen) }

    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp)
        ) {
            Text(
                text = "தமிழ் கற்போம்",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PURPLE,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
            )

            if (profileName != null) {
                Text(
                    text = "${profileAvatar.orEmpty()} $profileName".trim(),
                    fontSize = 14.sp,
                    color = Color(0xFF49454F),
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp)
                )
            }

            NavigationDrawerItem(
                label = { DestinationLabel(AppMenu.home.titleTamil, AppMenu.home.titleEnglish) },
                selected = highlighted == AppMenu.home.screen,
                onClick = { onNavigate(AppMenu.home.screen) },
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            AppMenu.sections.forEach { section ->
                val expanded = expandedId == section.id
                SectionHeader(
                    icon = section.icon,
                    titleTamil = section.titleTamil,
                    titleEnglish = section.titleEnglish,
                    expanded = expanded,
                    onClick = { expandedId = if (expanded) null else section.id }
                )
                AnimatedVisibility(visible = expanded) {
                    Column {
                        section.items.forEach { item ->
                            NavigationDrawerItem(
                                label = { DestinationLabel(item.titleTamil, item.titleEnglish) },
                                selected = highlighted == item.screen,
                                onClick = { onNavigate(item.screen) },
                                modifier = Modifier.padding(start = 32.dp, end = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    icon: String,
    titleTamil: String,
    titleEnglish: String,
    expanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(titleTamil, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1D1B20))
            Text(titleEnglish, fontSize = 12.sp, color = Color(0xFF49454F))
        }
        Icon(
            imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
            contentDescription = if (expanded) "Collapse $titleEnglish" else "Expand $titleEnglish",
            tint = PURPLE
        )
    }
}

@Composable
private fun DestinationLabel(titleTamil: String, titleEnglish: String) {
    Column {
        Text(titleTamil, fontSize = 15.sp, fontWeight = FontWeight.Medium)
        Text(titleEnglish, fontSize = 12.sp, color = Color(0xFF49454F))
    }
}
