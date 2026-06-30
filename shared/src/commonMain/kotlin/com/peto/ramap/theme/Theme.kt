package com.peto.ramap.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppTypography =
    staticCompositionLocalOf<AppTypography> {
        error("AppTypography is not provided")
    }

private val LightColorScheme =
    lightColorScheme(
        primary = GrayColor.C500,
        onPrimary = CommonColor.White,
        secondary = ChromaticColor.Green400,
        onSecondary = GrayColor.C500,
        background = CommonColor.White,
        onBackground = GrayColor.C500,
        surface = CommonColor.White,
        onSurface = GrayColor.C500,
        error = SystemColor.Warning,
    )

@Composable
fun RamapTheme(content: @Composable () -> Unit) {
    val typography = provideAppTypography()

    CompositionLocalProvider(
        LocalAppTypography provides typography,
    ) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            content = content,
        )
    }
}
