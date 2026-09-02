package com.example.game.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.audio.GameAudio
import com.example.game.model.AVAILABLE_CARS
import com.example.game.model.GameMode
import com.example.game.repository.ControlType
import com.example.game.repository.GamePreferences

@Composable
fun HomeScreen(
    preferences: GamePreferences,
    audio: GameAudio,
    onStartGame: (GameMode) -> Unit,
    onOpenGarage: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedMode by remember { mutableStateOf(GameMode.ENDLESS) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val totalCoins = preferences.totalCoins
    val selectedCar = AVAILABLE_CARS.find { it.id == preferences.selectedCarId } ?: AVAILABLE_CARS.first()
    val carColor = preferences.getCarColor(selectedCar.id, selectedCar.defaultColor)

    val currentHighScore = preferences.getHighScore(selectedMode.name)
    val currentBestDist = preferences.getBestDistance(selectedMode.name)

    Scaffold(
        containerColor = Color(0xFF070B19),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Game Title Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF1E44)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TURBO RACER",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.SansSerif,
                        color = Color.White
                    )
                }

                // Coins & Settings
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0x33FFD700))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = "Pièces",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$totalCoins",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFFFFD700)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier
                            .testTag("settings_button")
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0x33FFFFFF))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Paramètres",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Hero Banner Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.turbo_racing_banner_1788379325038),
                        contentDescription = "Turbo Racing Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient Scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color(0xDD070B19))
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "DÉFIEZ L'ASPHALTE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF00F0FF)
                        )
                        Text(
                            text = "Vitesse • Réflexes • Nitro",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Best Score Card
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF151D2A)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "MEILLEUR SCORE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$currentHighScore PTS",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFFFD700)
                        )
                    }

                    Divider(
                        modifier = Modifier
                            .height(36.dp)
                            .width(1.dp),
                        color = Color(0xFF334155)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "MEILLEURE DISTANCE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${currentBestDist.toInt()} m",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF00E676)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Equipped Car Card (Garage Shortcut)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenGarage() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(carColor)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = selectedCar.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Vitesse: ${selectedCar.baseSpeed.toInt()} km/h",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Button(
                        onClick = onOpenGarage,
                        modifier = Modifier.testTag("home_garage_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Garage", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Game Mode Selection Header
            Text(
                text = "CHOIX DU MODE DE JEU",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Game Modes
            GameMode.entries.forEach { mode ->
                val isSelected = selectedMode == mode
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF111827)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .testTag("mode_${mode.name}")
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            selectedMode = mode
                            audio.playCoin()
                        }
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) Color(0xFF00F0FF) else Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (mode) {
                                GameMode.ENDLESS -> Icons.Default.Speed
                                GameMode.TIME_ATTACK -> Icons.Default.Timer
                                GameMode.POLICE_CHASE -> Icons.Default.LocalPolice
                            },
                            contentDescription = mode.displayName,
                            tint = when (mode) {
                                GameMode.ENDLESS -> Color(0xFFFF8500)
                                GameMode.TIME_ATTACK -> Color(0xFF00F0FF)
                                GameMode.POLICE_CHASE -> Color(0xFFFF1744)
                            },
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mode.displayName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = mode.description,
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF00F0FF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Play Button
            Button(
                onClick = { onStartGame(selectedMode) },
                modifier = Modifier
                    .testTag("play_button")
                    .fillMaxWidth()
                    .height(58.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF1E44)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "DÉMARRER LA COURSE",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Settings Dialog
    if (showSettingsDialog) {
        var soundEnabled by remember { mutableStateOf(preferences.isSoundEnabled) }
        var hapticsEnabled by remember { mutableStateOf(preferences.isHapticsEnabled) }

        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Text(
                    text = "PARAMÈTRES",
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            },
            text = {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Effets Sonores", color = Color.White)
                        Switch(
                            checked = soundEnabled,
                            onCheckedChange = {
                                soundEnabled = it
                                preferences.isSoundEnabled = it
                                audio.isSoundEnabled = it
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Vibrations Haptiques", color = Color.White)
                        Switch(
                            checked = hapticsEnabled,
                            onCheckedChange = {
                                hapticsEnabled = it
                                preferences.isHapticsEnabled = it
                                audio.isHapticsEnabled = it
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("OK", fontWeight = FontWeight.Bold, color = Color(0xFF00F0FF))
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}
