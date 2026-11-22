package sound.gplayeraudio.model


import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import sound.gplayeraudio.R

enum class AppLanguage(val code: String, @StringRes val titleRes: Int) {
    RUSSIAN("ru", R.string.lang_russian),
    ENGLISH("en", R.string.lang_english)
}

enum class AppThemeMode(@StringRes val titleRes: Int) {
    SYSTEM(R.string.theme_system),
    LIGHT(R.string.theme_light),
    DARK(R.string.theme_dark)
}

enum class PlayerSliderStyle(@StringRes val titleRes: Int, @StringRes val subtitleRes: Int) {
    WAVY(R.string.slider_wavy_title, R.string.slider_wavy_subtitle),
    CLASSIC(R.string.slider_classic_title, R.string.slider_classic_subtitle),
    MINIMAL(R.string.slider_minimal_title, R.string.slider_minimal_subtitle)
}

enum class CoverShape(val titleRu: String, val titleEn: String) {
    ROUNDED("Скругленный", "Rounded"),
    CIRCLE("Круг", "Circle"),
    SQUIRCLE("Сквиркл", "Squircle"),
    COOKIE("Печенье", "Cookie"),
    CLOVER("Клевер", "Clover"),
    FLOWER("Цветок", "Flower"),
    BURST("Вспышка", "Burst"),
    WAVE("Волна", "Wave"),
    DROP("Капля", "Drop"),
    SQUARE("Квадрат", "Square"),
    DIAMOND("Ромб", "Diamond"),
    HEXAGON("Шестиугольник", "Hexagon"),
    HEART("Сердечко", "Heart"),
    ARCH("Арка", "Arch"),
    PENTAGON("Пятиугольник", "Pentagon");

    fun getTitle(lang: AppLanguage): String = if (lang == AppLanguage.RUSSIAN) titleRu else titleEn
}

enum class MainTab(
    @StringRes val titleRes: Int,
    val filledIcon: ImageVector,
    val outlinedIcon: ImageVector
) {
    LIBRARY(R.string.tab_library, Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic),
    SETTINGS(R.string.tab_settings, Icons.Filled.Settings, Icons.Outlined.Settings)
}
