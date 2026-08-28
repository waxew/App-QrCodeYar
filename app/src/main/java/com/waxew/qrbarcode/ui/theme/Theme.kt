/*
 * پالت روشن/تیره برنامه.
 *
 * از نسخه 1.9 رنگ Accent ذخیره‌شده واقعاً روی MaterialTheme اعمال می‌شود. انتخاب Dark/Light
 * همچنان از تنظیم سیستم پیروی می‌کند و تغییر Accent بعد از بازسازی Activity/اجرای بعدی دیده می‌شود.
 */
package com.waxew.qrbarcode.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.waxew.qrbarcode.v19.V19SettingsRepository

private fun lightPalette(accent: String): ColorScheme = when (accent) {
    "سبز نعنایی" -> lightColorScheme(
        primary = Color(0xFF3F806F), onPrimary = Color.White,
        primaryContainer = Color(0xFFCFF3E7), onPrimaryContainer = Color(0xFF12372F),
        secondary = Color(0xFF6B7D55), secondaryContainer = Color(0xFFE0EDC9),
        tertiary = Color(0xFF6C5D8E), tertiaryContainer = Color(0xFFEBDDFF),
        background = Color(0xFFF7FCF9), surface = Color(0xFFFBFFFC), surfaceVariant = Color(0xFFEAF4EF)
    )
    "آبی آسمانی" -> lightColorScheme(
        primary = Color(0xFF3D75A5), onPrimary = Color.White,
        primaryContainer = Color(0xFFD6EAFF), onPrimaryContainer = Color(0xFF10344F),
        secondary = Color(0xFF6E6A95), secondaryContainer = Color(0xFFE6E1FF),
        tertiary = Color(0xFF8C5F78), tertiaryContainer = Color(0xFFFFD9E8),
        background = Color(0xFFF8FAFF), surface = Color(0xFFFCFCFF), surfaceVariant = Color(0xFFEDF2F8)
    )
    else -> lightColorScheme(
        primary = Color(0xFF8167B4), onPrimary = Color.White,
        primaryContainer = Color(0xFFEBDDFF), onPrimaryContainer = Color(0xFF2D2042),
        secondary = Color(0xFFB86187), secondaryContainer = Color(0xFFFFD9E8),
        tertiary = Color(0xFF4D8A79), tertiaryContainer = Color(0xFFCFF3E7),
        background = Color(0xFFFFF8FB), surface = Color(0xFFFFFBFF), surfaceVariant = Color(0xFFF5EDF7)
    )
}

private fun darkPalette(accent: String): ColorScheme = when (accent) {
    "سبز نعنایی" -> darkColorScheme(primary = Color(0xFF9FDCCB), secondary = Color(0xFFC4D6A7), tertiary = Color(0xFFD2BCFF))
    "آبی آسمانی" -> darkColorScheme(primary = Color(0xFFA8CFFF), secondary = Color(0xFFC8C2F4), tertiary = Color(0xFFFFB0CF))
    else -> darkColorScheme(primary = Color(0xFFD2BCFF), secondary = Color(0xFFFFB0CF), tertiary = Color(0xFF9FDCCB))
}

@Composable
fun QrStudioTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val accent = V19SettingsRepository(context).accentName
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) darkPalette(accent) else lightPalette(accent),
        content = content
    )
}
