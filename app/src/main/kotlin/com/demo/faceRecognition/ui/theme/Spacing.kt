package com.demo.faceRecognition.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Spacing(
    val ExtraSmall: Dp = 4.dp,
    val Small: Dp = 8.dp,
    val Normal: Dp = 12.dp,
    val Medium: Dp = 16.dp,
    val Large: Dp = 24.dp,
    val ExtraLarge: Dp = 32.dp,
    val XXLarge: Dp = 48.dp,
    val XXXLarge: Dp = 64.dp,
    val Default: Dp = 0.dp
)

val LocalSpacing = compositionLocalOf { Spacing() }

val MaterialTheme.spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = LocalSpacing.current