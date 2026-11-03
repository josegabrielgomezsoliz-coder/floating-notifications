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

private val DarkColorScheme =
  darkColorScheme(
    primary = ShieldPrimaryDark,
    onPrimary = Color(0xFF003549),
    primaryContainer = Color(0xFF004D68),
    onPrimaryContainer = Color(0xFFBEE9FF),
    secondary = ShieldSecondaryDark,
    onSecondary = Color(0xFF003732),
    secondaryContainer = Color(0xFF004F48),
    onSecondaryContainer = Color(0xFF73F8E7),
    background = Color(0xFF0B111E),
    surface = Color(0xFF131D2E),
    surfaceVariant = Color(0xFF1E293B),
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF334155),
    error = StatusMissing,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ShieldPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC7E7FF),
    onPrimaryContainer = Color(0xFF001E2B),
    secondary = ShieldSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFA6F5EB),
    onSecondaryContainer = Color(0xFF00201D),
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    surfaceVariant = Color(0xFFF1F5F9),
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = StatusMissing,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep intentional brand cybersecurity look
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }
      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
