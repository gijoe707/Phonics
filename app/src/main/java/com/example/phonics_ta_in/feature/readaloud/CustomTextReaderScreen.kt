package com.example.phonics_ta_in.feature.readaloud

import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Paste any text, then read it aloud with the same hands-free read-along as the built-in story.
 */
@Composable
fun CustomTextReaderScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenLesson: ((String) -> Unit)? = null,
    draftText: String = "",
    onDraftTextChange: (String) -> Unit = {},
    readingText: String? = null,
    onReadingTextChange: (String?) -> Unit = {},
    readingPosition: Int = 0,
    onReadingPositionChange: (Int) -> Unit = {}
) {
    val context = LocalContext.current
    var showReviewDialog by remember { mutableStateOf(false) }

    val draftSentences = remember(draftText) { ReadAlongText.toSentences(draftText) }
    val readingSentences = remember(readingText) {
        readingText?.let { ReadAlongText.toSentences(it) } ?: emptyList()
    }
    val isReading = readingText != null && readingSentences.isNotEmpty()

    // Back from reading returns to the text, not out of the screen.
    BackHandler(enabled = isReading) {
        onReadingTextChange(null)
        onReadingPositionChange(0)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFBF8FD))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (isReading) {
                    onReadingTextChange(null)
                    onReadingPositionChange(0)
                } else {
                    onBack()
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Read Your Own Text",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6750A4),
                modifier = Modifier.weight(1f)
            )
            if (isReading) {
                IconButton(onClick = {
                    onReadingTextChange(null)
                    onReadingPositionChange(0)
                }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit text", tint = Color(0xFF6750A4))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isReading) {
            ReadAlongPlayer(
                sentences = readingSentences,
                onFinished = {
                    onReadingTextChange(null)
                    onReadingPositionChange(0)
                },
                wordFontSize = 18.sp,
                onOpenLesson = onOpenLesson,
                initialPosition = readingPosition,
                onPositionChanged = onReadingPositionChange,
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Paste or type the text you want to practise. The app listens while you read it aloud.",
                    color = Color(0xFF49454F),
                    fontSize = 15.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = draftText,
                    onValueChange = onDraftTextChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 220.dp),
                    textStyle = TextStyle(fontSize = 20.sp),
                    placeholder = { Text("Paste your text here") },
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { clipboardText(context)?.let { onDraftTextChange(it) } },
                        modifier = Modifier.weight(1f)
                    ) { Text("Paste") }
                    OutlinedButton(
                        onClick = { onDraftTextChange("") },
                        enabled = draftText.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) { Text("Clear") }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val wordCount = draftSentences.sumOf { it.words.size }
                Text(
                    text = if (draftSentences.isEmpty()) {
                        "Nothing to read yet"
                    } else {
                        "$wordCount words in ${draftSentences.size} " +
                            if (draftSentences.size == 1) "part" else "parts"
                    },
                    color = Color(0xFF79747E),
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val distinct = extractDistinctLetters(draftText)
                        if (distinct.isNotEmpty()) {
                            showReviewDialog = true
                        } else {
                            onReadingPositionChange(0)
                            onReadingTextChange(draftText)
                        }
                    },
                    enabled = draftSentences.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                ) { Text("Start reading") }
            }
        }
    }

    if (showReviewDialog) {
        val distinct = remember(draftText) { extractDistinctLetters(draftText) }
        LetterSoundReviewDialog(
            distinctLetters = distinct,
            onStartReading = {
                showReviewDialog = false
                onReadingPositionChange(0)
                onReadingTextChange(draftText)
            },
            onOpenLesson = onOpenLesson,
            onDismiss = {
                showReviewDialog = false
            }
        )
    }
}

/** The text currently on the clipboard, or null. */
private fun clipboardText(context: Context): String? {
    val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return null
    val clip = manager.primaryClip ?: return null
    if (clip.itemCount == 0) return null
    return clip.getItemAt(0).coerceToText(context)?.toString()?.takeIf { it.isNotBlank() }
}
