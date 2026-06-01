package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SongRepository
import com.example.ui.theme.DarkCharcoal
import com.example.ui.theme.PastelYellow
import com.example.ui.theme.SoftWhite

@Composable
fun SongListScreen(
    onBack: () -> Unit,
    onSongSelected: (String) -> Unit
) {
    val songs = SongRepository.songs

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PastelYellow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.size(32.dp))
            }
            Text("Choose a Song", fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp))
        }
        
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(songs) { song ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSongSelected(song.id) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftWhite)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = song.name, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = DarkCharcoal)
                            Text(text = "${song.notes.size} Notes", fontSize = 18.sp, color = DarkCharcoal.copy(alpha = 0.7f))
                        }
                        Row {
                            repeat(song.difficultyStars) {
                                Text("⭐", fontSize = 24.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
