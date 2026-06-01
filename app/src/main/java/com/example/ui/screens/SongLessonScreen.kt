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

@Composable
fun SongLessonScreen(
    songId: String,
    onBack: () -> Unit,
    onChooseAnother: () -> Unit
) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).songScoreDao() }
    val scope = rememberCoroutineScope()
    
    val song = remember { SongRepository.getSong(songId)!! }
    var currentNoteIndex by remember { mutableIntStateOf(0) }
    var mistakes by remember { mutableIntStateOf(0) }
    var isFinished by remember { mutableStateOf(false) }
    var lastWrongNote by remember { mutableStateOf<String?>(null) }

    val nextNote = if (currentNoteIndex < song.notes.size) song.notes[currentNoteIndex] else null
    val progress = if (song.notes.isEmpty()) 0f else currentNoteIndex.toFloat() / song.notes.size

    val handleNotePlayed: (String) -> Unit = { note ->
        if (!isFinished) {
            if (note == nextNote) {
                currentNoteIndex++
                if (currentNoteIndex >= song.notes.size) {
                    isFinished = true
                    val starsEarned = when {
                        mistakes == 0 -> 3
                        mistakes <= 3 -> 2
                        else -> 1
                    }
                    scope.launch {
                        val existing = dao.getScore(songId)
                        val maxStars = maxOf(existing?.stars ?: 0, starsEarned)
                        dao.insertScore(SongScore(songId, maxStars))
                    }
                }
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

    Box(modifier = Modifier.fillMaxSize().background(PastelBlue)) {
        PianoKeyboardView(
            modifier = Modifier.fillMaxSize(),
            highlightedNote = nextNote,
            wrongNote = lastWrongNote,
            upcomingNotes = song.notes,
            currentNoteIndex = currentNoteIndex,
            onNotePlayed = handleNotePlayed
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
                    
                    val stars = when {
                        mistakes == 0 -> 3
                        mistakes <= 3 -> 2
                        else -> 1
                    }
                    
                    Text("You earned $stars stars!", fontSize = 32.sp, modifier = Modifier.padding(vertical = 16.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(onClick = {
                            currentNoteIndex = 0
                            mistakes = 0
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
