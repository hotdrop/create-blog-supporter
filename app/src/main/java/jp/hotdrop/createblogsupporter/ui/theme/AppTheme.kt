package jp.hotdrop.createblogsupporter.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF166B68),
    onPrimary = Color.White,
    secondary = Color(0xFF7A4B22),
    tertiary = Color(0xFF475569),
    background = Color(0xFFFAFAF7),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE6E2D9),
    onSurface = Color(0xFF202124),
)

@Composable
fun CreateBlogSupporterTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content,
    )
}
