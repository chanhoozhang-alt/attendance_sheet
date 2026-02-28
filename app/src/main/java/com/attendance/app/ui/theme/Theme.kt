package com.attendance.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val PrimaryColor = Color(0xFF1976D2)
private val PrimaryDarkColor = Color(0xFF1565C0)
private val SecondaryColor = Color(0xFFFF5722)
private val BackgroundColor = Color(0xFFF5F5F5)
private val SurfaceColor = Color(0xFFFFFFFF)
private val OnPrimaryColor = Color.White
private val OnSecondaryColor = Color.White
private val OnBackgroundColor = Color(0xFF333333)
private val OnSurfaceColor = Color(0xFF333333)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    onPrimary = OnPrimaryColor,
    primaryContainer = Color(0xFFBBDEFB),
    secondary = SecondaryColor,
    onSecondary = OnSecondaryColor,
    background = BackgroundColor,
    surface = SurfaceColor,
    onBackground = OnBackgroundColor,
    onSurface = OnSurfaceColor
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    onPrimary = OnPrimaryColor,
    primaryContainer = PrimaryDarkColor,
    secondary = SecondaryColor,
    onSecondary = OnSecondaryColor,
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun AttendanceAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
