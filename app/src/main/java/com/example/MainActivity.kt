package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.game.audio.GameAudio
import com.example.game.model.GameMode
import com.example.game.repository.GamePreferences
import com.example.game.ui.screens.GarageScreen
import com.example.game.ui.screens.HomeScreen
import com.example.game.ui.screens.RaceScreen
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    HOME,
    RACE,
    GARAGE
}

class MainActivity : ComponentActivity() {

    private lateinit var audio: GameAudio
    private lateinit var preferences: GamePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferences = GamePreferences(applicationContext)
        audio = GameAudio(applicationContext).apply {
            isSoundEnabled = preferences.isSoundEnabled
            isHapticsEnabled = preferences.isHapticsEnabled
        }

        setContent {
            MyApplicationTheme {
                var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
                var activeGameMode by remember { mutableStateOf(GameMode.ENDLESS) }

                BackHandler(enabled = currentScreen != AppScreen.HOME) {
                    currentScreen = AppScreen.HOME
                }

                when (currentScreen) {
                    AppScreen.HOME -> {
                        HomeScreen(
                            preferences = preferences,
                            audio = audio,
                            onStartGame = { mode ->
                                activeGameMode = mode
                                currentScreen = AppScreen.RACE
                            },
                            onOpenGarage = {
                                currentScreen = AppScreen.GARAGE
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.RACE -> {
                        RaceScreen(
                            mode = activeGameMode,
                            preferences = preferences,
                            audio = audio,
                            onFinishRace = {
                                currentScreen = AppScreen.HOME
                            },
                            onOpenGarage = {
                                currentScreen = AppScreen.GARAGE
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.GARAGE -> {
                        GarageScreen(
                            preferences = preferences,
                            audio = audio,
                            onBack = {
                                currentScreen = AppScreen.HOME
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::audio.isInitialized) {
            audio.release()
        }
    }
}
