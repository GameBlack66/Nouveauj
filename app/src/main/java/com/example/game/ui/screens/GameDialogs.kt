package com.example.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.engine.GameStateSnapshot

@Composable
fun GameOverDialog(
    state: GameStateSnapshot,
    onRestart: () -> Unit,
    onGarage: () -> Unit,
    onHome: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFFF1E44), Color(0xFFFF8500))
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "COURSE TERMINÉE",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // New Record Banner
                if (state.isNewHighScore) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x33FFD700))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("NOUVEAU RECORD !", fontWeight = FontWeight.Black, fontSize = 14.sp, color = Color(0xFFFFD700))
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Final Score
                Text(
                    text = "SCORE FINAL",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = "${state.score}",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats row: Distance & Coins
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E293B))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Distance", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        Text(
                            text = "${state.distanceMeters.toInt()} m",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Divider(
                        modifier = Modifier
                            .height(36.dp)
                            .width(1.dp),
                        color = Color(0xFF334155)
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Pièces", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "+${state.coins}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .testTag("game_over_retry_button")
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Replay, contentDescription = null, tint = Color.Black)
                    Spacer(Modifier.width(8.dp))
                    Text("REJOUER", fontWeight = FontWeight.Black, fontSize = 16.sp, color = Color.Black)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onGarage,
                        modifier = Modifier
                            .testTag("game_over_garage_button")
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Garage", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onHome,
                        modifier = Modifier
                            .testTag("game_over_home_button")
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Menu", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PauseDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit
) {
    Dialog(onDismissRequest = onResume) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PAUSE",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onResume,
                    modifier = Modifier
                        .testTag("resume_button")
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                    Spacer(Modifier.width(8.dp))
                    Text("REPRENDRE", fontWeight = FontWeight.Black, color = Color.Black)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onRestart,
                    modifier = Modifier
                        .testTag("pause_restart_button")
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Replay, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Recommencer", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = onHome,
                    modifier = Modifier.testTag("pause_home_button")
                ) {
                    Text("Quitter vers le Menu", color = Color(0xFF94A3B8), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
