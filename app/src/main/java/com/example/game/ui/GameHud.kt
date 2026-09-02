package com.example.game.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.GameStateSnapshot
import com.example.game.model.GameMode

@Composable
fun GameHud(
    state: GameStateSnapshot,
    mode: GameMode,
    onSteerLeft: (Boolean) -> Unit,
    onSteerRight: (Boolean) -> Unit,
    onNitro: (Boolean) -> Unit,
    onBrake: (Boolean) -> Unit,
    onPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        // TOP HUD: Status, Health, Score, Pause
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score & Distance
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${state.score}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFFFD700)
                        )
                        Text(
                            text = " PTS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFCCCCCC)
                        )
                    }
                    Text(
                        text = "${state.distanceMeters.toInt()} m",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFB0BEC5)
                    )
                }

                // Time Attack Timer (if applicable)
                if (mode == GameMode.TIME_ATTACK) {
                    val isUrgent = state.timeRemaining < 10f
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isUrgent) Color(0xCCFF1744) else Color(0xAA1E293B)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Chrono",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%.1f s", state.timeRemaining),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                // Coins collected in run
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x77000000))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "Pièces",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "+${state.coins}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Pause Button
                IconButton(
                    onClick = onPause,
                    modifier = Modifier
                        .testTag("pause_button")
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0x881E293B))
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Health & Shield Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (state.isShieldActive) Icons.Default.Shield else Icons.Default.Favorite,
                    contentDescription = "Santé",
                    tint = if (state.isShieldActive) Color(0xFF00F0FF) else Color(0xFFFF3366),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                LinearProgressIndicator(
                    progress = { (state.health / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when {
                        state.isShieldActive -> Color(0xFF00F0FF)
                        state.health > 50f -> Color(0xFF00E676)
                        state.health > 25f -> Color(0xFFFF9100)
                        else -> Color(0xFFFF1744)
                    },
                    trackColor = Color(0x44FFFFFF)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${state.health.toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Active Power-ups Bar (Magnet / Shield)
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 90.dp, start = 16.dp)
        ) {
            if (state.isMagnetActive) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xCC9C27B0)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("AIMANT ACTIF", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
            if (state.isShieldActive) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xCC00B0FF)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("BOUCLIER ACTIF", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // Near-Miss Toast banner in center
        AnimatedVisibility(
            visible = state.nearMissToast != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 120.dp)
        ) {
            state.nearMissToast?.let { toast ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xEEFF3366)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Text(
                        text = toast,
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // BOTTOM HUD: Speedometer & Controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Speed & Nitro Gauges Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x990F172A))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Digital Speedometer
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${state.speedKmh.toInt()}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = if (state.isNitroActive) Color(0xFF00F0FF) else Color.White
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "KM/H",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF90A4AE),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // Nitro Gauge
                Column(modifier = Modifier.width(150.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "NITRO",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF00F0FF)
                        )
                        Text(
                            text = "${((state.nitro / state.maxNitro) * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00F0FF)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (state.nitro / state.maxNitro).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = Color(0xFF00F0FF),
                        trackColor = Color(0x3300F0FF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Control Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Steer Button
                Box(
                    modifier = Modifier
                        .testTag("steer_left_button")
                        .size(width = 72.dp, height = 72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF334155), Color(0xFF1E293B))
                            )
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    onSteerLeft(true)
                                    tryAwaitRelease()
                                    onSteerLeft(false)
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Tourner à Gauche",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Right Steer Button
                Box(
                    modifier = Modifier
                        .testTag("steer_right_button")
                        .size(width = 72.dp, height = 72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF334155), Color(0xFF1E293B))
                            )
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    onSteerRight(true)
                                    tryAwaitRelease()
                                    onSteerRight(false)
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Tourner à Droite",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(0.3f))

                // Brake / Drift Button
                Box(
                    modifier = Modifier
                        .testTag("brake_button")
                        .size(width = 68.dp, height = 72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF881122), Color(0xFF4A0A12))
                            )
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    onBrake(true)
                                    tryAwaitRelease()
                                    onBrake(false)
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DoNotDisturbOn,
                            contentDescription = "Frein",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "FREIN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Nitro Button (Prominent Fire / Boost Button)
                Box(
                    modifier = Modifier
                        .testTag("nitro_button")
                        .size(width = 84.dp, height = 72.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                if (state.nitro > 5f) listOf(Color(0xFF00E5FF), Color(0xFF0072FF))
                                else listOf(Color(0xFF455A64), Color(0xFF263238))
                            )
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    onNitro(true)
                                    tryAwaitRelease()
                                    onNitro(false)
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Nitro",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "NITRO",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
