package sound.gplayeraudio.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import sound.gplayeraudio.model.AppThemeMode

val PureBlackScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF242424),
    onPrimaryContainer = Color.White,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color(0xFF121212),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF333333),
    onSurfaceVariant = Color(0xFFB0B0B0)
)

val PureWhiteScheme = lightColorScheme(
    primary = Color.Black,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8E8E8),
    onPrimaryContainer = Color.Black,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color(0xFFF4F4F4),
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFDCDCDC),
    onSurfaceVariant = Color(0xFF555555)
)

@Composable
fun MonochromePlayerTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
    }

    MaterialTheme(
        colorScheme = if (isDark) PureBlackScheme else PureWhiteScheme,
        content = content
    )
}
