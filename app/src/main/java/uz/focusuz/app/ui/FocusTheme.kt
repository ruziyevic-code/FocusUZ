package uz.focusuz.app.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val FocusDark = darkColorScheme(
    primary = Color(0xFF91E8AC),
    onPrimary = Color(0xFF101612),
    background = Color(0xFF101612),
    surface = Color(0xFF1B2820),
    surfaceVariant = Color(0xFF26372B),
    onBackground = Color(0xFFEDF8EF),
    onSurface = Color(0xFFEDF8EF),
    onSurfaceVariant = Color(0xFFA2B5A8),
    outline = Color(0xFF34483B),
    error = Color(0xFFFF8A80)
)

private val FocusLight = lightColorScheme(
    primary = Color(0xFF246B3B),
    background = Color(0xFFF4F7F4),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF101612),
    onSurface = Color(0xFF101612)
)

@Composable
fun FocusTheme(
    dark: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (dark) FocusDark else FocusLight,
        content = content
    )
}
