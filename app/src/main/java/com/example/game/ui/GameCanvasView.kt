package com.example.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.game.engine.GameStateSnapshot
import com.example.game.model.*
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun GameCanvasView(
    state: GameStateSnapshot,
    car: CarModel,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val horizonY = h * 0.16f

        // Apply screen shake
        val shakeX = if (state.screenShake > 0f) (Random.nextFloat() - 0.5f) * state.screenShake * 3f else 0f
        val shakeY = if (state.screenShake > 0f) (Random.nextFloat() - 0.5f) * state.screenShake * 3f else 0f

        withTransform({
            translate(shakeX, shakeY)
        }) {
            // 1. Draw Sky & Cyber Horizon
            drawSkyAndHorizon(w, h, horizonY, state.roadOffset)

            // 2. Draw 3D Perspective Highway
            drawPerspectiveRoad(w, h, horizonY, state.roadOffset)

            // 3. Draw Skid Marks
            drawSkidMarks(w, h, horizonY, state.skidMarks)

            // 4. Draw Collectibles
            state.collectibles.forEach { item ->
                drawCollectible(w, h, horizonY, item)
            }

            // 5. Draw Traffic Vehicles
            state.traffic.forEach { tv ->
                drawTrafficVehicle(w, h, horizonY, tv)
            }

            // 6. Draw Player Vehicle
            drawPlayerVehicle(w, h, horizonY, state, car)

            // 7. Draw Visual Particles
            state.particles.forEach { p ->
                val pCanvasX = roadXToCanvasX(w, horizonY, p.x, p.y)
                val pCanvasY = horizonY + (p.y * (h - horizonY))
                drawCircle(
                    color = Color(p.color).copy(alpha = p.life.coerceIn(0f, 1f)),
                    radius = p.size,
                    center = Offset(pCanvasX, pCanvasY)
                )
            }

            // 8. Damage flash overlay
            if (state.damageFlash) {
                drawRect(
                    color = Color(0x66FF0033),
                    size = size
                )
            }
        }
    }
}

private fun DrawScope.drawSkyAndHorizon(w: Float, h: Float, horizonY: Float, offset: Float) {
    // Gradient sky
    val skyGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF070B19),
            Color(0xFF151936),
            Color(0xFF2C1E4A),
            Color(0xFFFF3366)
        ),
        startY = 0f,
        endY = horizonY
    )
    drawRect(brush = skyGradient, size = Size(w, horizonY))

    // Neon horizon glow sun
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFEE55), Color(0xFFFF0066).copy(alpha = 0.6f), Color.Transparent),
            center = Offset(w * 0.5f, horizonY),
            radius = w * 0.28f
        ),
        radius = w * 0.28f,
        center = Offset(w * 0.5f, horizonY)
    )

    // Distant city silhouette
    val buildingCount = 18
    val bWidth = w / buildingCount
    for (i in 0 until buildingCount) {
        val bH = 15f + ((sin(i * 1.7f) + 1f) * 22f)
        drawRect(
            color = Color(0xFF101226),
            topLeft = Offset(i * bWidth, horizonY - bH),
            size = Size(bWidth + 2f, bH)
        )
    }
}

