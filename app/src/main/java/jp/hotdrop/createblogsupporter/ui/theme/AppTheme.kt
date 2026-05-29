package jp.hotdrop.createblogsupporter.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7DD9D4),
    onPrimary = Color(0xFF003735),
    secondary = Color(0xFFE7BE8F),
    onSecondary = Color(0xFF462A0C),
    tertiary = Color(0xFFB8C8E6),
    onTertiary = Color(0xFF22314A),
    background = Color(0xFF101412),
    onBackground = Color(0xFFE1E4DF),
    surface = Color(0xFF181C1A),
    onSurface = Color(0xFFE1E4DF),
    surfaceVariant = Color(0xFF414945),
    onSurfaceVariant = Color(0xFFC1C9C4),
    primaryContainer = Color(0xFF00504D),
    onPrimaryContainer = Color(0xFF9CF2ED),
    secondaryContainer = Color(0xFF61401F),
    onSecondaryContainer = Color(0xFFFFDCB6),
    tertiaryContainer = Color(0xFF374861),
    onTertiaryContainer = Color(0xFFD7E3FF),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

@Composable
fun CreateBlogSupporterTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content,
    )
}
