package com.waxew.qrbarcode.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF8167B4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEBDDFF),
    onPrimaryContainer = Color(0xFF2D2042),
    secondary = Color(0xFFB86187),
    secondaryContainer = Color(0xFFFFD9E8),
    tertiary = Color(0xFF4D8A79),
    tertiaryContainer = Color(0xFFCFF3E7),
    background = Color(0xFFFFF8FB),
    surface = Color(0xFFFFFBFF),
    surfaceVariant = Color(0xFFF5EDF7)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFD2BCFF),
    secondary = Color(0xFFFFB0CF),
    tertiary = Color(0xFF9FDCCB)
)

@Composable
fun QrStudioTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        content = content
    )
}
