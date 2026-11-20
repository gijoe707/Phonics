package com.example.phonics_ta_in.feature.curriculum

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phonics_ta_in.core.curriculum.StartingSoundWord
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Small picture card for an example word: drawn picture + Tamil word + English meaning.
 * Tap to hear the word.
 */
@Composable
fun WordPracticeCard(
    word: StartingSoundWord,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(104.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(Color(0xFFF3EDF7), RoundedCornerShape(18.dp))
                .padding(6.dp)
        ) {
            WordIllustration(key = word.illustration, modifier = Modifier.fillMaxSize())
        }
        Text(
            text = word.wordTamil,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF21005D),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = word.wordEnglish,
            fontSize = 12.sp,
            color = Color(0xFF49454F),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Vector pictures drawn with Canvas primitives (no image assets).
 * Every picture is drawn in a 100 x 100 design space that is scaled to fit the available size.
 */
@Composable
fun WordIllustration(key: String, modifier: Modifier = Modifier.size(80.dp)) {
    Canvas(modifier = modifier) {
        val s = size.minDimension / 100f
        withTransform({ scale(s, s, pivot = Offset.Zero) }) {
            when (key) {
                "mother_baby" -> drawMotherBaby()
                "swan" -> drawSwan()
                "squirrel" -> drawSquirrel()
                "arrow" -> drawArrow()
                "goat" -> drawGoat()
                "banyan_tree" -> drawBanyanTree()
                "tortoise" -> drawTortoise()
                "owl" -> drawOwl()
                "apple" -> drawApple()
                "wheel" -> drawWheel()
                "crow" -> drawCrow()
                "crane" -> drawCrane()
                "sea" -> drawSea()
                "ship" -> drawShip()
                "eye" -> drawEye()
                "bear" -> drawBear()
                "tree" -> drawBanyanTree()
                "river" -> drawSea()
                "leaf" -> drawLeaf()
                "idli" -> drawIdli()
                "fly" -> drawFly()
                "spear" -> drawSpear()
                "mortar" -> drawMortar()
                "piggy_bank" -> drawPiggyBank()
                "swing" -> drawSwing()
                "needle" -> drawNeedle()
                "ant" -> drawAnt()
                "mouse" -> drawMouse()
                "ladder" -> drawLadder()
                "five" -> drawFive()
                "camel" -> drawCamel()
                "boat" -> drawBoat()
                "avvaiyar" -> drawAvvaiyar()
                "conch" -> drawConch()
                "gold" -> drawGold()
                "shirt" -> drawShirt()
                "sun" -> drawSun()
                "kite" -> drawKite()
                "circle" -> drawRing()
                "bell" -> drawBell()
                "coin" -> drawCoins()
                "frog" -> drawFrog()
                "plate" -> drawPlate()
                "snail" -> drawSnail()
                "star" -> drawStar()
                "ball" -> drawBall()
                "mountain" -> drawMountain()
                "elephant" -> drawElephant()
                "stone" -> drawStone()
                "cart" -> drawCart()
                "rain" -> drawRain()
                "parrot" -> drawParrot()
                "bangle" -> drawBangles()
                "fish" -> drawFish()
                "pot" -> drawPot()
                else -> fillCircle(Color(0xFFD0BCFF), 50f, 50f, 30f)
            }
        }
    }
}

// ───────────────────────── drawing helpers (100x100 space) ─────────────────────────

private val SKIN = Color(0xFFF1B98A)
private val INK = Color(0xFF2B2B2B)
private val WATER = Color(0xFF64B5F6)
private val GROUND = Color(0xFF81C784)

private fun DrawScope.fillCircle(c: Color, cx: Float, cy: Float, r: Float) =
    drawCircle(c, r, Offset(cx, cy))

private fun DrawScope.fillOval(c: Color, l: Float, t: Float, w: Float, h: Float) =
    drawOval(c, Offset(l, t), Size(w, h))

private fun DrawScope.fillRect(c: Color, l: Float, t: Float, w: Float, h: Float) =
    drawRect(c, Offset(l, t), Size(w, h))

private fun DrawScope.strokeLine(c: Color, x1: Float, y1: Float, x2: Float, y2: Float, w: Float) =
    drawLine(c, Offset(x1, y1), Offset(x2, y2), strokeWidth = w, cap = StrokeCap.Round)

/** Filled polygon from x,y pairs. */
private fun DrawScope.fillPoly(c: Color, vararg p: Float) {
    val path = Path()
    path.moveTo(p[0], p[1])
    var i = 2
    while (i < p.size - 1) {
        path.lineTo(p[i], p[i + 1])
        i += 2
    }
    path.close()
    drawPath(path, c)
}

private fun DrawScope.waves(c: Color, top: Float) {
    val path = Path()
    path.moveTo(0f, top)
    var x = 0f
    while (x < 100f) {
        path.quadraticTo(x + 10f, top - 8f, x + 20f, top)
        x += 20f
    }
    path.lineTo(100f, 100f)
    path.lineTo(0f, 100f)
    path.close()
    drawPath(path, c)
}

// ───────────────────────── அ words ─────────────────────────

/** அம்மா - lady holding a baby */
private fun DrawScope.drawMotherBaby() {
    // dress / saree
    fillPoly(Color(0xFFD81B60), 22f, 38f, 40f, 38f, 50f, 94f, 12f, 94f)
    // hair + face
    fillCircle(INK, 31f, 22f, 13f)
    fillCircle(SKIN, 31f, 25f, 10f)
    fillCircle(Color(0xFFE53935), 31f, 20f, 1.8f) // bindi
    fillCircle(INK, 27.5f, 25f, 1.2f)
    fillCircle(INK, 34.5f, 25f, 1.2f)
    drawArc(INK, 20f, 140f, false, Offset(27f, 25f), Size(8f, 6f), style = Stroke(1.2f))
    // arm holding baby
    strokeLine(SKIN, 36f, 52f, 58f, 62f, 7f)
    // baby bundle + head
    fillOval(Color(0xFFFFD54F), 46f, 54f, 30f, 22f)
    fillCircle(SKIN, 72f, 52f, 9f)
    fillCircle(INK, 69f, 51f, 1.3f)
    fillCircle(INK, 75f, 51f, 1.3f)
    fillCircle(Color(0xFFE57373), 72f, 56f, 1.6f)
}

/** அன்னம் - swan on water */
private fun DrawScope.drawSwan() {
    val neck = Path().apply {
        moveTo(66f, 58f)
        quadraticTo(84f, 46f, 70f, 24f)
    }
    drawPath(neck, Color(0xFF9E9E9E), style = Stroke(11f, cap = StrokeCap.Round))
    drawPath(neck, Color.White, style = Stroke(8f, cap = StrokeCap.Round))
    fillPoly(Color.White, 24f, 58f, 8f, 44f, 26f, 66f)
    fillOval(Color(0xFFBDBDBD), 18f, 50f, 56f, 32f)
    fillOval(Color.White, 20f, 50f, 54f, 30f)
    fillCircle(Color.White, 70f, 22f, 8f)
    fillPoly(Color(0xFFFF9800), 75f, 20f, 92f, 25f, 75f, 28f)
    fillCircle(INK, 71f, 20f, 1.6f)
    waves(WATER, 84f)
}

/** அணில் - squirrel with big tail */
private fun DrawScope.drawSquirrel() {
    fillOval(Color(0xFFBF6B3C), 58f, 14f, 30f, 62f) // tail
    fillOval(Color(0xFFD9895A), 63f, 22f, 18f, 46f)
    fillOval(Color(0xFF8D5524), 28f, 44f, 38f, 42f) // body
    fillOval(Color(0xFFFFE0B2), 36f, 56f, 20f, 26f) // belly
    fillCircle(Color(0xFF8D5524), 38f, 36f, 14f)    // head
    fillPoly(Color(0xFF8D5524), 28f, 30f, 28f, 14f, 38f, 24f) // ears
    fillPoly(Color(0xFF8D5524), 40f, 24f, 48f, 12f, 50f, 28f)
    fillCircle(Color.White, 33f, 35f, 3.6f)
    fillCircle(INK, 33f, 35f, 1.8f)
    fillCircle(INK, 25f, 40f, 2.2f) // nose
    fillRect(GROUND, 0f, 90f, 100f, 10f)
}

/** அம்பு - arrow */
private fun DrawScope.drawArrow() {
    strokeLine(Color(0xFF8D6E63), 18f, 82f, 76f, 24f, 5f)
    fillPoly(Color(0xFF757575), 90f, 10f, 66f, 20f, 80f, 34f)       // head
    fillPoly(Color(0xFFE53935), 22f, 78f, 8f, 76f, 14f, 66f)         // feathers
    fillPoly(Color(0xFFE53935), 22f, 78f, 24f, 92f, 34f, 86f)
    fillPoly(Color(0xFFEF9A9A), 30f, 70f, 16f, 66f, 24f, 56f)
}

// ───────────────────────── ஆ words ─────────────────────────

/** ஆடு - goat */
private fun DrawScope.drawGoat() {
    fillRect(GROUND, 0f, 88f, 100f, 12f)
    // legs
    strokeLine(Color(0xFF8D8D8D), 32f, 68f, 32f, 88f, 5f)
    strokeLine(Color(0xFF8D8D8D), 44f, 68f, 44f, 88f, 5f)
    strokeLine(Color(0xFF8D8D8D), 60f, 68f, 60f, 88f, 5f)
    strokeLine(Color(0xFF8D8D8D), 70f, 68f, 70f, 88f, 5f)
    fillOval(Color(0xFFF5F5F5), 24f, 44f, 56f, 30f)        // body
    strokeLine(Color(0xFFF5F5F5), 24f, 52f, 16f, 46f, 5f)  // tail
    fillCircle(Color(0xFFF5F5F5), 82f, 40f, 11f)           // head
    fillOval(Color(0xFFF5F5F5), 80f, 36f, 18f, 10f)        // snout
    strokeLine(Color(0xFF6D4C41), 80f, 31f, 74f, 16f, 3.5f) // horns
    strokeLine(Color(0xFF6D4C41), 86f, 31f, 90f, 16f, 3.5f)
    fillPoly(Color(0xFFBDBDBD), 74f, 36f, 64f, 40f, 74f, 44f) // ear
    fillPoly(Color(0xFF6D4C41), 84f, 50f, 90f, 50f, 87f, 60f) // beard
    fillCircle(INK, 84f, 38f, 1.8f)
}

/** ஆலமரம் - banyan tree with hanging roots */
private fun DrawScope.drawBanyanTree() {
    fillRect(GROUND, 0f, 90f, 100f, 10f)
    fillRect(Color(0xFF6D4C41), 44f, 50f, 12f, 42f)
    fillCircle(Color(0xFF2E7D32), 50f, 32f, 24f)
    fillCircle(Color(0xFF388E3C), 28f, 44f, 17f)
    fillCircle(Color(0xFF388E3C), 72f, 44f, 17f)
    fillCircle(Color(0xFF43A047), 50f, 40f, 16f)
    // aerial roots
    for (x in listOf(22f, 32f, 68f, 78f)) {
        strokeLine(Color(0xFF795548), x, 54f, x, 88f, 2.2f)
    }
}

/** ஆமை - tortoise */
private fun DrawScope.drawTortoise() {
    fillRect(GROUND, 0f, 84f, 100f, 16f)
    fillRect(Color(0xFF9CCC65), 30f, 66f, 12f, 16f)  // legs
    fillRect(Color(0xFF9CCC65), 62f, 66f, 12f, 16f)
    fillCircle(Color(0xFF9CCC65), 86f, 60f, 9f)      // head
    strokeLine(Color(0xFF9CCC65), 20f, 70f, 10f, 76f, 4f) // tail
    drawArc(
        Color(0xFF6D8B3A), 180f, 180f, true,
        Offset(18f, 30f), Size(68f, 56f)
    )
    // shell pattern
    drawArc(Color(0xFF8BAA4A), 180f, 180f, true, Offset(30f, 42f), Size(44f, 36f))
    strokeLine(Color(0xFF55702A), 52f, 58f, 52f, 86f, 2f)
    strokeLine(Color(0xFF55702A), 30f, 70f, 74f, 70f, 2f)
    fillCircle(INK, 89f, 58f, 1.6f)
}

/** ஆந்தை - owl on a branch */
private fun DrawScope.drawOwl() {
    fillRect(Color(0xFF6D4C41), 12f, 86f, 76f, 6f)
    fillOval(Color(0xFF8D6E63), 26f, 28f, 48f, 60f)           // body
    fillOval(Color(0xFFD7CCC8), 36f, 52f, 28f, 30f)           // belly
    fillPoly(Color(0xFF8D6E63), 28f, 20f, 40f, 34f, 26f, 40f) // ear tufts
    fillPoly(Color(0xFF8D6E63), 72f, 20f, 60f, 34f, 74f, 40f)
    fillCircle(Color.White, 40f, 40f, 11f)                    // eyes
    fillCircle(Color.White, 60f, 40f, 11f)
    fillCircle(Color(0xFFFFB300), 40f, 40f, 6f)
    fillCircle(Color(0xFFFFB300), 60f, 40f, 6f)
    fillCircle(INK, 40f, 40f, 3f)
    fillCircle(INK, 60f, 40f, 3f)
    fillPoly(Color(0xFFFF9800), 50f, 44f, 45f, 54f, 55f, 54f) // beak
    fillOval(Color(0xFF6D4C41), 14f, 50f, 16f, 30f)           // wings
    fillOval(Color(0xFF6D4C41), 70f, 50f, 16f, 30f)
    fillPoly(Color(0xFFFF9800), 40f, 88f, 36f, 92f, 46f, 92f) // feet
    fillPoly(Color(0xFFFF9800), 60f, 88f, 56f, 92f, 66f, 92f)
}

/** ஆப்பிள் - apple */
private fun DrawScope.drawApple() {
    fillCircle(Color(0xFFD32F2F), 40f, 58f, 26f)
    fillCircle(Color(0xFFD32F2F), 60f, 58f, 26f)
    fillOval(Color(0xFFD32F2F), 20f, 40f, 60f, 54f)
    fillOval(Color(0xFFFFCDD2), 30f, 46f, 10f, 16f)           // highlight
    strokeLine(Color(0xFF6D4C41), 50f, 36f, 54f, 16f, 4f)     // stem
    fillOval(Color(0xFF43A047), 54f, 14f, 24f, 12f)           // leaf
}

// ───────────────────────── க் words (க் sits inside the word) ─────────────────────────

/** சக்கரம் - wheel */
private fun DrawScope.drawWheel() {
    val c = Offset(50f, 50f)
    drawCircle(Color(0xFF37474F), 40f, c)
    drawCircle(Color(0xFFB0BEC5), 31f, c)
    for (i in 0 until 8) {
        val a = i * PI.toFloat() / 4f
        strokeLine(
            Color(0xFF455A64),
            c.x, c.y,
            c.x + 30f * cos(a), c.y + 30f * sin(a),
            3.5f
        )
    }
    drawCircle(Color(0xFF37474F), 31f, c, style = Stroke(3f))
    drawCircle(Color(0xFFFFB300), 7f, c)
}

/** காக்கை - crow on a branch */
private fun DrawScope.drawCrow() {
    fillRect(Color(0xFF6D4C41), 14f, 86f, 72f, 6f)
    strokeLine(Color(0xFFFF9800), 44f, 68f, 44f, 86f, 3f)    // legs
    strokeLine(Color(0xFFFF9800), 56f, 68f, 56f, 86f, 3f)
    fillPoly(INK, 30f, 50f, 4f, 72f, 14f, 50f)                // tail
    fillOval(INK, 22f, 38f, 52f, 34f)                         // body
    fillOval(Color(0xFF37474F), 30f, 44f, 30f, 20f)           // wing
    fillCircle(INK, 72f, 36f, 13f)                            // head
    fillPoly(Color(0xFF757575), 82f, 32f, 98f, 40f, 82f, 44f) // beak
    fillCircle(Color.White, 75f, 33f, 3.2f)
    fillCircle(INK, 76f, 33f, 1.5f)
}

/** கொக்கு - crane standing in water */
private fun DrawScope.drawCrane() {
    strokeLine(Color(0xFFFF9800), 46f, 66f, 46f, 94f, 2.6f)  // long legs
    strokeLine(Color(0xFFFF9800), 56f, 66f, 56f, 94f, 2.6f)
    fillPoly(Color.White, 30f, 52f, 10f, 56f, 32f, 62f)       // tail
    fillOval(Color(0xFFE0E0E0), 28f, 46f, 44f, 24f)
    fillOval(Color.White, 30f, 46f, 42f, 22f)
    val neck = Path().apply {
        moveTo(66f, 52f)
        quadraticTo(78f, 40f, 70f, 20f)
    }
    drawPath(neck, Color(0xFFE0E0E0), style = Stroke(7f, cap = StrokeCap.Round))
    drawPath(neck, Color.White, style = Stroke(5f, cap = StrokeCap.Round))
    fillCircle(Color.White, 70f, 17f, 6f)
    fillPoly(Color(0xFFFFB300), 75f, 15f, 97f, 20f, 75f, 22f) // beak
    fillCircle(INK, 71f, 16f, 1.4f)
    waves(WATER, 84f)
}

// ───────────────────────── க words ─────────────────────────

/** கடல் - sea with sun */
private fun DrawScope.drawSea() {
    fillCircle(Color(0xFFFFCA28), 74f, 26f, 14f)
    waves(Color(0xFF90CAF9), 56f)
    waves(Color(0xFF42A5F5), 70f)
    waves(Color(0xFF1E88E5), 84f)
}

/** கப்பல் - ship */
private fun DrawScope.drawShip() {
    // smoke
    fillCircle(Color(0xFFCFD8DC), 56f, 26f, 5f)
    fillCircle(Color(0xFFCFD8DC), 64f, 18f, 6f)
    fillRect(Color(0xFFE53935), 50f, 34f, 9f, 14f)            // funnel
    fillRect(Color.White, 38f, 46f, 30f, 14f)                 // cabin
    fillRect(Color(0xFF90CAF9), 42f, 50f, 5f, 5f)
    fillRect(Color(0xFF90CAF9), 52f, 50f, 5f, 5f)
    fillRect(Color(0xFF90CAF9), 60f, 50f, 5f, 5f)
    fillPoly(Color(0xFF455A64), 10f, 60f, 92f, 60f, 80f, 82f, 24f, 82f) // hull
    fillRect(Color(0xFFE53935), 16f, 66f, 72f, 4f)
    waves(Color(0xFF42A5F5), 84f)
}

/** கண் - eye */
private fun DrawScope.drawEye() {
    val almond = Path().apply {
        moveTo(6f, 52f)
        quadraticTo(50f, 8f, 94f, 52f)
        quadraticTo(50f, 96f, 6f, 52f)
        close()
    }
    drawPath(almond, Color.White)
    drawPath(almond, INK, style = Stroke(3.5f))
    fillCircle(Color(0xFF6D4C41), 50f, 52f, 19f)
    fillCircle(INK, 50f, 52f, 9f)
    fillCircle(Color.White, 44f, 46f, 4f)
    for (i in 0 until 5) { // lashes
        val x = 22f + i * 14f
        strokeLine(INK, x, 34f - (if (i == 2) 4f else 0f), x - 3f, 24f - (if (i == 2) 4f else 0f), 2.4f)
    }
}

// ─────────────────────── இ ஈ உ ஊ எ ஏ ஐ ஒ ஓ ஔ words ───────────────────────

/** இலை - leaf */
private fun DrawScope.drawLeaf() {
    val leaf = Path().apply {
        moveTo(50f, 92f)
        quadraticTo(4f, 50f, 50f, 8f)
        quadraticTo(96f, 50f, 50f, 92f)
        close()
    }
    drawPath(leaf, Color(0xFF43A047))
    strokeLine(Color(0xFF1B5E20), 50f, 92f, 50f, 18f, 2.6f)
    for (y in listOf(36f, 52f, 68f)) {
        strokeLine(Color(0xFF2E7D32), 50f, y + 8f, 28f, y - 6f, 1.8f)
        strokeLine(Color(0xFF2E7D32), 50f, y + 8f, 72f, y - 6f, 1.8f)
    }
}

/** இட்லி - idli on a plate */
private fun DrawScope.drawIdli() {
    fillOval(Color(0xFF90A4AE), 6f, 62f, 88f, 24f)
    fillOval(Color(0xFFECEFF1), 10f, 62f, 80f, 16f)
    for (x in listOf(14f, 36f, 58f)) {
        fillOval(Color(0xFFCFD8DC), x, 44f, 30f, 28f)
        fillOval(Color.White, x + 1f, 42f, 28f, 24f)
    }
}

/** ஈ - fly */
private fun DrawScope.drawFly() {
    for ((dx, dy) in listOf(0f to 0f, 1f to 0f)) {
        fillOval(Color(0xAA90CAF9), 18f + dx * 34f, 26f, 30f, 14f)
    }
    for (x in listOf(42f, 50f, 58f)) {
        strokeLine(INK, x, 58f, x + (x - 50f) * 2f, 74f, 1.8f)
    }
    fillOval(Color(0xFF424242), 40f, 40f, 20f, 38f)
    fillCircle(Color(0xFF212121), 50f, 38f, 9f)
    fillCircle(Color(0xFFD32F2F), 45f, 36f, 3.8f)
    fillCircle(Color(0xFFD32F2F), 55f, 36f, 3.8f)
}

/** ஈட்டி - spear */
private fun DrawScope.drawSpear() {
    strokeLine(Color(0xFF8D6E63), 16f, 88f, 72f, 32f, 4.5f)
    fillPoly(Color(0xFF90A4AE), 92f, 10f, 64f, 26f, 76f, 38f)
    fillPoly(Color(0xFFE53935), 22f, 82f, 10f, 70f, 20f, 66f)
    fillRect(GROUND, 0f, 92f, 100f, 8f)
}

/** உரல் - mortar and pestle */
private fun DrawScope.drawMortar() {
    strokeLine(Color(0xFF8D6E63), 66f, 8f, 52f, 46f, 9f)
    fillCircle(Color(0xFF6D4C41), 67f, 9f, 6f)
    fillPoly(Color(0xFF9E9E9E), 20f, 40f, 80f, 40f, 70f, 88f, 30f, 88f)
    fillOval(Color(0xFF616161), 20f, 34f, 60f, 14f)
    fillRect(GROUND, 0f, 90f, 100f, 10f)
}

/** உண்டியல் - piggy bank */
private fun DrawScope.drawPiggyBank() {
    fillCircle(Color(0xFFFFC107), 46f, 18f, 9f)
    fillCircle(Color(0xFFFFE082), 46f, 18f, 5f)
    fillRect(Color(0xFFF48FB1), 26f, 72f, 10f, 14f)
    fillRect(Color(0xFFF48FB1), 60f, 72f, 10f, 14f)
    fillOval(Color(0xFFF8BBD0), 12f, 34f, 70f, 46f)
    fillPoly(Color(0xFFF48FB1), 58f, 40f, 66f, 26f, 76f, 42f)
    fillCircle(Color(0xFFF48FB1), 86f, 58f, 9f)
    fillCircle(Color(0xFFAD1457), 84f, 56f, 1.6f)
    fillCircle(Color(0xFFAD1457), 88f, 60f, 1.6f)
    fillCircle(INK, 74f, 50f, 2f)
    strokeLine(INK, 30f, 40f, 54f, 40f, 3f)
}

/** ஊஞ்சல் - swing */
private fun DrawScope.drawSwing() {
    fillRect(GROUND, 0f, 90f, 100f, 10f)
    fillRect(Color(0xFF6D4C41), 8f, 10f, 84f, 6f)
    strokeLine(Color(0xFF6D4C41), 12f, 14f, 22f, 90f, 4f)
    strokeLine(Color(0xFF6D4C41), 88f, 14f, 78f, 90f, 4f)
    strokeLine(Color(0xFF8D6E63), 32f, 16f, 32f, 66f, 2f)
    strokeLine(Color(0xFF8D6E63), 68f, 16f, 68f, 66f, 2f)
    fillRect(Color(0xFFE53935), 26f, 66f, 48f, 7f)
}

/** ஊசி - needle with thread */
private fun DrawScope.drawNeedle() {
    strokeLine(Color(0xFF9E9E9E), 22f, 90f, 76f, 14f, 3.2f)
    drawCircle(Color(0xFF757575), 4.5f, Offset(77f, 12f), style = Stroke(1.8f))
    val thread = Path().apply {
        moveTo(77f, 12f)
        quadraticTo(96f, 40f, 66f, 54f)
        quadraticTo(40f, 66f, 54f, 86f)
    }
    drawPath(thread, Color(0xFFE53935), style = Stroke(2f, cap = StrokeCap.Round))
}

/** எறும்பு - ant */
private fun DrawScope.drawAnt() {
    for (x in listOf(40f, 52f, 64f)) {
        strokeLine(INK, x, 58f, x - 8f, 76f, 2f)
        strokeLine(INK, x, 58f, x + 6f, 76f, 2f)
    }
    fillOval(Color(0xFF6D4C41), 12f, 42f, 36f, 28f)
    fillCircle(Color(0xFF5D4037), 55f, 56f, 9f)
    fillCircle(Color(0xFF6D4C41), 74f, 50f, 11f)
    strokeLine(INK, 78f, 42f, 90f, 28f, 1.8f)
    strokeLine(INK, 72f, 42f, 74f, 26f, 1.8f)
    fillCircle(Color.White, 78f, 49f, 2.4f)
    fillRect(GROUND, 0f, 88f, 100f, 12f)
}

/** எலி - mouse */
private fun DrawScope.drawMouse() {
    val tail = Path().apply {
        moveTo(26f, 66f)
        quadraticTo(6f, 70f, 10f, 44f)
    }
    drawPath(tail, Color(0xFFF48FB1), style = Stroke(3f, cap = StrokeCap.Round))
    fillOval(Color(0xFF9E9E9E), 22f, 46f, 50f, 34f)
    fillCircle(Color(0xFF9E9E9E), 74f, 62f, 14f)
    fillCircle(Color(0xFF9E9E9E), 66f, 42f, 10f)
    fillCircle(Color(0xFFF8BBD0), 66f, 42f, 5f)
    fillCircle(Color(0xFFE91E63), 88f, 64f, 3.5f)
    fillCircle(INK, 76f, 58f, 2f)
    strokeLine(INK, 82f, 66f, 96f, 62f, 1.2f)
    strokeLine(INK, 82f, 68f, 96f, 72f, 1.2f)
    fillRect(GROUND, 0f, 86f, 100f, 14f)
}

/** ஏணி - ladder */
private fun DrawScope.drawLadder() {
    fillRect(Color(0xFF8D6E63), 30f, 6f, 7f, 88f)
    fillRect(Color(0xFF8D6E63), 63f, 6f, 7f, 88f)
    for (i in 0 until 6) {
        fillRect(Color(0xFFA1887F), 30f, 16f + i * 14f, 40f, 5f)
    }
}

/** ஐந்து - five (hand with five fingers) */
private fun DrawScope.drawFive() {
    strokeLine(SKIN, 34f, 56f, 32f, 22f, 9f)
    strokeLine(SKIN, 45f, 54f, 45f, 12f, 9f)
    strokeLine(SKIN, 56f, 54f, 58f, 16f, 9f)
    strokeLine(SKIN, 67f, 58f, 71f, 26f, 9f)
    strokeLine(SKIN, 30f, 72f, 12f, 52f, 10f)
    fillOval(SKIN, 26f, 52f, 50f, 40f)
}

/** ஒட்டகம் - camel */
private fun DrawScope.drawCamel() {
    fillRect(Color(0xFFFFE082), 0f, 88f, 100f, 12f)
    for (x in listOf(26f, 36f, 58f, 68f)) strokeLine(Color(0xFFBF8F4A), x, 66f, x, 88f, 5f)
    fillCircle(Color(0xFFD7A45F), 38f, 38f, 11f)
    fillCircle(Color(0xFFD7A45F), 56f, 38f, 11f)
    fillOval(Color(0xFFD7A45F), 20f, 42f, 56f, 28f)
    strokeLine(Color(0xFFD7A45F), 72f, 52f, 82f, 28f, 9f)
    fillOval(Color(0xFFD7A45F), 78f, 18f, 18f, 12f)
    fillCircle(INK, 88f, 22f, 1.6f)
    strokeLine(Color(0xFFBF8F4A), 20f, 50f, 12f, 66f, 2.5f)
}

/** ஓடம் - boat */
private fun DrawScope.drawBoat() {
    strokeLine(Color(0xFF6D4C41), 50f, 18f, 50f, 62f, 3f)
    fillPoly(Color(0xFFFFA726), 52f, 20f, 52f, 56f, 82f, 56f)
    fillPoly(Color.White, 48f, 26f, 48f, 56f, 24f, 56f)
    fillPoly(Color(0xFF8D6E63), 10f, 62f, 90f, 62f, 76f, 82f, 24f, 82f)
    waves(Color(0xFF42A5F5), 84f)
}

/** ஔவையார் - Avvaiyar, the old poetess, with a walking stick */
private fun DrawScope.drawAvvaiyar() {
    strokeLine(Color(0xFF8D6E63), 66f, 44f, 74f, 92f, 4f)
    fillPoly(Color(0xFFF5F5F5), 30f, 40f, 50f, 40f, 60f, 92f, 20f, 92f)
    fillPoly(Color(0xFFB0BEC5), 30f, 40f, 40f, 40f, 40f, 92f, 20f, 92f)
    strokeLine(SKIN, 48f, 52f, 66f, 46f, 6f)
    fillCircle(Color(0xFFECEFF1), 40f, 20f, 13f)
    fillCircle(SKIN, 40f, 26f, 10f)
    fillCircle(Color(0xFFECEFF1), 40f, 14f, 9f)
    strokeLine(Color(0xFFBDBDBD), 35f, 20f, 45f, 20f, 1.4f)
    fillCircle(INK, 36f, 27f, 1.2f)
    fillCircle(INK, 44f, 27f, 1.2f)
}

// ─────────────────────── consonant words ───────────────────────

/** சங்கு - conch shell */
private fun DrawScope.drawConch() {
    fillPoly(Color(0xFFFFAB91), 58f, 38f, 90f, 18f, 76f, 52f)
    fillOval(Color(0xFFFFCCBC), 14f, 34f, 66f, 46f)
    fillOval(Color(0xFFF48FB1), 22f, 52f, 26f, 20f)
    drawArc(Color(0xFFBF8E7A), 0f, 280f, false, Offset(40f, 40f), Size(30f, 26f), style = Stroke(2.4f))
    drawArc(Color(0xFFBF8E7A), 20f, 260f, false, Offset(48f, 46f), Size(14f, 12f), style = Stroke(2.4f))
}

/** தங்கம் - gold bars */
private fun DrawScope.drawGold() {
    fillPoly(Color(0xFFFFC107), 6f, 84f, 16f, 62f, 50f, 62f, 58f, 84f)
    fillPoly(Color(0xFFFFC107), 42f, 84f, 52f, 62f, 86f, 62f, 96f, 84f)
    fillPoly(Color(0xFFFFD54F), 24f, 62f, 34f, 40f, 68f, 40f, 78f, 62f)
    fillPoly(Color(0xFFFFECB3), 34f, 40f, 68f, 40f, 66f, 46f, 36f, 46f)
    fillPoly(Color(0xFFFFECB3), 16f, 62f, 50f, 62f, 49f, 68f, 18f, 68f)
}

/** சட்டை - shirt */
private fun DrawScope.drawShirt() {
    fillPoly(Color(0xFF42A5F5), 30f, 28f, 12f, 44f, 20f, 58f, 30f, 50f)
    fillPoly(Color(0xFF42A5F5), 70f, 28f, 88f, 44f, 80f, 58f, 70f, 50f)
    fillRect(Color(0xFF42A5F5), 30f, 28f, 40f, 60f)
    fillPoly(Color.White, 40f, 28f, 60f, 28f, 50f, 44f)
    for (y in listOf(54f, 66f, 78f)) fillCircle(Color.White, 50f, y, 2.2f)
}

/** ஞாயிறு - sun */
private fun DrawScope.drawSun() {
    for (i in 0 until 12) {
        val a = i * PI.toFloat() / 6f
        strokeLine(Color(0xFFFF9800), 50f + 30f * cos(a), 50f + 30f * sin(a), 50f + 44f * cos(a), 50f + 44f * sin(a), 4f)
    }
    fillCircle(Color(0xFFFFCA28), 50f, 50f, 24f)
}

/** பட்டம் - kite */
private fun DrawScope.drawKite() {
    val tail = Path().apply {
        moveTo(50f, 72f)
        quadraticTo(66f, 82f, 44f, 92f)
    }
    drawPath(tail, Color(0xFF5D4037), style = Stroke(1.8f))
    fillCircle(Color(0xFF43A047), 58f, 82f, 3.5f)
    fillCircle(Color(0xFFFFC107), 46f, 90f, 3.5f)
    fillPoly(Color(0xFFE53935), 50f, 8f, 78f, 40f, 50f, 72f, 22f, 40f)
    fillPoly(Color(0xFFFFC107), 50f, 8f, 78f, 40f, 50f, 40f)
    strokeLine(Color.White, 50f, 8f, 50f, 72f, 1.6f)
    strokeLine(Color.White, 22f, 40f, 78f, 40f, 1.6f)
}

/** வட்டம் - circle */
private fun DrawScope.drawRing() {
    drawCircle(Color(0xFFFF7043), 34f, Offset(50f, 50f), style = Stroke(12f))
    drawCircle(Color(0xFFFFAB91), 34f, Offset(50f, 50f), style = Stroke(3f))
}

/** மணி - bell */
private fun DrawScope.drawBell() {
    val bell = Path().apply {
        moveTo(24f, 72f)
        quadraticTo(24f, 22f, 50f, 22f)
        quadraticTo(76f, 22f, 76f, 72f)
        close()
    }
    fillCircle(Color(0xFFF9A825), 50f, 16f, 5f)
    drawPath(bell, Color(0xFFFFC107))
    fillRect(Color(0xFFF9A825), 18f, 70f, 64f, 9f)
    fillCircle(Color(0xFF8D6E63), 50f, 84f, 6f)
    fillOval(Color(0xFFFFE082), 34f, 32f, 8f, 24f)
}

/** பணம் - coins */
private fun DrawScope.drawCoins() {
    for (y in listOf(66f, 52f, 38f)) {
        fillOval(Color(0xFFF9A825), 22f, y + 6f, 56f, 20f)
        fillOval(Color(0xFFFFD54F), 22f, y, 56f, 20f)
    }
    fillOval(Color(0xFFFFB300), 34f, 41f, 32f, 12f)
}

/** தவளை - frog */
private fun DrawScope.drawFrog() {
    fillOval(Color(0xFF66BB6A), 4f, 80f, 92f, 16f)
    fillOval(Color(0xFF388E3C), 8f, 68f, 24f, 16f)
    fillOval(Color(0xFF388E3C), 68f, 68f, 24f, 16f)
    fillOval(Color(0xFF4CAF50), 18f, 36f, 64f, 46f)
    fillCircle(Color(0xFF4CAF50), 32f, 34f, 11f)
    fillCircle(Color(0xFF4CAF50), 68f, 34f, 11f)
    fillCircle(Color.White, 32f, 33f, 7f)
    fillCircle(Color.White, 68f, 33f, 7f)
    fillCircle(INK, 32f, 33f, 3.4f)
    fillCircle(INK, 68f, 33f, 3.4f)
    drawArc(INK, 10f, 160f, false, Offset(34f, 54f), Size(32f, 16f), style = Stroke(2.4f))
}

/** தட்டு - plate with food */
private fun DrawScope.drawPlate() {
    drawCircle(Color(0xFF90A4AE), 42f, Offset(50f, 50f))
    drawCircle(Color(0xFFECEFF1), 36f, Offset(50f, 50f))
    drawCircle(Color(0xFFCFD8DC), 26f, Offset(50f, 50f), style = Stroke(2f))
    fillCircle(Color.White, 44f, 48f, 12f)
    fillCircle(Color(0xFFFF8F00), 62f, 42f, 7f)
    fillCircle(Color(0xFF66BB6A), 58f, 62f, 7f)
    fillCircle(Color(0xFFE53935), 36f, 66f, 6f)
}

/** நத்தை - snail */
private fun DrawScope.drawSnail() {
    fillRect(GROUND, 0f, 86f, 100f, 14f)
    fillOval(Color(0xFFFFCC80), 10f, 66f, 78f, 20f)
    fillCircle(Color(0xFFFFCC80), 86f, 70f, 9f)
    strokeLine(Color(0xFFFFCC80), 84f, 64f, 82f, 44f, 2.4f)
    strokeLine(Color(0xFFFFCC80), 90f, 64f, 94f, 46f, 2.4f)
    fillCircle(INK, 82f, 44f, 2.6f)
    fillCircle(INK, 94f, 46f, 2.6f)
    fillCircle(Color(0xFF8D6E63), 42f, 48f, 24f)
    drawCircle(Color(0xFF5D4037), 15f, Offset(42f, 48f), style = Stroke(2.5f))
    drawCircle(Color(0xFF5D4037), 7f, Offset(42f, 48f), style = Stroke(2.5f))
}

/** நட்சத்திரம் - star */
private fun DrawScope.drawStar() {
    val path = Path()
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) 42f else 17f
        val a = (-PI / 2).toFloat() + i * PI.toFloat() / 5f
        val x = 50f + r * cos(a)
        val y = 54f + r * sin(a)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, Color(0xFFFFC107))
    drawPath(path, Color(0xFFF9A825), style = Stroke(2.4f))
}

