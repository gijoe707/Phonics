package com.example.phonics_ta_in.feature.readaloud

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.phonics_ta_in.core.audio.AppAudioEngine
import com.example.phonics_ta_in.core.curriculum.CurriculumRepository
import com.example.phonics_ta_in.core.text.TamilSyllableSegmenter
import com.example.phonics_ta_in.core.text.TamilTextSegmenter
import com.example.phonics_ta_in.core.text.TamilTransliteration
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** The previous sentence stays on screen this long, so the burst around its last word can finish. */
private const val SENTENCE_SWITCH_DELAY_MS = 1000L
private const val COMPLETION_DELAY_MS = 1100L
private const val BURST_DURATION_MS = 800f

private val GREEN_TEXT = Color(0xFF2E7D32)
private val GREEN_BACKGROUND = Color(0xFFE3F5E5)
private val YELLOW_BACKGROUND = Color(0xFFFFEE58)
private val YELLOW_TEXT = Color(0xFF3E2723)

private val BURST_COLORS = listOf(
    Color(0xFFE91E63), Color(0xFFFFC107), Color(0xFF4CAF50),
    Color(0xFF2196F3), Color(0xFF9C27B0), Color(0xFFFF5722)
)
private const val BURST_PARTICLES = 14

/**
 * The reading screen shared by the built-in story and by pasted text.
 *
 * One tap on the microphone and the child just reads: words turn green (with a short party burst around them) as
 * they are read; a word said wrongly turns yellow while the helper says it, and the microphone comes back by itself.
 * Nothing is announced aloud when a word is right or wrong.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReadAlongPlayer(
    sentences: List<ReadAloudSentence>,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    wordFontSize: TextUnit = 18.sp,
    onOpenLesson: ((String) -> Unit)? = null,
    initialPosition: Int = 0,
    onPositionChanged: ((Int) -> Unit)? = null
) {
    if (sentences.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Nothing to read yet.", color = Color(0xFF49454F))
        }
        return
    }

    val context = LocalContext.current
    val audioEngine = remember { AppAudioEngine(context) }
    val controller = remember(sentences) {
        ReadAlongController(context, audioEngine, sentences, initialPosition, onPositionChanged)
    }

    DisposableEffect(controller) {
        onDispose { controller.release() }
    }
    DisposableEffect(audioEngine) {
        onDispose { audioEngine.release() }
    }

    // The microphone is closed as soon as the screen leaves the foreground.
    val lifecycleOwner = remember(context) { context.findLifecycleOwner() }
    DisposableEffect(lifecycleOwner, controller) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) controller.stopMic()
        }
        lifecycleOwner?.lifecycle?.addObserver(observer)
        onDispose { lifecycleOwner?.lifecycle?.removeObserver(observer) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            controller.startMic()
        } else {
            controller.showMessage("The microphone is needed to listen. You can still tap \"I said it!\"")
        }
    }

    // Show the sentence being read; switch a moment after it is complete so its last burst is seen.
    var shownSentence by remember(controller) { mutableIntStateOf(0) }
    LaunchedEffect(controller, controller.currentSentence) {
        val target = controller.currentSentence
        if (target < shownSentence) {
            shownSentence = target
        } else if (target > shownSentence) {
            delay(SENTENCE_SWITCH_DELAY_MS)
            shownSentence = target
        }
    }

    var showCompletion by remember(controller) { mutableStateOf(false) }
    LaunchedEffect(controller, controller.finished) {
        if (controller.finished) {
            delay(COMPLETION_DELAY_MS)
            showCompletion = true
        }
    }

    if (showCompletion) {
        CompletionCard(
            onReadAgain = {
                controller.reset()
                shownSentence = 0
                showCompletion = false
            },
            onFinished = onFinished,
            modifier = modifier
        )
        return
    }

    val groupSize = 3
    val currentBlock = shownSentence / groupSize
    val startSentenceIndex = currentBlock * groupSize
    val endSentenceIndex = minOf(startSentenceIndex + groupSize - 1, sentences.lastIndex)

    val done = controller.statuses.count { it == WordStatus.CORRECT || it == WordStatus.SKIPPED }

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (sentences.size <= 3) "All ${sentences.size} Sentences" else "Sentences ${startSentenceIndex + 1}-${endSentenceIndex + 1} of ${sentences.size}",
                            color = Color(0xFF79747E),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = {
                            for (sIdx in startSentenceIndex..endSentenceIndex) {
                                controller.readSentenceAloud(sIdx)
                            }
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Hear these sentences",
                                tint = Color(0xFF6750A4)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val blockSentences = (startSentenceIndex..endSentenceIndex).map { sentences[it] }
                    val blockFirstWordIndex = controller.startIndexOf(startSentenceIndex)

                    key(currentBlock) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                var wordOffset = blockFirstWordIndex
                                blockSentences.forEach { s ->
                                    s.words.forEach { word ->
                                        val global = wordOffset
                                        wordOffset++
                                        ReadAlongWordChip(
                                            word = word,
                                            status = controller.statuses[global],
                                            isNext = global == controller.position,
                                            isDemo = global == controller.demoWord,
                                            fontSize = wordFontSize,
                                            onClick = { controller.onWordTapped(global) }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // English translations for the block
                            blockSentences.forEach { s ->
                                if (s.englishTranslation.isNotBlank()) {
                                    Text(
                                        text = "• ${s.englishTranslation}",
                                        color = Color(0xFF79747E),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = { done.toFloat() / controller.words.size.coerceAtLeast(1) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = Color(0xFF6750A4),
                trackColor = Color(0xFFE7E0EC)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = controller.message,
                color = Color(0xFF49454F),
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            val micColor = when (controller.micState) {
                MicState.OFF -> Color(0xFF6750A4)
                MicState.LISTENING -> Color(0xFFC62828)
                MicState.HELPING -> Color(0xFFF9A825)
            }
            Surface(
                modifier = Modifier
                    .size(84.dp)
                    .clickable {
                        if (controller.micState != MicState.OFF) {
                            controller.stopMic()
                        } else if (
                            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                            PackageManager.PERMISSION_GRANTED
                        ) {
                            controller.startMic()
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                shape = CircleShape,
                color = micColor,
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = if (controller.micState == MicState.OFF) "Start listening" else "Stop listening",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(onClick = { controller.markCurrentWordRead() }) {
                Text("I said it!")
            }
        }
    }

    val activeBreakdown = controller.activeBreakdownWord
    if (activeBreakdown != null) {
        WordBreakdownDialog(
            state = activeBreakdown,
            onPlayPart = { text -> controller.playSoundPart(text) },
            onPlaySyllables = { syls -> controller.playSyllablesSequence(syls) },
            onSwitchMode = { mode -> controller.setBreakdownMode(mode) },
            onOpenLesson = onOpenLesson,
            onDismiss = { controller.closeBreakdownDialog() }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WordBreakdownDialog(
    state: BreakdownWordState,
    onPlayPart: (String) -> Unit,
    onPlaySyllables: (List<String>) -> Unit,
    onSwitchMode: (BreakdownMode) -> Unit,
    onOpenLesson: ((String) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val graphemes = remember(state.word) { TamilTextSegmenter.segmentGraphemes(state.word) }
    val syllables = remember(state.word) { TamilSyllableSegmenter.segmentSyllables(state.word) }
    val transliteration = remember(graphemes) { TamilTransliteration.transliterateWord(graphemes) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Row: Word & Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.word,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D1B20)
                        )
                        if (transliteration.isNotBlank()) {
                            Text(
                                text = transliteration,
                                fontSize = 16.sp,
                                color = Color(0xFF79747E)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF49454F))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Attempt Badge & Subtitle
                val attemptText = when (state.misses) {
                    1 -> "1st Attempt: Tap a letter to hear its sound"
                    2 -> "2nd Attempt: Tap a syllable to hear its sound"
                    else -> "Full Word Mode"
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFFF9C4)
                ) {
                    Text(
                        text = attemptText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF5D4037),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mode Selector Tabs (Letters | Syllables | Full Word)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF3EDF7), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val modes = listOf(
                        BreakdownMode.LETTERS to "Letters",
                        BreakdownMode.SYLLABLES to "Syllables",
                        BreakdownMode.FULL_WORD to "Full Word"
                    )
                    modes.forEach { (mode, label) ->
                        val isSelected = state.mode == mode
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFF6750A4) else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSwitchMode(mode) }
                        ) {
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF49454F),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Content area according to selected mode
                when (state.mode) {
                    BreakdownMode.LETTERS -> {
                        Text(
                            text = "Tap any letter below to hear its individual sound:",
                            fontSize = 14.sp,
                            color = Color(0xFF49454F),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            graphemes.forEach { g ->
                                val unitId = remember(g.text) { CurriculumRepository.findUnitIdForLetter(g.text) }
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFFEADDFF),
                                    shadowElevation = 2.dp,
                                    modifier = Modifier.clickable { onPlayPart(g.text) }
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                    ) {
                                        Text(
                                            text = g.text,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF21005D)
                                        )
                                        if (g.transliteration.isNotBlank()) {
                                            Text(
                                                text = g.transliteration,
                                                fontSize = 12.sp,
                                                color = Color(0xFF6750A4)
                                            )
                                        }
                                        if (unitId != null && onOpenLesson != null) {
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFF6750A4),
                                                modifier = Modifier.clickable {
                                                    onDismiss()
                                                    onOpenLesson(unitId)
                                                }
                                            ) {
                                                Text(
                                                    text = "Practice 🎯",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    BreakdownMode.SYLLABLES -> {
                        Text(
                            text = "Tap a syllable to hear its sound:",
                            fontSize = 14.sp,
                            color = Color(0xFF49454F),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            syllables.forEach { syl ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFFD0BCFF),
                                    shadowElevation = 2.dp,
                                    modifier = Modifier.clickable { onPlayPart(syl) }
                                ) {
                                    Text(
                                        text = syl,
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF381E72),
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                                    )
                                }
                            }
                        }
                        if (syllables.size > 1) {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { onPlaySyllables(syllables) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Listen to all syllables")
                            }
                        }
                    }

                    BreakdownMode.FULL_WORD -> {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFE8DEF8),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPlayPart(state.word) }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Text(
                                    text = state.word,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D192B)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = null,
                                        tint = Color(0xFF6750A4)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Tap to listen to full word",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF6750A4)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletionCard(
    onReadAgain: () -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🎉", fontSize = 56.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Well done!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = GREEN_TEXT
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row {
                    OutlinedButton(onClick = onReadAgain) { Text("Read again") }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = onFinished,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                    ) { Text("Finish") }
                }
            }
        }
    }
}

/**
 * One word: grey/purple while waiting, green (with a short burst of confetti around it) once read correctly,
 * yellow while it needs another try. Tap it to hear it.
 */
