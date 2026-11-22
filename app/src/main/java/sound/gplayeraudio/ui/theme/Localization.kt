package sound.gplayeraudio.ui.theme

import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppResources = staticCompositionLocalOf<Resources> {
    error("No Resources provided")
}

@Composable
fun appString(@StringRes id: Int): String {
    return LocalAppResources.current.getString(id)
}