/** பந்து - ball */
private fun DrawScope.drawBall() {
    fillCircle(Color(0xFFEF5350), 50f, 50f, 36f)
    drawArc(Color.White, 200f, 140f, false, Offset(14f, 24f), Size(72f, 56f), style = Stroke(3f))
    drawArc(Color.White, 20f, 140f, false, Offset(14f, 20f), Size(72f, 56f), style = Stroke(3f))
    fillCircle(Color(0x66FFFFFF), 38f, 34f, 8f)
}

/** மலை - mountain */
private fun DrawScope.drawMountain() {
    fillCircle(Color(0xFFFFCA28), 80f, 20f, 10f)
    fillPoly(Color(0xFF78909C), 2f, 90f, 36f, 28f, 72f, 90f)
    fillPoly(Color(0xFF546E7A), 40f, 90f, 68f, 40f, 98f, 90f)
    fillPoly(Color.White, 36f, 28f, 27f, 44f, 36f, 40f, 45f, 46f)
    fillPoly(Color.White, 68f, 40f, 61f, 52f, 68f, 48f, 75f, 52f)
}

/** யானை - elephant */
private fun DrawScope.drawElephant() {
    fillRect(GROUND, 0f, 90f, 100f, 10f)
    for (x in listOf(24f, 38f, 54f, 66f)) fillRect(Color(0xFF78909C), x, 66f, 10f, 24f)
    fillOval(Color(0xFF90A4AE), 14f, 34f, 56f, 42f)
    fillCircle(Color(0xFF90A4AE), 76f, 44f, 17f)
    fillCircle(Color(0xFF78909C), 64f, 44f, 13f)
    val trunk = Path().apply {
        moveTo(86f, 48f)
        quadraticTo(98f, 66f, 86f, 82f)
    }
    drawPath(trunk, Color(0xFF90A4AE), style = Stroke(8f, cap = StrokeCap.Round))
    strokeLine(Color.White, 80f, 56f, 90f, 68f, 3f)
    fillCircle(INK, 80f, 38f, 2f)
}

