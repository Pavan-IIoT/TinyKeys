package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppDatabase
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    onFreePlayClick: () -> Unit,
    onLearnSongClick: () -> Unit,
    onMyStarsClick: () -> Unit
) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.getDatabase(context).songScoreDao() }
    val scores by dao.getAllScores().collectAsState(initial = emptyList())
    val totalStars = scores.sumOf { it.stars }

    val infiniteTransition = rememberInfiniteTransition(label = "bounce")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutBack),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ThemeBackground)
    ) {
        // Decorative background blobs
        Box(
            modifier = Modifier
                .offset(x = (-50).dp, y = (-50).dp)
                .size(250.dp)
                .background(Color(0xFFE1F5FE), CircleShape)
                .scale(scale)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 50.dp, y = 50.dp)
                .size(200.dp)
                .background(Color(0xFFF3E5F5), CircleShape)
                .scale(scale)
        )

        // Top right star tracker
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
                .background(Color.White, RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = totalStars.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD97706) // Amber 600
            )
            Text(text = "⭐", fontSize = 24.sp)
        }

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header area
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Mascot
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(MascotSkyBlue, RoundedCornerShape(24.dp))
                            .padding(4.dp)
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        Row(
                            modifier = Modifier.align(Alignment.Center).offset(y = (-10).dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(modifier = Modifier.size(12.dp, 16.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                                Box(modifier = Modifier.size(6.dp).offset(y = 2.dp).background(MascotDarkSlate, CircleShape))
                            }
                            Box(modifier = Modifier.size(12.dp, 16.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                                Box(modifier = Modifier.size(6.dp).offset(y = 2.dp).background(MascotDarkSlate, CircleShape))
                            }
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = (-20).dp)
                                .size(24.dp, 8.dp)
                                .background(Color.White, RoundedCornerShape(50))
                        )
                    }
                    Text(
                        text = "🎵",
                        fontSize = 32.sp,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 10.dp, y = (-10).dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(24.dp))
                
                // Title
                Column {
                    Text(
                        text = "TinyKeys 🎹",
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Black,
                        color = ThemeOrangeText,
                        modifier = Modifier.scale(scale)
                    )
                    Text(
                        text = "Let's make some music!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemeBrownText,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(top = 4.dp).alpha(0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Navigation Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HomeMenuButton(
                    icon = "🎹",
                    title = "Free Play",
                    subtitle = "EXPLORE SOUNDS",
                    baseColor = BtnGreenBase,
                    borderColor = BtnGreenBorder,
                    onClick = onFreePlayClick
                )
                HomeMenuButton(
                    icon = "🌟",
                    title = "Learn a Song",
                    subtitle = "STEP BY STEP",
                    baseColor = BtnOrangeBase,
                    borderColor = BtnOrangeBorder,
                    onClick = onLearnSongClick
                )
                HomeMenuButton(
                    icon = "⭐",
                    title = "My Stars",
                    subtitle = "TROPHIES",
                    baseColor = BtnPurpleBase,
                    borderColor = BtnPurpleBorder,
                    onClick = onMyStarsClick
                )
            }
        }
    }
}

@Composable
fun HomeMenuButton(
    icon: String,
    title: String,
    subtitle: String,
    baseColor: Color,
    borderColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val offsetY by animateDpAsState(
        targetValue = if (isPressed) 6.dp else 0.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "press_offset"
    )

    Box(
        modifier = Modifier
            .width(240.dp)
            .height(100.dp)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        // Shadow/Border layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = 8.dp)
                .background(borderColor, RoundedCornerShape(24.dp))
        )
        // Main button layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = offsetY)
                .background(baseColor, RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icon Box
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = 24.sp)
                }
                
                // Text Area
                Column {
                    Text(
                        text = title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f),
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