private fun DrawScope.drawPerspectiveRoad(w: Float, h: Float, horizonY: Float, roadOffset: Float) {
    val roadTopWidth = w * 0.30f
    val roadBottomWidth = w * 0.94f

    val topStartX = (w - roadTopWidth) / 2f
    val topEndX = topStartX + roadTopWidth
    val botStartX = (w - roadBottomWidth) / 2f
    val botEndX = botStartX + roadBottomWidth

    // Grass / roadside
    val grassBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF0F172A), Color(0xFF06281B), Color(0xFF084128)),
        startY = horizonY,
        endY = h
    )
    drawRect(
        brush = grassBrush,
        topLeft = Offset(0f, horizonY),
        size = Size(w, h - horizonY)
    )

    // Road asphalt
    val roadPath = Path().apply {
        moveTo(topStartX, horizonY)
        lineTo(topEndX, horizonY)
        lineTo(botEndX, h)
        lineTo(botStartX, h)
        close()
    }
    drawPath(roadPath, color = Color(0xFF181C26))

    // Road curbs (red and white stripes along edges)
    val curbWidth = w * 0.035f
    val curbSteps = 16
    for (i in 0 until curbSteps) {
        val t1 = (i / curbSteps.toFloat() + roadOffset) % 1.0f
        val t2 = ((i + 0.5f) / curbSteps.toFloat() + roadOffset) % 1.0f
        val color = if (i % 2 == 0) Color(0xFFFF2244) else Color(0xFFF0F4F8)

        val y1 = horizonY + t1 * (h - horizonY)
        val y2 = horizonY + t2 * (h - horizonY)
        if (y2 > y1) {
            val leftX1 = topStartX + (botStartX - topStartX) * t1
            val leftX2 = topStartX + (botStartX - topStartX) * t2
            drawLine(color, Offset(leftX1 - curbWidth, y1), Offset(leftX2 - curbWidth, y2), strokeWidth = curbWidth * (0.4f + t1 * 0.6f))

            val rightX1 = topEndX + (botEndX - topEndX) * t1
            val rightX2 = topEndX + (botEndX - topEndX) * t2
            drawLine(color, Offset(rightX1, y1), Offset(rightX2, y2), strokeWidth = curbWidth * (0.4f + t1 * 0.6f))
        }
    }

    // Lane dividers (2 dashed line sets for 3 lanes)
    val laneFractions = listOf(0.33f, 0.66f)
    laneFractions.forEach { frac ->
        val stripeCount = 14
        for (i in 0 until stripeCount) {
            val t1 = (i / stripeCount.toFloat() + roadOffset) % 1.0f
            val t2 = (t1 + 0.035f).coerceAtMost(1.0f)
            val lineAlpha = (t1 * 1.5f).coerceIn(0.15f, 1f)
            val strokeW = 2.5f + (t1 * 5.5f)

            val y1 = horizonY + t1 * (h - horizonY)
            val y2 = horizonY + t2 * (h - horizonY)

            val currentRoadW1 = roadTopWidth + (roadBottomWidth - roadTopWidth) * t1
            val currentRoadW2 = roadTopWidth + (roadBottomWidth - roadTopWidth) * t2

            val curStartX1 = (w - currentRoadW1) / 2f
            val curStartX2 = (w - currentRoadW2) / 2f

            val x1 = curStartX1 + (currentRoadW1 * frac)
            val x2 = curStartX2 + (currentRoadW2 * frac)

            drawLine(
                color = Color(0xFFFFFFE0).copy(alpha = lineAlpha),
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = strokeW,
                cap = StrokeCap.Round
            )
        }
    }
}

// Converts normalized -1f..1f road coordinate and 0f..1f Y to 2D canvas pixel coordinates
private fun roadXToCanvasX(w: Float, horizonY: Float, normX: Float, normY: Float): Float {
    val roadTopWidth = w * 0.30f
    val roadBottomWidth = w * 0.94f
    val clampedY = normY.coerceIn(0f, 1.2f)
    val currentRoadW = roadTopWidth + (roadBottomWidth - roadTopWidth) * clampedY
    val roadCenter = w * 0.5f
    return roadCenter + (normX * (currentRoadW * 0.45f))
}

private fun DrawScope.drawSkidMarks(w: Float, h: Float, horizonY: Float, skids: List<SkidMark>) {
    skids.forEach { sm ->
        val canvasY = horizonY + (sm.y * (h - horizonY))
        val leftX = roadXToCanvasX(w, horizonY, sm.leftX, sm.y)
        val rightX = roadXToCanvasX(w, horizonY, sm.rightX, sm.y)

        val skidW = 6f + (sm.y * 6f)
        drawLine(
            color = Color(0xFF111111).copy(alpha = sm.alpha * 0.5f),
            start = Offset(leftX, canvasY),
            end = Offset(leftX, canvasY + 12f),
            strokeWidth = skidW
        )
        drawLine(
            color = Color(0xFF111111).copy(alpha = sm.alpha * 0.5f),
            start = Offset(rightX, canvasY),
            end = Offset(rightX, canvasY + 12f),
            strokeWidth = skidW
        )
    }
}

