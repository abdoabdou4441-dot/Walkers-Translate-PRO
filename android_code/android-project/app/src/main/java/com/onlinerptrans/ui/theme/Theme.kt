package com.onlinerptrans.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GamingDarkColorScheme = darkColorScheme(
    primary            = NeonCyan,
    onPrimary          = GamingBlack,
    primaryContainer   = NeonCyanSubtle,
    onPrimaryContainer = NeonCyan,
    secondary          = NeonCyanDim,
    onSecondary        = GamingBlack,
    secondaryContainer = GamingDark3,
    onSecondaryContainer = TextPrimary,
    background         = GamingBlack,
    onBackground       = TextPrimary,
    surface            = GamingDark,
    onSurface          = TextPrimary,
    surfaceVariant     = GamingDark2,
    onSurfaceVariant   = TextSecondary,
    outline            = GamingDark4,
    error              = ErrorRed,
    onError            = GamingBlack
)

@Composable
fun OnlineRPTransTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GamingDarkColorScheme,
        typography  = GamingTypography,
        content     = content
    )
}