/** கல் - stone */
private fun DrawScope.drawStone() {
    fillRect(GROUND, 0f, 86f, 100f, 14f)
    fillPoly(Color(0xFF78909C), 14f, 84f, 22f, 52f, 46f, 34f, 74f, 42f, 88f, 66f, 84f, 84f)
    fillPoly(Color(0xFFB0BEC5), 28f, 54f, 46f, 40f, 62f, 46f, 44f, 56f)
}

/** வண்டி - bullock cart */
private fun DrawScope.drawCart() {
    fillRect(GROUND, 0f, 88f, 100f, 12f)
    strokeLine(Color(0xFF6D4C41), 62f, 50f, 96f, 40f, 4f)
    fillRect(Color(0xFF8D6E63), 14f, 36f, 54f, 22f)
    fillRect(Color(0xFFA1887F), 14f, 36f, 54f, 5f)
    drawCircle(Color(0xFF4E342E), 20f, Offset(40f, 68f), style = Stroke(4f))
    for (i in 0 until 4) {
        val a = i * PI.toFloat() / 4f
        strokeLine(Color(0xFF4E342E), 40f + 20f * cos(a), 68f + 20f * sin(a), 40f - 20f * cos(a), 68f - 20f * sin(a), 2.4f)
    }
    fillCircle(Color(0xFF4E342E), 40f, 68f, 4f)
}