private fun DrawScope.drawPlayerVehicle(
    w: Float,
    h: Float,
    horizonY: Float,
    state: GameStateSnapshot,
    car: CarModel
) {
    val carCanvasX = roadXToCanvasX(w, horizonY, state.playerX, state.playerY)
    val carCanvasY = horizonY + (state.playerY * (h - horizonY))

    val scale = 0.85f + (state.playerY * 0.45f)
    val carW = 64f * scale
    val carH = 110f * scale

    rotate(degrees = state.steerAngle, pivot = Offset(carCanvasX, carCanvasY)) {
        // Shadow
        drawRoundRect(
            color = Color(0x77000000),
            topLeft = Offset(carCanvasX - carW * 0.52f, carCanvasY - carH * 0.42f + 10f),
            size = Size(carW * 1.04f, carH * 0.95f),
            cornerRadius = CornerRadius(14f, 14f)
        )

        // Neon Underglow
        val underglowColor = if (state.isNitroActive) Color(0xFF00F0FF) else Color(car.accentColor)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(underglowColor.copy(alpha = 0.65f), Color.Transparent),
                center = Offset(carCanvasX, carCanvasY),
                radius = carW * 0.9f
            ),
            radius = carW * 0.9f,
            center = Offset(carCanvasX, carCanvasY)
        )

        // Tires
        val tireW = carW * 0.20f
        val tireH = carH * 0.24f
        val tireColor = Color(0xFF1E232A)
        // Front left
        drawRoundRect(tireColor, Offset(carCanvasX - carW * 0.56f, carCanvasY - carH * 0.44f), Size(tireW, tireH), CornerRadius(4f, 4f))
        // Front right
        drawRoundRect(tireColor, Offset(carCanvasX + carW * 0.36f, carCanvasY - carH * 0.44f), Size(tireW, tireH), CornerRadius(4f, 4f))
        // Rear left
        drawRoundRect(tireColor, Offset(carCanvasX - carW * 0.56f, carCanvasY + carH * 0.20f), Size(tireW, tireH), CornerRadius(4f, 4f))
        // Rear right
        drawRoundRect(tireColor, Offset(carCanvasX + carW * 0.36f, carCanvasY + carH * 0.20f), Size(tireW, tireH), CornerRadius(4f, 4f))

        // Main Car Body
        val bodyColor = Color(car.defaultColor)
        val bodyBrush = Brush.verticalGradient(
            colors = listOf(bodyColor.copy(alpha = 0.95f), bodyColor, bodyColor.copy(alpha = 0.75f)),
            startY = carCanvasY - carH * 0.5f,
            endY = carCanvasY + carH * 0.5f
        )

        val carBodyPath = Path().apply {
            moveTo(carCanvasX - carW * 0.35f, carCanvasY - carH * 0.48f)
            quadraticTo(carCanvasX, carCanvasY - carH * 0.52f, carCanvasX + carW * 0.35f, carCanvasY - carH * 0.48f)
            lineTo(carCanvasX + carW * 0.46f, carCanvasY - carH * 0.15f)
            lineTo(carCanvasX + carW * 0.42f, carCanvasY + carH * 0.44f)
            quadraticTo(carCanvasX, carCanvasY + carH * 0.48f, carCanvasX - carW * 0.42f, carCanvasY + carH * 0.44f)
            lineTo(carCanvasX - carW * 0.46f, carCanvasY - carH * 0.15f)
            close()
        }
        drawPath(carBodyPath, brush = bodyBrush)

        // Racing stripe
        val stripeColor = Color(car.accentColor)
        drawRect(
            color = stripeColor,
            topLeft = Offset(carCanvasX - carW * 0.08f, carCanvasY - carH * 0.46f),
            size = Size(carW * 0.16f, carH * 0.90f)
        )

        // Windshield and Cabin
        val glassColor = Color(0xFF0F1E2E)
        val cabinPath = Path().apply {
            moveTo(carCanvasX - carW * 0.28f, carCanvasY - carH * 0.22f)
            lineTo(carCanvasX + carW * 0.28f, carCanvasY - carH * 0.22f)
            lineTo(carCanvasX + carW * 0.32f, carCanvasY + carH * 0.15f)
            lineTo(carCanvasX - carW * 0.32f, carCanvasY + carH * 0.15f)
            close()
        }
        drawPath(cabinPath, color = glassColor)

        // Windshield reflection
        drawLine(
            color = Color(0x66FFFFFF),
            start = Offset(carCanvasX - carW * 0.20f, carCanvasY - carH * 0.18f),
            end = Offset(carCanvasX + carW * 0.12f, carCanvasY + carH * 0.08f),
            strokeWidth = 3f
        )

        // Headlights (front)
        drawRoundRect(
            color = Color(0xFFE0FFFF),
            topLeft = Offset(carCanvasX - carW * 0.32f, carCanvasY - carH * 0.48f),
            size = Size(carW * 0.18f, carH * 0.07f),
            cornerRadius = CornerRadius(3f, 3f)
        )
        drawRoundRect(
            color = Color(0xFFE0FFFF),
            topLeft = Offset(carCanvasX + carW * 0.14f, carCanvasY - carH * 0.48f),
            size = Size(carW * 0.18f, carH * 0.07f),
            cornerRadius = CornerRadius(3f, 3f)
        )

        // Taillights (rear)
        val tailLightColor = if (state.isBraking) Color(0xFFFF0011) else Color(0xFFCC1122)
        drawRoundRect(
            color = tailLightColor,
            topLeft = Offset(carCanvasX - carW * 0.34f, carCanvasY + carH * 0.42f),
            size = Size(carW * 0.22f, carH * 0.06f),
            cornerRadius = CornerRadius(3f, 3f)
        )
        drawRoundRect(
            color = tailLightColor,
            topLeft = Offset(carCanvasX + carW * 0.12f, carCanvasY + carH * 0.42f),
            size = Size(carW * 0.22f, carH * 0.06f),
            cornerRadius = CornerRadius(3f, 3f)
        )

        // Rear Spoiler Wing
        drawRoundRect(
            color = Color(0xFF15181F),
            topLeft = Offset(carCanvasX - carW * 0.40f, carCanvasY + carH * 0.45f),
            size = Size(carW * 0.80f, carH * 0.07f),
            cornerRadius = CornerRadius(3f, 3f)
        )

        // Nitro Exhaust Plumes
        if (state.isNitroActive) {
            val flameH = (35f + Random.nextFloat() * 20f) * scale
            val flameBrush = Brush.verticalGradient(
                colors = listOf(Color(0xFF00F0FF), Color(0xFFFF8500), Color.Transparent),
                startY = carCanvasY + carH * 0.48f,
                endY = carCanvasY + carH * 0.48f + flameH
            )
            // Left exhaust
            drawOval(
                brush = flameBrush,
                topLeft = Offset(carCanvasX - carW * 0.26f, carCanvasY + carH * 0.48f),
                size = Size(carW * 0.16f, flameH)
            )
            // Right exhaust
            drawOval(
                brush = flameBrush,
                topLeft = Offset(carCanvasX + carW * 0.10f, carCanvasY + carH * 0.48f),
                size = Size(carW * 0.16f, flameH)
            )
        }

        // Shield Aura
        if (state.isShieldActive) {
            drawCircle(
                color = Color(0x5500F0FF),
                radius = carH * 0.65f,
                center = Offset(carCanvasX, carCanvasY),
                style = Stroke(width = 6f)
            )
            drawCircle(
                color = Color(0x3300F0FF),
                radius = carH * 0.60f,
                center = Offset(carCanvasX, carCanvasY),
                style = Fill
            )
        }
    }
}

