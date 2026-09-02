package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RacingDarkColorScheme = darkColorScheme(
    primary = RacingRed,
    onPrimary = Color.White,
    secondary = CyberCyan,
    onSecondary = Color.Black,
    tertiary = NitroOrange,
    background = DarkAsphalt,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White,
    surfaceVariant = CardSurface,
    onSurfaceVariant = Color(0xFFCBD5E1)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RacingDarkColorScheme,
        typography = Typography,
        content = content
    )
}
