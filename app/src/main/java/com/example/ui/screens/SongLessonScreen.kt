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
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.data.AppDatabase
import com.example.data.SongRepository
import com.example.data.SongScore
import com.example.ui.PianoKeyboardView
import com.example.ui.theme.DarkCharcoal
import com.example.ui.theme.PastelGreen
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

    val nextNote = if (currentNoteIndex < song.notes.size) song.notes[currentNoteIndex] else null
    val progress = if (song.notes.isEmpty()) 0f else currentNoteIndex.toFloat() / song.notes.size

    fun handleNotePlayed(note: String) {
        if (isFinished) return
        
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
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(PastelGreen)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(32.dp))
                }
                Text(song.name, fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp))
                
                Spacer(modifier = Modifier.weight(1f))
                
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .width(200.dp)
                        .height(16.dp)
                        .padding(end = 16.dp),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f),
                )
            }

            PianoKeyboardView(
                modifier = Modifier.weight(1f),
                highlightedNote = nextNote,
                onNotePlayed = ::handleNotePlayed
            )
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
