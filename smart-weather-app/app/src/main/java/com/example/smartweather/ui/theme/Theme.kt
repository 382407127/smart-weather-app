package com.example.smartweather.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// 晴天主题色
val SunnyPrimary = Color(0xFFFF9800)
val SunnyBackground = Color(0xFF1E88E5)
val SunnySurface = Color(0x33FFFFFF)

// 多云主题色
val CloudyPrimary = Color(0xFF78909C)
val CloudyBackground = Color(0xFF546E7A)
val CloudySurface = Color(0x33FFFFFF)

// 阴天主题色
val OvercastPrimary = Color(0xFF607D8B)
val OvercastBackground = Color(0xFF455A64)
val OvercastSurface = Color(0x33FFFFFF)

// 雨天主题色
val RainyPrimary = Color(0xFF42A5F5)
val RainyBackground = Color(0xFF37474F)
val RainySurface = Color(0x33FFFFFF)

// 雪天主题色
val SnowyPrimary = Color(0xFFB3E5FC)
val SnowyBackground = Color(0xFF5C6BC0)
val SnowySurface = Color(0x33FFFFFF)

private val DarkColorScheme = darkColorScheme(
    primary = SunnyPrimary,
    secondary = SunnyBackground,
    tertiary = SunnySurface
)

private val LightColorScheme = lightColorScheme(
    primary = SunnyPrimary,
    secondary = SunnyBackground,
    tertiary = SunnySurface
)

@Composable
fun SmartWeatherTheme(
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
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