/** மழை - rain */
private fun DrawScope.drawRain() {
    fillCircle(Color(0xFF90A4AE), 32f, 36f, 15f)
    fillCircle(Color(0xFF90A4AE), 52f, 28f, 18f)
    fillCircle(Color(0xFF90A4AE), 70f, 36f, 14f)
    fillRect(Color(0xFF90A4AE), 32f, 36f, 38f, 14f)
    for (x in listOf(28f, 42f, 56f, 70f)) {
        strokeLine(WATER, x, 60f, x - 5f, 74f, 3f)
        strokeLine(WATER, x + 7f, 74f, x + 2f, 88f, 3f)
    }
}

/** கிளி - parrot */
private fun DrawScope.drawParrot() {
    fillRect(Color(0xFF6D4C41), 10f, 88f, 80f, 6f)
    fillPoly(Color(0xFF2E7D32), 40f, 70f, 28f, 96f, 56f, 76f)
    fillOval(Color(0xFF66BB6A), 30f, 34f, 38f, 54f)
    fillOval(Color(0xFF2E7D32), 34f, 48f, 20f, 32f)
    fillCircle(Color(0xFF66BB6A), 52f, 28f, 15f)
    fillPoly(Color(0xFFE53935), 62f, 20f, 78f, 28f, 72f, 40f, 62f, 36f)
    fillCircle(Color.White, 56f, 24f, 4f)
    fillCircle(INK, 57f, 24f, 2f)
}

