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

private val DarkColorScheme = darkColorScheme(
    primary = IndigoLight,
    onPrimary = Slate900,
    primaryContainer = IndigoDark,
    onPrimaryContainer = IndigoContainer,
    secondary = CyanAccent,
    onSecondary = Slate900,
    secondaryContainer = CyanDark,
    onSecondaryContainer = CyanContainer,
    tertiary = AmberWarning,
    onTertiary = Slate900,
    tertiaryContainer = Color(0xFF78350F),
    onTertiaryContainer = AmberContainer,
    background = Color(0xFF0B0F19),
    surface = Color(0xFF131B2E),
    surfaceVariant = Color(0xFF1E293B),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569)
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = IndigoContainer,
    onPrimaryContainer = OnIndigoContainer,
    secondary = CyanDark,
    onSecondary = Color.White,
    secondaryContainer = CyanContainer,
    onSecondaryContainer = Color(0xFF164E63),
    tertiary = AmberWarning,
    onTertiary = Slate900,
    tertiaryContainer = AmberContainer,
    onTertiaryContainer = Color(0xFF78350F),
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    surfaceVariant = Color(0xFFF1F5F9),
    onBackground = Slate900,
    onSurface = Slate900,
    onSurfaceVariant = Slate700,
    outline = Slate200
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our rich custom brand palette
    content: @Composable () -> Unit,
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
        typography = Typography,
        content = content
    )
}
