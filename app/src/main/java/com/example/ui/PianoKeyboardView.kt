package com.example.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.ui.theme.*

data class PianoKey(
    val name: String,
    val isBlack: Boolean,
    val whiteIndex: Int,
    val offsetRatio: Float = 0f // Offset relative to white key width for black keys
)

val pianoKeys = listOf(
    // Octave 4
    PianoKey("C4", false, 0),
    PianoKey("C#4", true, 0, 0.65f),
    PianoKey("D4", false, 1),
    PianoKey("D#4", true, 1, 0.65f),
    PianoKey("E4", false, 2),
    PianoKey("F4", false, 3),
    PianoKey("F#4", true, 3, 0.65f),
    PianoKey("G4", false, 4),
    PianoKey("G#4", true, 4, 0.65f),
    PianoKey("A4", false, 5),
    PianoKey("A#4", true, 5, 0.65f),
    PianoKey("B4", false, 6),
    
    // Octave 5
    PianoKey("C5", false, 7),
    PianoKey("C#5", true, 7, 0.65f),
    PianoKey("D5", false, 8),
    PianoKey("D#5", true, 8, 0.65f),
    PianoKey("E5", false, 9),
    PianoKey("F5", false, 10),
    PianoKey("F#5", true, 10, 0.65f),
    PianoKey("G5", false, 11),
    PianoKey("G#5", true, 11, 0.65f),
    PianoKey("A5", false, 12),
    PianoKey("A#5", true, 12, 0.65f),
    PianoKey("B5", false, 13)
)

val noteColors = listOf(
    Color(0xFFFF595E), Color(0xFFFF924C), Color(0xFFFFCA3A),
    Color(0xFF8AC926), Color(0xFF1982C4), Color(0xFF6A4C93)
)

