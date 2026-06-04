package com.hugo.notificationsilencer.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AppBackground = Color(0xFFF6F7F8)
val GlassWhite = Color(0xFFFFFFFF)
val PrimaryText = Color(0xFF1F2933)
val SecondaryText = Color(0xFF69727D)
val MutedText = Color(0xFF9AA3AD)
val MistRed = Color(0xFFE88D8D)
val MistRedContainer = Color(0xFFFFEEEE)
val MistGreen = Color(0xFF6FAF8F)
val MistGreenContainer = Color(0xFFEAF7F0)
val MistBlue = Color(0xFF708AA8)
val MistBlueContainer = Color(0xFFEFF4FA)
val SoftGray = Color(0xFFE9EDF1)
val SelectedToken = Color(0xFFDDEBE4)
val AddedToken = Color(0xFFE2E5E8)

private val LightColors = lightColorScheme(
    background = AppBackground,
    surface = GlassWhite,
    surfaceVariant = SoftGray,
    primary = PrimaryText,
    onPrimary = Color.White,
    secondary = SecondaryText,
    onSecondary = Color.White,
    tertiary = MistGreen,
    onTertiary = Color.White,
    onBackground = PrimaryText,
    onSurface = PrimaryText,
    onSurfaceVariant = SecondaryText,
    outline = Color(0xFFD8DEE5),
)

@Composable
fun NotificationSilencerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content,
    )
}
