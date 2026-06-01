package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppDatabase
import com.example.data.SongRepository
import com.example.ui.theme.DarkCharcoal
import com.example.ui.theme.PastelPurple
import com.example.ui.theme.SoftWhite

@Composable
fun MyStarsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).songScoreDao() }
    val scores by dao.getAllScores().collectAsState(initial = emptyList())
    
    val totalStars = scores.sumOf { it.stars }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PastelPurple)
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
            Text("My Stars", fontSize = 32.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp))
            
            Spacer(modifier = Modifier.weight(1f))
            
            Text("⭐ Total: $totalStars", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        }
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(SongRepository.songs) { song ->
                val score = scores.find { it.songId == song.id }?.stars ?: 0
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SoftWhite)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = song.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkCharcoal)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (score > 0) {
                            Row {
                                repeat(score) {
                                    Text("⭐", fontSize = 28.sp)
                                }
                            }
                        } else {
                            Text("🔒 Play to unlock", fontSize = 16.sp, color = DarkCharcoal.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }
}
