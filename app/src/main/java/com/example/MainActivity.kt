package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.audio.SoundManager
import com.example.ui.screens.FreePlayScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MyStarsScreen
import com.example.ui.screens.SongLessonScreen
import com.example.ui.screens.SongListScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    SoundManager.init()
    
    setContent {
      MyApplicationTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          val navController = rememberNavController()
          NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
          ) {
            composable("home") {
              HomeScreen(
                onFreePlayClick = { navController.navigate("free_play") },
                onLearnSongClick = { navController.navigate("song_list") },
                onMyStarsClick = { navController.navigate("my_stars") }
              )
            }
            composable("free_play") {
              FreePlayScreen(onBack = { navController.popBackStack() })
            }
            composable("song_list") {
              SongListScreen(
                onBack = { navController.popBackStack() },
                onSongSelected = { songId -> navController.navigate("lesson/$songId") }
              )
            }
            composable("lesson/{songId}") { backStackEntry ->
              val songId = backStackEntry.arguments?.getString("songId") ?: ""
              SongLessonScreen(
                songId = songId,
                onBack = { navController.popBackStack() },
                onChooseAnother = { 
                  navController.popBackStack("song_list", inclusive = false)
                }
              )
            }
            composable("my_stars") {
              MyStarsScreen(onBack = { navController.popBackStack() })
            }
          }
        }
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    SoundManager.release()
  }
}

