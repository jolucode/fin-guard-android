package com.jose.holamundo.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val FinGuardColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Background,
    primaryContainer = PrimaryVariant,
    onPrimaryContainer = OnSurface,
    secondary = ChartPurple,
    onSecondary = OnSurface,
    secondaryContainer = SurfaceVariant,
    onSecondaryContainer = OnSurface,
    tertiary = ChartBlue,
    onTertiary = OnSurface,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    error = Error,
    onError = OnSurface,
    outline = TextSecondary
)

@Composable
fun HolaMundoAndroidTheme(
    darkTheme: Boolean = true, // Always dark theme for FinGuard
    content: @Composable () -> Unit
) {
    val colorScheme = FinGuardColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Background.toArgb()
            window.navigationBarColor = Background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Alias for the new theme name
@Composable
fun FinGuardTheme(
    content: @Composable () -> Unit
) = HolaMundoAndroidTheme(content = content)
