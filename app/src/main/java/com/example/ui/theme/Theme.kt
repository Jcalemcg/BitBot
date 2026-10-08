package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
  primary = NeonCyan,
  onPrimary = CyberDark,
  primaryContainer = CyberSurfaceVariant,
  onPrimaryContainer = NeonCyanLight,
  secondary = TelemetryEmerald,
  onSecondary = CyberDark,
  secondaryContainer = TelemetryEmeraldDim,
  onSecondaryContainer = TelemetryEmerald,
  tertiary = WarningAmber,
  onTertiary = CyberDark,
  background = CyberBackground,
  onBackground = TextPrimary,
  surface = CyberSurface,
  onSurface = TextPrimary,
  surfaceVariant = CyberSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  outline = CyberCardBorder,
  error = AlertCrimson,
  onError = CyberDark,
  errorContainer = AlertCrimsonDim,
  onErrorContainer = AlertCrimson
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  // Always render high-tech dark telemetry console per confirmed user preference
  MaterialTheme(
    colorScheme = DarkColorScheme,
    typography = Typography,
    content = content
  )
}
