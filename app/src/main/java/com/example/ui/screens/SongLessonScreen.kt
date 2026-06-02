package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.data.AppDatabase
import com.example.data.SongRepository
import com.example.data.SongScore
import com.example.ui.PianoKeyboardView
import com.example.ui.theme.DarkCharcoal
import com.example.ui.theme.PastelBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class NoteState { UPCOMING, ACTIVE, DONE }

data class NoteSchedule(val index: Int, val note: String, val durationBeats: Float, val scheduledTimeMs: Long)

@Composable
fun SongLessonScreen(
    songId: String,
    onBack: () -> Unit,
    onChooseAnother: () -> Unit
) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).songScoreDao() }
    val scope = rememberCoroutineScope()
    
    val song = remember { SongRepository.getSong(songId) }
    if (song == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    var currentElapsedTimeMs by remember { mutableLongStateOf(-2500L) }
    var mistakes by remember { mutableIntStateOf(0) }
    var isFinished by remember { mutableStateOf(false) }
    var lastWrongNote by remember { mutableStateOf<String?>(null) }
    
    val noteSchedules = remember(song) {
        val schedules = mutableListOf<NoteSchedule>()
        var currentBeat = 0f
        val msPerBeat = 60000f / song.bpm
        for (i in song.notes.indices) {
            val n = song.notes[i]
            val timeMs = (currentBeat * msPerBeat).toLong()
            schedules.add(NoteSchedule(i, n.note, n.durationBeats, timeMs))
            currentBeat += n.durationBeats
        }
        schedules
    }
    
    val noteStates = remember { mutableStateMapOf<Int, NoteState>() }

    LaunchedEffect(isFinished) {
        if (!isFinished) {
            var lastFrameTime = withFrameMillis { it }
            val msPerBeat = 60000f / song.bpm
            while (true) {
                val currentFrameTime = withFrameMillis { it }
                val delta = currentFrameTime - lastFrameTime
                lastFrameTime = currentFrameTime
                
                currentElapsedTimeMs += delta
                
                val nextUpcoming = noteSchedules.firstOrNull { noteStates[it.index] == null || noteStates[it.index] == NoteState.UPCOMING }
                if (nextUpcoming != null && currentElapsedTimeMs > nextUpcoming.scheduledTimeMs) {
                    currentElapsedTimeMs = nextUpcoming.scheduledTimeMs
                }
                
                noteSchedules.forEach { ns ->
                    val state = noteStates[ns.index] ?: NoteState.UPCOMING
                    if (state == NoteState.ACTIVE) {
                        val timeHeldMs = currentElapsedTimeMs - ns.scheduledTimeMs
                        val totalHoldMs = ns.durationBeats * msPerBeat
                        if (timeHeldMs >= totalHoldMs) {
                            noteStates[ns.index] = NoteState.DONE
                        }
                    }
                }
                
                if (noteSchedules.all { noteStates[it.index] == NoteState.DONE }) {
                    val lastSchedule = noteSchedules.lastOrNull()
                    if (lastSchedule != null) {
                        val totalSongTimeMs = lastSchedule.scheduledTimeMs + (lastSchedule.durationBeats * msPerBeat).toLong()
                        if (currentElapsedTimeMs >= totalSongTimeMs) {
                            if (!isFinished) {
                                isFinished = true
                            }
                        }
                    }
                }
            }
        }
    }

    val starsEarned = remember(mistakes) {
        when {
            mistakes == 0 -> 3
            mistakes <= 3 -> 2
            else -> 1
        }
    }

    LaunchedEffect(isFinished) {
        if (isFinished) {
            val existing = dao.getScore(songId)
            val maxStars = maxOf(existing?.stars ?: 0, starsEarned)
            dao.insertScore(SongScore(songId, maxStars))
        }
    }

    val msPerBeatForDuration = 60000f / song.bpm
    val nextUpcomingSchedule = noteSchedules.firstOrNull { noteStates[it.index] == null || noteStates[it.index] == NoteState.UPCOMING }
    val nextNote = nextUpcomingSchedule?.note ?: noteSchedules.firstOrNull { noteStates[it.index] == NoteState.ACTIVE }?.note

    val handleNotePlayed: (String) -> Unit = { note ->
        if (!isFinished) {
            val hitCandidate = nextUpcomingSchedule?.takeIf { 
                it.note == note && (it.scheduledTimeMs - currentElapsedTimeMs <= 500)
            }
            if (hitCandidate != null) {
                noteStates[hitCandidate.index] = NoteState.ACTIVE
                SoundManager.playNote(note, (hitCandidate.durationBeats * msPerBeatForDuration).toInt())
            } else {
                mistakes++
                lastWrongNote = note
                scope.launch {
                    delay(500)
                    if (lastWrongNote == note) {
                        lastWrongNote = null
                    }
                }
            }
        }
    }

    val handleNoteReleased: (String) -> Unit = { note ->
        noteSchedules.filter { noteStates[it.index] == NoteState.ACTIVE }.forEach { activeNs ->
            if (activeNs.note == note) {
                noteStates[activeNs.index] = NoteState.DONE
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(PastelBlue)) {
        val msPerBeat = 60000f / song.bpm
        val maxDurationMs = msPerBeat * 4f // 4 beats = full height

        // Calculate hit count per note index
        val hitIndices = remember(song) {
            val counts = mutableMapOf<String, Int>()
            song.notes.mapIndexed { index, schedule ->
                val count = counts.getOrDefault(schedule.note, 0)
                counts[schedule.note] = count + 1
                index to count
            }.toMap()
        }

        val keySliders = mutableListOf<com.example.ui.KeySliderInfo>()

        // Get currently active notes
        val activeSchedules = noteSchedules.filter { noteStates[it.index] == NoteState.ACTIVE }
        // Get the next upcoming note(s) -- at most 2 to preview next in series
        val upcomingSchedules = noteSchedules.filter { noteStates[it.index] == NoteState.UPCOMING }.take(2)

        val notesToShow = activeSchedules + upcomingSchedules

        // Sort by duration descending so larger sliders draw first (behind) in case of same note
        val sortedNotesToShow = notesToShow.sortedByDescending { it.durationBeats }

        sortedNotesToShow.forEach { schedule ->
            val state = noteStates[schedule.index]
            val totalHoldMs = schedule.durationBeats * msPerBeat

            val yOffsetRatio: Float
            val heightRatio: Float

            if (state == NoteState.ACTIVE) {
                // Bottom is at the key (0 offset). The total height shrinks as it is held.
                val timeHeldMs = currentElapsedTimeMs - schedule.scheduledTimeMs
                val remainingMs = totalHoldMs - timeHeldMs
                yOffsetRatio = 0f
                heightRatio = (remainingMs / maxDurationMs).coerceIn(0f, 1f)
            } else {
                // Upcoming: it's above the key.
                val timeUntilHit = schedule.scheduledTimeMs - currentElapsedTimeMs
                yOffsetRatio = (timeUntilHit / maxDurationMs).coerceIn(0f, 1f)
                heightRatio = (totalHoldMs / maxDurationMs).coerceIn(0f, 1f)
            }

            if (heightRatio > 0.01f) {
                keySliders.add(com.example.ui.KeySliderInfo(schedule.note, heightRatio, yOffsetRatio, hitIndices[schedule.index] ?: 0))
            }
        }

        PianoKeyboardView(
            modifier = Modifier.fillMaxSize(),
            highlightedNote = nextNote,
            wrongNote = lastWrongNote,
            isLessonMode = true,
            keySliders = keySliders,
            onNotePlayed = handleNotePlayed,
            onNoteReleased = handleNoteReleased
        )

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(32.dp), tint = Color.White)
        }

        AnimatedVisibility(
            visible = isFinished,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.padding(32.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Well done! 🌟", fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    
                    Text("You earned $starsEarned stars!", fontSize = 32.sp, modifier = Modifier.padding(vertical = 16.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(onClick = {
                            currentElapsedTimeMs = -2500L
                            mistakes = 0
                            noteStates.clear()
                            isFinished = false
                        }) {
                            Text("Play Again", fontSize = 20.sp)
                        }
                        Button(onClick = onChooseAnother) {
                            Text("Choose Another Song", fontSize = 20.sp)
                        }
                    }
                }
            }
        }
    }
}