private fun DrawScope.drawTrafficVehicle(
    w: Float,
    h: Float,
    horizonY: Float,
    tv: TrafficVehicle
) {
    val canvasX = roadXToCanvasX(w, horizonY, tv.x, tv.y)
    val canvasY = horizonY + (tv.y * (h - horizonY))

    // Scale increases with perspective depth
    val scale = (0.28f + (tv.y.coerceIn(0f, 1.2f) * 0.72f)).coerceAtLeast(0.2f)
    val vehicleW = (if (tv.type == TrafficType.TRUCK) 85f else 60f) * scale
    val vehicleH = (if (tv.type == TrafficType.TRUCK) 140f else 96f) * scale

    // Ground Shadow
    drawRoundRect(
        color = Color(0x66000000),
        topLeft = Offset(canvasX - vehicleW * 0.5f, canvasY - vehicleH * 0.4f + 6f),
        size = Size(vehicleW, vehicleH * 0.9f),
        cornerRadius = CornerRadius(6f * scale, 6f * scale)
    )

    val bodyColor = Color(tv.color)
    when (tv.type) {
        TrafficType.TRUCK -> {
            // Cab (front)
            drawRoundRect(
                color = Color(0xFFE74C3C),
                topLeft = Offset(canvasX - vehicleW * 0.42f, canvasY - vehicleH * 0.48f),
                size = Size(vehicleW * 0.84f, vehicleH * 0.28f),
                cornerRadius = CornerRadius(6f * scale, 6f * scale)
            )
            // Cargo Container (rear)
            drawRoundRect(
                color = bodyColor,
                topLeft = Offset(canvasX - vehicleW * 0.48f, canvasY - vehicleH * 0.20f),
                size = Size(vehicleW * 0.96f, vehicleH * 0.68f),
                cornerRadius = CornerRadius(4f * scale, 4f * scale)
            )
            // Rear red reflectors
            drawRect(
                color = Color(0xFFFF2222),
                topLeft = Offset(canvasX - vehicleW * 0.42f, canvasY + vehicleH * 0.44f),
                size = Size(vehicleW * 0.18f, vehicleH * 0.04f)
            )
            drawRect(
                color = Color(0xFFFF2222),
                topLeft = Offset(canvasX + vehicleW * 0.24f, canvasY + vehicleH * 0.44f),
                size = Size(vehicleW * 0.18f, vehicleH * 0.04f)
            )
        }
        TrafficType.POLICE -> {
            // Police black & white chassis
            drawRoundRect(
                color = Color(0xFF1E272C),
                topLeft = Offset(canvasX - vehicleW * 0.45f, canvasY - vehicleH * 0.48f),
                size = Size(vehicleW * 0.90f, vehicleH * 0.96f),
                cornerRadius = CornerRadius(8f * scale, 8f * scale)
            )
            // White roof
            drawRoundRect(
                color = Color(0xFFFFFFFF),
                topLeft = Offset(canvasX - vehicleW * 0.35f, canvasY - vehicleH * 0.20f),
                size = Size(vehicleW * 0.70f, vehicleH * 0.40f),
                cornerRadius = CornerRadius(5f * scale, 5f * scale)
            )
            // Flashing roof lights
            val sirenBlue = if (tv.sirenFlash) Color(0xFF00B0FF) else Color(0x4400B0FF)
            val sirenRed = if (!tv.sirenFlash) Color(0xFFFF1744) else Color(0x44FF1744)
            drawRoundRect(
                color = sirenBlue,
                topLeft = Offset(canvasX - vehicleW * 0.26f, canvasY - vehicleH * 0.08f),
                size = Size(vehicleW * 0.22f, vehicleH * 0.08f),
                cornerRadius = CornerRadius(2f, 2f)
            )
            drawRoundRect(
                color = sirenRed,
                topLeft = Offset(canvasX + vehicleW * 0.04f, canvasY - vehicleH * 0.08f),
                size = Size(vehicleW * 0.22f, vehicleH * 0.08f),
                cornerRadius = CornerRadius(2f, 2f)
            )
        }
        else -> {
            // Standard Sedan or Sports
            drawRoundRect(
                color = bodyColor,
                topLeft = Offset(canvasX - vehicleW * 0.45f, canvasY - vehicleH * 0.48f),
                size = Size(vehicleW * 0.90f, vehicleH * 0.96f),
                cornerRadius = CornerRadius(8f * scale, 8f * scale)
            )
            // Cabin glass
            drawRoundRect(
                color = Color(0xFF111E2E),
                topLeft = Offset(canvasX - vehicleW * 0.32f, canvasY - vehicleH * 0.24f),
                size = Size(vehicleW * 0.64f, vehicleH * 0.44f),
                cornerRadius = CornerRadius(4f * scale, 4f * scale)
            )
            // Taillights
            drawRoundRect(
                color = Color(0xFFFF2222),
                topLeft = Offset(canvasX - vehicleW * 0.38f, canvasY + vehicleH * 0.40f),
                size = Size(vehicleW * 0.22f, vehicleH * 0.06f),
                cornerRadius = CornerRadius(2f, 2f)
            )
            drawRoundRect(
                color = Color(0xFFFF2222),
                topLeft = Offset(canvasX + vehicleW * 0.16f, canvasY + vehicleH * 0.40f),
                size = Size(vehicleW * 0.22f, vehicleH * 0.06f),
                cornerRadius = CornerRadius(2f, 2f)
            )
        }
    }
}