@Composable
fun PianoKeyboardView(
    modifier: Modifier = Modifier,
    highlightedNote: String? = null,
    wrongNote: String? = null,
    upcomingNotes: List<String> = emptyList(),
    currentNoteIndex: Int = 0,
    onNotePlayed: (String) -> Unit = {}
) {
    val pressedKeys = remember { mutableStateMapOf<Long, String>() }
    val currentOnNotePlayed by rememberUpdatedState(newValue = onNotePlayed)

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(DarkCharcoal)
            .padding(top = 16.dp, bottom = 16.dp, start = 8.dp, end = 8.dp)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        val changes = event.changes
                        
                        changes.forEach { change ->
                            if (change.pressed) {
                                // Calculate which key is pressed
                                val pt = change.position
                                val trackHeight = size.height * (if (upcomingNotes.isNotEmpty()) 0.45f else 0f)
                                if (pt.y < trackHeight) return@forEach
                                val keysHeight = size.height - trackHeight

                                val wWidth = size.width / 14f
                                val bWidth = wWidth * 0.7f
                                val bHeight = keysHeight * 0.6f

                                var hitKey: String? = null
                                
                                // Check black keys first (they are on top)
                                for (key in pianoKeys.filter { it.isBlack }) {
                                    val startX = key.whiteIndex * wWidth + wWidth * key.offsetRatio
                                    val endX = startX + bWidth
                                    if (pt.x in startX..endX && pt.y <= trackHeight + bHeight) {
                                        hitKey = key.name
                                        break
                                    }
                                }

                                // Check white keys if no black key hit
                                if (hitKey == null) {
                                    val index = (pt.x / wWidth).toInt().coerceIn(0, 13)
                                    hitKey = pianoKeys.firstOrNull { !it.isBlack && it.whiteIndex == index }?.name
                                }

                                if (hitKey != null) {
                                    val previousKey = pressedKeys[change.id.value]
                                    if (previousKey != hitKey) {
                                        pressedKeys[change.id.value] = hitKey
                                        SoundManager.playNote(hitKey)
                                        currentOnNotePlayed(hitKey)
                                    }
                                }
                            } else {
                                pressedKeys.remove(change.id.value)
                            }
                        }
                    }
                }
            }
    ) {
        val trackHeight = maxHeight * (if (upcomingNotes.isNotEmpty()) 0.45f else 0f)
        val keysHeight = maxHeight - trackHeight

        val wWidth = maxWidth / 14
        val bWidth = wWidth * 0.7f
        val wHeight = keysHeight
        val bHeight = keysHeight * 0.6f

        if (upcomingNotes.isNotEmpty()) {
            val animatedIndex by animateFloatAsState(
                targetValue = currentNoteIndex.toFloat(),
                animationSpec = tween(durationMillis = 300, easing = LinearEasing),
                label = "block_scroll"
            )

            val blockHeight = maxHeight * 0.15f
            val blockSpacing = maxHeight * 0.05f
            val distancePerNote = blockHeight + blockSpacing

            for (index in (currentNoteIndex - 1).coerceAtLeast(0) .. minOf(currentNoteIndex + 12, upcomingNotes.lastIndex)) {
                val noteStr = upcomingNotes[index]
                val key = pianoKeys.find { it.name == noteStr } ?: continue

                val diff = index - animatedIndex
                val noteYPos = trackHeight - blockHeight - (distancePerNote * diff)

                val startX = if (key.isBlack) {
                    wWidth * key.whiteIndex + wWidth * key.offsetRatio + wWidth * 0.05f
                } else {
                    wWidth * key.whiteIndex + wWidth * 0.1f
                }
                val keyWidth = if (key.isBlack) bWidth * 0.9f else wWidth * 0.8f
                
                val blockColor = if (key.isBlack) {
                    noteColors[(key.whiteIndex + 3) % noteColors.size]
                } else {
                    noteColors[key.whiteIndex % noteColors.size]
                }

                Box(
                    modifier = Modifier
                        .offset(x = startX, y = noteYPos)
                        .width(keyWidth)
                        .height(blockHeight)
                        .clip(RoundedCornerShape(8.dp))
                        .background(blockColor.copy(alpha = 0.8f))
                )
            }
        }

        val infiniteTransition = rememberInfiniteTransition(label = "arrow_bounce")
        val arrowOffsetY by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -15f,
            animationSpec = infiniteRepeatable(
                animation = tween(400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "arrow_offset"
        )

        // Draw White Keys
        pianoKeys.filter { !it.isBlack }.forEach { key ->
            val isPressed = pressedKeys.values.contains(key.name)
            val isHighlighted = key.name == highlightedNote
            
            val offsetY by animateDpAsState(
                targetValue = if (isPressed) trackHeight + 8.dp else trackHeight,
                animationSpec = spring(),
                label = "press"
            )
            
            val color by animateColorAsState(
                targetValue = when {
                    key.name == wrongNote -> Color(0xFFFF5252)
                    isHighlighted -> BrightGreen
                    isPressed -> noteColors[key.whiteIndex % noteColors.size]
                    else -> SoftWhite
                },
                label = "color"
            )

            Box(
                modifier = Modifier
                    .offset(x = wWidth * key.whiteIndex, y = offsetY)
                    .width(wWidth)
                    .height(wHeight)
                    .padding(end = 2.dp)
                    .shadow(if (isPressed) 0.dp else 4.dp, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                    .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                    .background(color)
                    .then(
                        if (isHighlighted) Modifier.border(4.dp, GlowColor, RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                        else Modifier
                    )
            ) {
                Text(
                    text = key.name.first().toString(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            }
        }

        // Draw Black Keys
        pianoKeys.filter { it.isBlack }.forEach { key ->
            val isPressed = pressedKeys.values.contains(key.name)
            val isHighlighted = key.name == highlightedNote

            val offsetY by animateDpAsState(
                targetValue = if (isPressed) trackHeight + 8.dp else trackHeight,
                animationSpec = spring(),
                label = "press"
            )

            val color by animateColorAsState(
                targetValue = when {
                    key.name == wrongNote -> Color(0xFFFF5252)
                    isHighlighted -> BrightGreen
                    isPressed -> noteColors[(key.whiteIndex + 3) % noteColors.size]
                    else -> BlackKeys
                },
                label = "color"
            )

            Box(
                modifier = Modifier
                    .offset(x = wWidth * key.whiteIndex + wWidth * key.offsetRatio, y = offsetY)
                    .width(bWidth)
                    .height(bHeight)
                    .shadow(if (isPressed) 0.dp else 6.dp, RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                    .clip(RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                    .background(color)
                    .then(
                        if (isHighlighted) Modifier.border(3.dp, GlowColor, RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                        else Modifier
                    )
            )
        }

        // Draw jumping arrow over highlighted note
        highlightedNote?.let { noteStr ->
            val key = pianoKeys.find { it.name == noteStr }
            if (key != null) {
                val startX = if (key.isBlack) {
                    wWidth * key.whiteIndex + wWidth * key.offsetRatio
                } else {
                    wWidth * key.whiteIndex
                }
                val keyWidth = if (key.isBlack) bWidth else wWidth
                val arrowX = startX + keyWidth / 2f
                
                Text(
                    text = "▼",
                    color = Color.Yellow,
                    fontSize = 32.sp,
                    modifier = Modifier
                        .offset(x = arrowX - 16.dp, y = trackHeight - 10.dp + arrowOffsetY.dp)
                )
            }
        }
    }
}