/** வளையல் - bangles */
private fun DrawScope.drawBangles() {
    drawCircle(Color(0xFFE53935), 24f, Offset(36f, 46f), style = Stroke(7f))
    drawCircle(Color(0xFF43A047), 24f, Offset(60f, 46f), style = Stroke(7f))
    drawCircle(Color(0xFFFFC107), 24f, Offset(48f, 66f), style = Stroke(7f))
}

/** மீன் - fish */
private fun DrawScope.drawFish() {
    fillCircle(Color(0x6690CAF9), 20f, 84f, 3f)
    fillCircle(Color(0x6690CAF9), 28f, 74f, 2f)
    fillPoly(Color(0xFFFF7043), 70f, 50f, 94f, 30f, 94f, 70f)
    fillPoly(Color(0xFFFF8A65), 36f, 38f, 50f, 22f, 58f, 40f)
    fillOval(Color(0xFFFF7043), 10f, 32f, 66f, 36f)
    fillCircle(Color.White, 26f, 46f, 5f)
    fillCircle(INK, 25f, 46f, 2.4f)
    for (x in listOf(40f, 52f)) {
        drawArc(Color(0xFFFFAB91), 270f, 180f, false, Offset(x, 40f), Size(10f, 20f), style = Stroke(1.6f))
    }
}

