package com.freewdcmkt.bck.ui.theme

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
    primary = Color(0xFF97D0F6),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF0067C0),
    onPrimaryContainer = Color(0xFFD1E4FF),

    secondary = Color(0xFF7CC1F0),
    onSecondary = Color(0xFF003258),
    secondaryContainer = Color(0xFF2880CA),
    onSecondaryContainer = Color(0xFFD1E4FF),

    tertiary = Color(0xFF518497),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF2880CA),
    onTertiaryContainer = Color(0xFFD1E4FF),

    background = Color(0xFF101418),
    onBackground = Color(0xFFE1E2E8),
    surface = Color(0xFF101418),
    onSurface = Color(0xFFE1E2E8),
    surfaceVariant = Color(0xFF41474D),
    onSurfaceVariant = Color(0xFFC1C7CE),
    outline = Color(0xFF8B9198)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0067C0),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF97D0F6),
    onPrimaryContainer = Color(0xFF001D36),

    secondary = Color(0xFF2880CA),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFF7CC1F0),
    onSecondaryContainer = Color(0xFF001D36),

    tertiary = Color(0xFF518497),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFF97D0F6),
    onTertiaryContainer = Color(0xFF001D36),

    background = Color(0xFFF7F9FF),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFF7F9FF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFDDE3EA),
    onSurfaceVariant = Color(0xFF41474D),
    outline = Color(0xFF72787E)
)

@Composable
fun FreewdTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // 改成 false 才会使用上面的静态配色
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
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