package com.multichoice.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GitHubDark = Color(0xFF0D1117)
private val White = Color(0xFFFFFFFF)
private val Purple = Color(0xFF7C3AED)
private val PurplePressed = Color(0xFF6D28D9)
private val CardDark = Color(0xFF161B22)
private  val CorrectColor = Color(0xFF36D011)
private val IncorrectColor = Color(0xFFEF5350)

private val AppColors = darkColorScheme(
    background = GitHubDark,
    surface = GitHubDark,
    surfaceVariant = GitHubDark,
    onBackground = White,
    onSurface = White,
    onSurfaceVariant = White,
    primary = Color(0xFF7C3AED),
    onPrimary = White
)

@Composable
fun MultiChoiceTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColors,
        content = content
    )
}