/** பானை - clay pot */
private fun DrawScope.drawPot() {
    fillRect(GROUND, 0f, 90f, 100f, 10f)
    fillCircle(Color(0xFFBF5B3A), 50f, 62f, 28f)
    fillRect(Color(0xFFBF5B3A), 36f, 26f, 28f, 14f)
    fillOval(Color(0xFFA1472B), 32f, 20f, 36f, 12f)
    strokeLine(Color(0xFF8D3B22), 26f, 62f, 74f, 62f, 2.4f)
    strokeLine(Color(0xFF8D3B22), 30f, 74f, 70f, 74f, 2.4f)
    fillOval(Color(0x44FFFFFF), 32f, 46f, 10f, 16f)
}

/** கரடி - bear */
private fun DrawScope.drawBear() {
    fillCircle(Color(0xFF6D4C41), 26f, 28f, 14f)              // ears
    fillCircle(Color(0xFF6D4C41), 74f, 28f, 14f)
    fillCircle(Color(0xFFD7B899), 26f, 28f, 7f)
    fillCircle(Color(0xFFD7B899), 74f, 28f, 7f)
    fillCircle(Color(0xFF6D4C41), 50f, 55f, 32f)              // head
    fillOval(Color(0xFFD7B899), 36f, 56f, 28f, 24f)           // muzzle
    fillOval(INK, 44f, 58f, 12f, 8f)                          // nose
    fillCircle(INK, 38f, 46f, 3.2f)
    fillCircle(INK, 62f, 46f, 3.2f)
    strokeLine(INK, 50f, 66f, 50f, 72f, 1.8f)
    drawArc(INK, 10f, 160f, false, Offset(42f, 68f), Size(16f, 8f), style = Stroke(1.8f))
}
