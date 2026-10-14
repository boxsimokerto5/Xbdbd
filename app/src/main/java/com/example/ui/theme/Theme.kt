package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val PlayfulLightColorScheme = lightColorScheme(
    primary = GameSkyBlue,
    onPrimary = Color.White,
    primaryContainer = GameSkyBlueLight,
    onPrimaryContainer = GameSkyBlueDark,
    secondary = GameAmber,
    onSecondary = Color.White,
    secondaryContainer = GameSunYellowLight,
    onSecondaryContainer = Color(0xFF78350F),
    tertiary = GameMintGreenDark,
    onTertiary = Color.White,
    tertiaryContainer = GameMintGreenLight,
    onTertiaryContainer = Color(0xFF064E3B),
    background = GameScreenBg,
    onBackground = GameDarkNavy,
    surface = GameCardBg,
    onSurface = GameDarkNavy,
    surfaceVariant = Color(0xFFF8FAFC),
    onSurfaceVariant = GameSlate,
    error = GameCoralRed,
    onError = Color.White,
    errorContainer = GameCoralRedLight,
    onErrorContainer = Color(0xFF881337)
)

private val PlayfulDarkColorScheme = darkColorScheme(
    primary = GameSkyBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = GameSkyBlueLight,
    secondary = GameSunYellow,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF78350F),
    onSecondaryContainer = GameSunYellowLight,
    tertiary = GameMintGreen,
    onTertiary = Color.Black,
    background = Color(0xFF0B132B),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1C2541),
    onSurface = Color(0xFFF8FAFC),
    error = GameCoralRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent cheerful branding
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> PlayfulDarkColorScheme
        else -> PlayfulLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
