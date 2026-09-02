package com.example.game.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.audio.GameAudio
import com.example.game.model.*
import com.example.game.repository.GamePreferences

@Composable
fun GarageScreen(
    preferences: GamePreferences,
    audio: GameAudio,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCarId by remember { mutableStateOf(preferences.selectedCarId) }
    var totalCoins by remember { mutableStateOf(preferences.totalCoins) }

    val currentCar = AVAILABLE_CARS.find { it.id == selectedCarId } ?: AVAILABLE_CARS.first()
    val isUnlocked = preferences.isCarUnlocked(currentCar.id)

    // Current car color
    var currentColorLong by remember(selectedCarId) {
        mutableStateOf(preferences.getCarColor(currentCar.id, currentCar.defaultColor))
    }

    // Upgrades
    var engineLvl by remember(selectedCarId) {
        mutableStateOf(preferences.getUpgradeLevel(currentCar.id, "engine"))
    }
    var handlingLvl by remember(selectedCarId) {
        mutableStateOf(preferences.getUpgradeLevel(currentCar.id, "handling"))
    }
    var nitroLvl by remember(selectedCarId) {
        mutableStateOf(preferences.getUpgradeLevel(currentCar.id, "nitro"))
    }

    Scaffold(
        containerColor = Color(0xFF0A0F1D),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .testTag("garage_back_button")
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0x33FFFFFF))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = Color.White
                    )
                }

                Text(
                    text = "GARAGE TURBO",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )

                // Coin Balance
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x33FFD700))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
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
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // Car Selection Carousel
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                items(AVAILABLE_CARS) { car ->
                    val isSelected = car.id == selectedCarId
                    val carUnlocked = preferences.isCarUnlocked(car.id)
                    val cardColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF111827)

                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .testTag("car_card_${car.id}")
                            .width(130.dp)
                            .clickable {
                                selectedCarId = car.id
                                if (carUnlocked) {
                                    preferences.selectedCarId = car.id
                                }
                            }
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) Color(0xFF00F0FF) else Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (carUnlocked) Icons.Default.DirectionsCar else Icons.Default.Lock,
                                contentDescription = car.name,
                                tint = if (carUnlocked) Color(car.defaultColor) else Color(0xFF64748B),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = car.name,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (carUnlocked) "Débloqué" else "${car.price} pièces",
                                fontSize = 11.sp,
                                color = if (carUnlocked) Color(0xFF00E676) else Color(0xFFFFD700)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Car Display Showroom Plate
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Circular showroom pedestal
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width * 0.5f
                    val cy = size.height * 0.62f
                    // Pedestal glow
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(currentColorLong).copy(alpha = 0.5f), Color.Transparent),
                            center = Offset(cx, cy),
                            radius = size.width * 0.45f
                        ),
                        topLeft = Offset(cx - size.width * 0.45f, cy - 35f),
                        size = Size(size.width * 0.9f, 70f)
                    )
                    // Pedestal base
                    drawOval(
                        color = Color(0xFF2B374E),
                        topLeft = Offset(cx - 150f, cy - 25f),
                        size = Size(300f, 50f)
                    )

                    // Render Car Preview
                    val carW = 90f
                    val carH = 150f
                    val carCanvasY = cy - 20f

                    // Shadow
                    drawRoundRect(
                        color = Color(0x66000000),
                        topLeft = Offset(cx - carW * 0.5f, carCanvasY - carH * 0.4f + 10f),
                        size = Size(carW, carH * 0.9f),
                        cornerRadius = CornerRadius(14f, 14f)
                    )

                    // Tires
                    val tireColor = Color(0xFF151921)
                    drawRoundRect(tireColor, Offset(cx - carW * 0.55f, carCanvasY - carH * 0.42f), Size(20f, 36f), CornerRadius(6f, 6f))
                    drawRoundRect(tireColor, Offset(cx + carW * 0.35f, carCanvasY - carH * 0.42f), Size(20f, 36f), CornerRadius(6f, 6f))
                    drawRoundRect(tireColor, Offset(cx - carW * 0.55f, carCanvasY + carH * 0.16f), Size(20f, 36f), CornerRadius(6f, 6f))
                    drawRoundRect(tireColor, Offset(cx + carW * 0.35f, carCanvasY + carH * 0.16f), Size(20f, 36f), CornerRadius(6f, 6f))

                    // Body
                    val bodyColor = Color(currentColorLong)
                    val bodyPath = Path().apply {
                        moveTo(cx - carW * 0.35f, carCanvasY - carH * 0.48f)
                        quadraticTo(cx, carCanvasY - carH * 0.52f, cx + carW * 0.35f, carCanvasY - carH * 0.48f)
                        lineTo(cx + carW * 0.46f, carCanvasY - carH * 0.15f)
                        lineTo(cx + carW * 0.42f, carCanvasY + carH * 0.44f)
                        quadraticTo(cx, carCanvasY + carH * 0.48f, cx - carW * 0.42f, carCanvasY + carH * 0.44f)
                        lineTo(cx - carW * 0.46f, carCanvasY - carH * 0.15f)
                        close()
                    }
                    drawPath(bodyPath, color = bodyColor)

                    // Racing Stripe
                    drawRect(
                        color = Color(currentCar.accentColor),
                        topLeft = Offset(cx - carW * 0.08f, carCanvasY - carH * 0.46f),
                        size = Size(carW * 0.16f, carH * 0.90f)
                    )

                    // Cockpit Glass
                    drawRoundRect(
                        color = Color(0xFF0F1E2E),
                        topLeft = Offset(cx - carW * 0.30f, carCanvasY - carH * 0.20f),
                        size = Size(carW * 0.60f, carH * 0.38f),
                        cornerRadius = CornerRadius(8f, 8f)
                    )

                    // Spoiler
                    drawRoundRect(
                        color = Color(0xFF1E232E),
                        topLeft = Offset(cx - carW * 0.40f, carCanvasY + carH * 0.44f),
                        size = Size(carW * 0.80f, carH * 0.08f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }

                // Car info label overlay
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = currentCar.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = currentCar.description,
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.widthIn(max = 240.dp)
                    )
                }

                // Unlock Button if locked
                if (!isUnlocked) {
                    Button(
                        onClick = {
                            if (preferences.spendCoins(currentCar.price)) {
                                preferences.unlockCar(currentCar.id)
                                preferences.selectedCarId = currentCar.id
                                totalCoins = preferences.totalCoins
                                audio.playUpgrade()
                            }
                        },
                        enabled = totalCoins >= currentCar.price,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD700),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .testTag("unlock_car_button")
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Débloquer (${currentCar.price})", fontWeight = FontWeight.Bold)
                    }
                } else if (preferences.selectedCarId != currentCar.id) {
                    Button(
                        onClick = {
                            preferences.selectedCarId = currentCar.id
                            selectedCarId = currentCar.id
                            audio.playCoin()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F0FF)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .testTag("select_car_button")
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                    ) {
                        Text("Sélectionner", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Paint Customization
            Text(
                text = "PEINTURE DU VÉHICULE",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(CAR_PAINTS) { paintColor ->
                    val isSelected = currentColorLong == paintColor
                    Box(
                        modifier = Modifier
                            .testTag("paint_color_${paintColor}")
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(paintColor))
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.White else Color(0x44FFFFFF),
                                shape = CircleShape
                            )
                            .clickable {
                                currentColorLong = paintColor
                                preferences.setCarColor(currentCar.id, paintColor)
                                audio.playCoin()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Vehicle Upgrades
            Text(
                text = "AMÉLIORATIONS MÉCANIQUES",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Engine Upgrade
            UpgradeCard(
                title = "Moteur (Vitesse)",
                level = engineLvl,
                maxLevel = 5,
                cost = 100 + (engineLvl * 80),
                totalCoins = totalCoins,
                onUpgrade = { cost ->
                    if (preferences.spendCoins(cost)) {
                        preferences.incrementUpgrade(currentCar.id, "engine")
                        engineLvl++
                        totalCoins = preferences.totalCoins
                        audio.playUpgrade()
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Handling Upgrade
            UpgradeCard(
                title = "Direction (Maniabilité)",
                level = handlingLvl,
                maxLevel = 5,
                cost = 90 + (handlingLvl * 75),
                totalCoins = totalCoins,
                onUpgrade = { cost ->
                    if (preferences.spendCoins(cost)) {
                        preferences.incrementUpgrade(currentCar.id, "handling")
                        handlingLvl++
                        totalCoins = preferences.totalCoins
                        audio.playUpgrade()
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // Nitro Upgrade
            UpgradeCard(
                title = "Turbocompresseur (Nitro)",
                level = nitroLvl,
                maxLevel = 5,
                cost = 120 + (nitroLvl * 90),
                totalCoins = totalCoins,
                onUpgrade = { cost ->
                    if (preferences.spendCoins(cost)) {
                        preferences.incrementUpgrade(currentCar.id, "nitro")
                        nitroLvl++
                        totalCoins = preferences.totalCoins
                        audio.playUpgrade()
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun UpgradeCard(
    title: String,
    level: Int,
    maxLevel: Int,
    cost: Int,
    totalCoins: Int,
    onUpgrade: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isMax = level >= maxLevel
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                // Progress blocks
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (i in 1..maxLevel) {
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (i <= level) Color(0xFF00F0FF) else Color(0xFF334155)
                                )
                        )
                    }
                }
            }

            if (!isMax) {
                Button(
                    onClick = { onUpgrade(cost) },
                    enabled = totalCoins >= cost,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "$cost",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Text(
                    text = "MAX",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = Color(0xFFFFD700)
                )
            }
        }
    }
}