private fun DrawScope.drawCollectible(
    w: Float,
    h: Float,
    horizonY: Float,
    item: Collectible
) {
    val canvasX = roadXToCanvasX(w, horizonY, item.x, item.y)
    val canvasY = horizonY + (item.y * (h - horizonY))
    val scale = (0.35f + (item.y.coerceIn(0f, 1f) * 0.65f)).coerceAtLeast(0.25f)
    val radius = 22f * scale

    when (item.type) {
        CollectibleType.COIN -> {
            // Gold glowing coin
            drawCircle(
                color = Color(0x66FFD700),
                radius = radius * 1.5f,
                center = Offset(canvasX, canvasY)
            )
            drawCircle(
                color = Color(0xFFFFD700),
                radius = radius,
                center = Offset(canvasX, canvasY)
            )
            drawCircle(
                color = Color(0xFFFFF275),
                radius = radius * 0.65f,
                center = Offset(canvasX, canvasY)
            )
        }
        CollectibleType.NITRO -> {
            // Cyan N2O bottle
            drawRoundRect(
                color = Color(0xFF00F0FF),
                topLeft = Offset(canvasX - radius * 0.7f, canvasY - radius * 1.2f),
                size = Size(radius * 1.4f, radius * 2.4f),
                cornerRadius = CornerRadius(6f * scale, 6f * scale)
            )
            drawCircle(
                color = Color(0xFFFFFFFF),
                radius = radius * 0.35f,
                center = Offset(canvasX, canvasY)
            )
        }
        CollectibleType.REPAIR -> {
            // Green repair cross
            drawCircle(
                color = Color(0xFF00E676),
                radius = radius * 1.2f,
                center = Offset(canvasX, canvasY)
            )
            val armW = radius * 0.4f
            val armL = radius * 1.2f
            drawRect(
                color = Color(0xFFFFFFFF),
                topLeft = Offset(canvasX - armW * 0.5f, canvasY - armL * 0.5f),
                size = Size(armW, armL)
            )
            drawRect(
                color = Color(0xFFFFFFFF),
                topLeft = Offset(canvasX - armL * 0.5f, canvasY - armW * 0.5f),
                size = Size(armL, armW)
            )
        }
        CollectibleType.MAGNET -> {
            // Pink magnet ring
            drawCircle(
                color = Color(0xFFFF007F),
                radius = radius * 1.1f,
                center = Offset(canvasX, canvasY),
                style = Stroke(width = 6f * scale)
            )
        }
        CollectibleType.SHIELD -> {
            // Golden shield star orb
            drawCircle(
                color = Color(0xFFFFFF00),
                radius = radius * 1.2f,
                center = Offset(canvasX, canvasY),
                style = Stroke(width = 5f * scale)
            )
            drawCircle(
                color = Color(0x7700FFFF),
                radius = radius * 0.8f,
                center = Offset(canvasX, canvasY),
                style = Fill
            )
        }
    }
}
