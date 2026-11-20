package com.example.phonics_ta_in.feature.tracing

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.phonics_ta_in.core.curriculum.NormalizedPoint

@Composable
fun TracingCanvas(
    letterText: String,
    templateStrokes: List<List<NormalizedPoint>>,
    onTraceCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userPaths = remember { mutableStateListOf<List<Offset>>() }
    var currentPath by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var isVerified by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Trace: $letterText",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 28.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Canvas container box
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFF7F2FA),
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentPath = listOf(offset)
                                },
                                onDrag = { change, _ ->
                                    currentPath = currentPath + change.position
                                },
                                onDragEnd = {
                                    if (currentPath.isNotEmpty()) {
                                        userPaths.add(currentPath)
                                        currentPath = emptyList()
                                    }
                                }
                            )
                        }
                ) {
                    val width = size.width
                    val height = size.height

                    // 1. Draw Template Guide Outline (Dashed & Faded)
                    for (stroke in templateStrokes) {
                        if (stroke.size >= 2) {
                            val path = Path()
                            path.moveTo(stroke[0].x * width, stroke[0].y * height)
                            for (i in 1 until stroke.size) {
                                path.lineTo(stroke[i].x * width, stroke[i].y * height)
                            }
                            drawPath(
                                path = path,
                                color = Color.LightGray,
                                style = Stroke(
                                    width = 36f,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f)
                                )
                            )
                        }
                    }

                    // 2. Draw User Traced Lines
                    val allUserStrokes = userPaths + listOfNotNull(currentPath.takeIf { it.isNotEmpty() })
                    for (userStroke in allUserStrokes) {
                        if (userStroke.size >= 2) {
                            val path = Path()
                            path.moveTo(userStroke[0].x, userStroke[0].y)
                            for (i in 1 until userStroke.size) {
                                path.lineTo(userStroke[i].x, userStroke[i].y)
                            }
                            drawPath(
                                path = path,
                                color = if (isVerified) Color(0xFF2E7D32) else Color(0xFF6750A4),
                                style = Stroke(
                                    width = 28f,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OutlinedButton(
                onClick = {
                    userPaths.clear()
                    currentPath = emptyList()
                    isVerified = false
                },
                modifier = Modifier.height(48.dp)
            ) {
                Text("Clear", fontSize = 18.sp)
            }

            Button(
                onClick = {
                    if (userPaths.isNotEmpty() || currentPath.isNotEmpty()) {
                        isVerified = true
                        onTraceCompleted()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6750A4)
                ),
                modifier = Modifier.height(48.dp)
            ) {
                Text("Check", fontSize = 18.sp)
            }
        }
    }
}
