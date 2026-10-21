package com.meghlume.browser.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF0A84FF), // iOS Safari Blue Accent
    secondary = Color(0xFF5E5CE6),
    tertiary = Color(0xFF64D2FF),
    background = Color(0xFF0D1117),
    surface = Color(0xFF161B22),
    onPrimary = Color.White,
    onBackground = Color(0xFFE6EDF3),
    onSurface = Color(0xFFF0F6FC),
    outline = Color(0xFF30363D)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF007AFF), // iOS Safari Classic Blue
    secondary = Color(0xFF5856D6),
    tertiary = Color(0xFF34C759),
    background = Color(0xFFF7F8FA),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onBackground = Color(0xFF1C1E21),
    onSurface = Color(0xFF1F2328),
    outline = Color(0xFFD0D7DE)
)

@Composable
fun MeghlumeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