@Composable
private fun ReadAlongWordChip(
    word: String,
    status: WordStatus,
    isNext: Boolean,
    isDemo: Boolean,
    fontSize: TextUnit,
    onClick: () -> Unit
) {
    // 0 -> 1 while the burst plays; 1 means "not playing".
    var burst by remember { mutableFloatStateOf(1f) }
    var celebrated by remember { mutableStateOf(false) }
    LaunchedEffect(status) {
        if (status == WordStatus.CORRECT && !celebrated) {
            celebrated = true
            burst = 0f
            val start = withFrameNanos { it }
            while (true) {
                val t = (withFrameNanos { it } - start) / 1_000_000f / BURST_DURATION_MS
                if (t >= 1f) break
                burst = 1f - (1f - t) * (1f - t) // ease out
            }
            burst = 1f
        }
    }

    val background: Color
    val textColor: Color
    when {
        status == WordStatus.WRONG -> {
            background = YELLOW_BACKGROUND
            textColor = YELLOW_TEXT
        }
        status == WordStatus.CORRECT -> {
            background = GREEN_BACKGROUND
            textColor = GREEN_TEXT
        }
        status == WordStatus.SKIPPED -> {
            background = Color.Transparent
            textColor = Color(0xFF79747E)
        }
        isDemo || isNext -> {
            background = Color(0xFFEADDFF)
            textColor = Color(0xFF21005D)
        }
        else -> {
            background = Color(0xFFF3EDF7)
            textColor = Color(0xFF1D1B20)
        }
    }

    Box(contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            color = background,
            shadowElevation = if (isNext && status == WordStatus.PENDING) 2.dp else 0.dp
        ) {
            Text(
                text = word,
                fontSize = fontSize,
                fontWeight = if (status == WordStatus.CORRECT || status == WordStatus.WRONG) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        PartyBurst(progress = burst, modifier = Modifier.matchParentSize())
    }
}

/** Confetti flying outwards from the edge of the word. [progress] 0..1; nothing is drawn outside that range. */
@Composable
private fun PartyBurst(progress: Float, modifier: Modifier = Modifier) {
    if (progress <= 0f || progress >= 1f) return
    Canvas(modifier = modifier) {
        val halfWidth = size.width / 2f
        val halfHeight = size.height / 2f
        val reach = 20.dp.toPx() * progress
        val fade = 1f - progress
        for (i in 0 until BURST_PARTICLES) {
            val angle = (2.0 * PI * i / BURST_PARTICLES).toFloat()
            val distance = if (i % 2 == 0) reach else reach * 0.6f
            val x = halfWidth + cos(angle) * (halfWidth + distance)
            val y = halfHeight + sin(angle) * (halfHeight + distance)
            val color = BURST_COLORS[i % BURST_COLORS.size].copy(alpha = fade)
            val shrink = 1f - 0.4f * progress
            if (i % 3 == 0) {
                val side = 6.dp.toPx() * shrink
                drawRect(color = color, topLeft = Offset(x - side / 2f, y - side / 2f), size = Size(side, side))
            } else {
                drawCircle(color = color, radius = 3.5f.dp.toPx() * shrink, center = Offset(x, y))
            }
        }
    }
}

private tailrec fun Context.findLifecycleOwner(): LifecycleOwner? = when (this) {
    is LifecycleOwner -> this
    is ContextWrapper -> baseContext.findLifecycleOwner()
    else -> null
}
