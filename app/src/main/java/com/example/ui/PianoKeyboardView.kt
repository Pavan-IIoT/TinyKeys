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

private const val BLACK_KEY_OFFSET_RATIO = 0.65f
private const val BLACK_KEY_WIDTH_RATIO = 0.7f
private const val BLACK_KEY_HEIGHT_RATIO = 0.6f
private const val WHITE_KEY_COUNT = 14

data class PianoKey(
    val name: String,
    val isBlack: Boolean,
    val whiteIndex: Int,
    val offsetRatio: Float = BLACK_KEY_OFFSET_RATIO // Offset relative to white key width for black keys
)

val pianoKeys = listOf(
    // Octave 4
    PianoKey("C4", false, 0),
    PianoKey("C#4", true, 0),
    PianoKey("D4", false, 1),
    PianoKey("D#4", true, 1),
    PianoKey("E4", false, 2),
    PianoKey("F4", false, 3),
    PianoKey("F#4", true, 3),
    PianoKey("G4", false, 4),
    PianoKey("G#4", true, 4),
    PianoKey("A4", false, 5),
    PianoKey("A#4", true, 5),
    PianoKey("B4", false, 6),
    
    // Octave 5
    PianoKey("C5", false, 7),
    PianoKey("C#5", true, 7),
    PianoKey("D5", false, 8),
    PianoKey("D#5", true, 8),
    PianoKey("E5", false, 9),
    PianoKey("F5", false, 10),
    PianoKey("F#5", true, 10),
    PianoKey("G5", false, 11),
    PianoKey("G#5", true, 11),
    PianoKey("A5", false, 12),
    PianoKey("A#5", true, 12),
    PianoKey("B5", false, 13)
)

val noteColors = listOf(
    Color(0xFFFF595E), Color(0xFFFF924C), Color(0xFFFFCA3A),
    Color(0xFF8AC926), Color(0xFF1982C4), Color(0xFF6A4C93)
)

data class KeySliderInfo(
    val noteName: String,
    val heightRatio: Float,
    val yOffsetRatio: Float,
    val colorIndex: Int
)

@Composable
fun PianoKeyboardView(
    modifier: Modifier = Modifier,
    highlightedNote: String? = null,
    wrongNote: String? = null,
    isLessonMode: Boolean = false,
    keySliders: List<KeySliderInfo> = emptyList(),
    onNotePlayed: (String) -> Unit = {},
    onNoteReleased: (String) -> Unit = {}
) {
    val pressedKeys = remember { mutableStateMapOf<Long, String>() }
    val currentOnNotePlayed by rememberUpdatedState(newValue = onNotePlayed)
    val currentOnNoteReleased by rememberUpdatedState(newValue = onNoteReleased)
    val effectiveLessonMode = isLessonMode || highlightedNote != null || keySliders.isNotEmpty()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(DarkCharcoal)
            .padding(top = 16.dp, bottom = 16.dp, start = 8.dp, end = 8.dp)
            .pointerInput(effectiveLessonMode) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Main)
                        val changes = event.changes

                        changes.forEach { change ->
                            if (change.pressed) {
                                // Calculate which key is pressed
                                val pt = change.position
                                val trackHeight = size.height * (if (effectiveLessonMode) 0.3f else 0f)
                                if (pt.y < trackHeight) return@forEach
                                val keysHeight = size.height - trackHeight

                                val wWidth = size.width / WHITE_KEY_COUNT.toFloat()
                                val bWidth = wWidth * BLACK_KEY_WIDTH_RATIO
                                val bHeight = keysHeight * BLACK_KEY_HEIGHT_RATIO

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
                                        if (previousKey != null) {
                                            currentOnNoteReleased(previousKey)
                                        }
                                        pressedKeys[change.id.value] = hitKey
                                        currentOnNotePlayed(hitKey)
                                    }
                                }
                            } else {
                                val releasedNote = pressedKeys.remove(change.id.value)
                                if (releasedNote != null) currentOnNoteReleased(releasedNote)
                            }
                        }
                    }
                }
            }
    ) {
        val trackHeight = if (effectiveLessonMode) maxHeight * 0.3f else 0.dp
        val keysHeight = maxHeight - trackHeight

        val wWidth = maxWidth / WHITE_KEY_COUNT
        val bWidth = wWidth * BLACK_KEY_WIDTH_RATIO
        val wHeight = keysHeight
        val bHeight = keysHeight * BLACK_KEY_HEIGHT_RATIO

        // Vertical Timer / Sliders above specific keys
        if (trackHeight > 0.dp) {
            val groupedSliders = keySliders.groupBy { it.noteName }
            groupedSliders.forEach { (noteName, slidersOnKey) ->
                val key = pianoKeys.find { it.name == noteName }
                if (key != null) {
                    val startX = if (key.isBlack) {
                        wWidth * key.whiteIndex + wWidth * key.offsetRatio
                    } else {
                        wWidth * key.whiteIndex
                    }
                    val keyWidth = if (key.isBlack) bWidth else wWidth

                    Box(
                        modifier = Modifier
                            .offset(x = startX + keyWidth / 2f - 12.dp, y = 16.dp)
                            .width(24.dp)
                            .height((trackHeight - 32.dp).coerceAtLeast(0.dp))
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    ) {
                        // Drawing them in reverse order of size or just staggered
                        slidersOnKey.reversed().forEachIndexed { index, slider ->
                            val sliderColor = noteColors[slider.colorIndex % noteColors.size]
                            val inset = (index * 4).dp // Make subsequent ones thinner so they show inside!
                            
                            // To map yOffsetRatio (distance from bottom) to a graphic:
                            // we can position a Box within the track using an offset from bottom.
                            // But align(Alignment.BottomCenter) doesn't support percentage offset from bottom out of the box in Modifier natively,
                            // except by doing fraction layout, or we can just use BoxWithConstraints or fillMaxHeight and spacer.
                            // Simpler: use a Spacer to push it up inside a Column, or just use Modifier.align(BottomCenter) + Spacer.
                            // Wait, BoxWithConstraints provides maxHeight. Since we know trackHeight value, we can use absolute offset in dp!
                            
                            // The track height is `(trackHeight - 32.dp).coerceAtLeast(0.dp)`
                            val availableTrackHeight = (trackHeight - 32.dp).coerceAtLeast(0.dp)
                            val yOffsetDp = availableTrackHeight * slider.yOffsetRatio
                            
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(y = -yOffsetDp)
                                    .padding(horizontal = inset)
                                    .fillMaxWidth()
                                    .height(availableTrackHeight * slider.heightRatio)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(sliderColor)
                            )
                        }
                    }
                }
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
                        .offset(x = arrowX - 16.dp, y = trackHeight - 40.dp + arrowOffsetY.dp)
                )
            }
        }
    }
}
