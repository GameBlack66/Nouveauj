package com.example.game.ui.screens

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.example.game.audio.GameAudio
import com.example.game.engine.GameEngine
import com.example.game.model.AVAILABLE_CARS
import com.example.game.model.GameMode
import com.example.game.repository.GamePreferences
import com.example.game.ui.GameCanvasView
import com.example.game.ui.GameHud
import kotlinx.coroutines.isActive

@Composable
fun RaceScreen(
    mode: GameMode,
    preferences: GamePreferences,
    audio: GameAudio,
    onFinishRace: () -> Unit,
    onOpenGarage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedCar = remember {
        val car = AVAILABLE_CARS.find { it.id == preferences.selectedCarId } ?: AVAILABLE_CARS.first()
        val customColor = preferences.getCarColor(car.id, car.defaultColor)
        car.copy(defaultColor = customColor)
    }

    var restartKey by remember { mutableIntStateOf(0) }
    val engine = remember(restartKey) {
        GameEngine(
            preferences = preferences,
            audio = audio,
            mode = mode,
            car = selectedCar
        )
    }

    val gameState by engine.state.collectAsState()

    // 60 FPS Game Loop
    LaunchedEffect(engine) {
        var lastTimeNanos = System.nanoTime()
        while (isActive) {
            withFrameNanos { frameTimeNanos ->
                val dt = ((frameTimeNanos - lastTimeNanos) / 1_000_000_000.0f).coerceIn(0.001f, 0.05f)
                lastTimeNanos = frameTimeNanos
                engine.update(dt)
            }
        }
    }

    var steerLeftPressed by remember { mutableStateOf(false) }
    var steerRightPressed by remember { mutableStateOf(false) }

    LaunchedEffect(steerLeftPressed, steerRightPressed) {
        val steer = when {
            steerLeftPressed && !steerRightPressed -> -1f
            steerRightPressed && !steerLeftPressed -> 1f
            else -> 0f
        }
        engine.setSteerInput(steer)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(engine) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        // Dragging left or right steers the car directly
                        val dragSteer = (dragAmount.x / 18f).coerceIn(-1f, 1f)
                        engine.setSteerInput(dragSteer)
                    },
                    onDragEnd = {
                        if (!steerLeftPressed && !steerRightPressed) {
                            engine.setSteerInput(0f)
                        }
                    },
                    onDragCancel = {
                        if (!steerLeftPressed && !steerRightPressed) {
                            engine.setSteerInput(0f)
                        }
                    }
                )
            }
    ) {
        // Canvas Rendering
        GameCanvasView(
            state = gameState,
            car = selectedCar
        )

        // HUD & Controls
        GameHud(
            state = gameState,
            mode = mode,
            onSteerLeft = { pressed -> steerLeftPressed = pressed },
            onSteerRight = { pressed -> steerRightPressed = pressed },
            onNitro = { active -> engine.setNitro(active) },
            onBrake = { active -> engine.setBrake(active) },
            onPause = { engine.togglePause() }
        )

        // Pause Dialog
        if (gameState.isPaused && !gameState.isGameOver) {
            PauseDialog(
                onResume = { engine.togglePause() },
                onRestart = { restartKey++ },
                onHome = onFinishRace
            )
        }

        // Game Over Dialog
        if (gameState.isGameOver) {
            GameOverDialog(
                state = gameState,
                onRestart = { restartKey++ },
                onGarage = onOpenGarage,
                onHome = onFinishRace
            )
        }
    }
}
