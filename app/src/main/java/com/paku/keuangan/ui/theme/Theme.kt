package com.paku.keuangan.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.paku.keuangan.R
import com.paku.keuangan.data.ThemeMode
import com.paku.keuangan.i18n.LanguageMode
import com.paku.keuangan.i18n.LocalStrings
import com.paku.keuangan.i18n.strings

// ---------------------------------------------------------------------------
// Palet warna PAKU — hangat, ramah, dan tetap tegas untuk data keuangan.
// ---------------------------------------------------------------------------

val Coral = Color(0xFFE15B64)
val CoralDeep = Color(0xFFC84650)
val CoralSoft = Color(0xFFFFE6E3)
val Peach = Color(0xFFFFB59F)
val Teal = Color(0xFF1C9BAA)
val TealSoft = Color(0xFFD8F1F4)
val Mint = Color(0xFF2E9E7E)
val MintSoft = Color(0xFFDCF2E9)
val Amber = Color(0xFFE3A33C)
val Cream = Color(0xFFFFF8F2)
val WarmWhite = Color(0xFFFFFCF9)
val Ink = Color(0xFF2B2320)
val InkSoft = Color(0xFF8B7C76)
val Sand = Color(0xFFF3E6DE)
val Line = Color(0xFFEFE2DA)

private val LightColors = lightColorScheme(
    primary = Coral,
    onPrimary = Color.White,
    primaryContainer = CoralSoft,
    onPrimaryContainer = Color(0xFF5B151D),
    secondary = Teal,
    onSecondary = Color.White,
    secondaryContainer = TealSoft,
    onSecondaryContainer = Color(0xFF083C44),
    tertiary = Mint,
    onTertiary = Color.White,
    tertiaryContainer = MintSoft,
    onTertiaryContainer = Color(0xFF0C3B2D),
    background = Cream,
    onBackground = Ink,
    surface = WarmWhite,
    onSurface = Ink,
    surfaceVariant = Color(0xFFF7ECE5),
    onSurfaceVariant = InkSoft,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFFFAF6),
    surfaceContainer = Color(0xFFFFF5EE),
    surfaceContainerHigh = Color(0xFFF9EEE7),
    surfaceContainerHighest = Color(0xFFF4E8E1),
    outline = Color(0xFFD9C7BD),
    outlineVariant = Line,
    error = Color(0xFFD14343),
    onError = Color.White,
    errorContainer = Color(0xFFFFE1DF),
    onErrorContainer = Color(0xFF5E1113),
    inverseSurface = Ink,
    inverseOnSurface = Cream,
    inversePrimary = Peach,
    scrim = Color.Black
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF9AA1),
    onPrimary = Color(0xFF431016),
    primaryContainer = Color(0xFF5E2229),
    onPrimaryContainer = Color(0xFFFFDADD),
    secondary = Color(0xFF6FD4DF),
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF114E55),
    onSecondaryContainer = Color(0xFFB4EFF5),
    tertiary = Color(0xFF63D6AD),
    onTertiary = Color(0xFF00382A),
    tertiaryContainer = Color(0xFF15513F),
    onTertiaryContainer = Color(0xFFB7F5DD),
    background = Color(0xFF151110),
    onBackground = Color(0xFFF4E7E0),
    surface = Color(0xFF201917),
    onSurface = Color(0xFFF4E7E0),
    surfaceVariant = Color(0xFF2B2220),
    onSurfaceVariant = Color(0xFFC7B4AB),
    surfaceContainerLowest = Color(0xFF100D0C),
    surfaceContainerLow = Color(0xFF1B1513),
    surfaceContainer = Color(0xFF231C1A),
    surfaceContainerHigh = Color(0xFF2B2220),
    surfaceContainerHighest = Color(0xFF362C29),
    outline = Color(0xFF6E5A52),
    outlineVariant = Color(0xFF3C302C),
    error = Color(0xFFFFB3B0),
    onError = Color(0xFF5F1413),
    errorContainer = Color(0xFF7C2423),
    onErrorContainer = Color(0xFFFFDAD7),
    inverseSurface = Color(0xFFF4E7E0),
    inverseOnSurface = Color(0xFF2B2320),
    inversePrimary = CoralDeep,
    scrim = Color.Black
)

/** Warna semantik untuk arus kas. */
val androidx.compose.material3.ColorScheme.income: Color
    get() = tertiary
val androidx.compose.material3.ColorScheme.expense: Color
    get() = primary

// ---------------------------------------------------------------------------
// Tipografi — Plus Jakarta Sans (lisensi OFL).
// ---------------------------------------------------------------------------

val PakuFont = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold)
)

private fun pakuStyle(
    size: Int,
    weight: FontWeight,
    lineHeight: Int = (size * 1.3f).toInt(),
    tracking: Double = -0.2
) = TextStyle(
    fontFamily = PakuFont,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = tracking.sp
)

val PakuTypography = Typography(
    displayLarge = pakuStyle(44, FontWeight.ExtraBold, 52, -1.4),
    displayMedium = pakuStyle(38, FontWeight.ExtraBold, 46, -1.2),
    displaySmall = pakuStyle(32, FontWeight.Bold, 40, -0.8),
    headlineLarge = pakuStyle(28, FontWeight.ExtraBold, 36, -0.6),
    headlineMedium = pakuStyle(24, FontWeight.Bold, 32, -0.5),
    headlineSmall = pakuStyle(20, FontWeight.Bold, 28, -0.3),
    titleLarge = pakuStyle(18, FontWeight.Bold, 26, -0.2),
    titleMedium = pakuStyle(16, FontWeight.SemiBold, 24, -0.1),
    titleSmall = pakuStyle(14, FontWeight.SemiBold, 20, 0.0),
    bodyLarge = pakuStyle(16, FontWeight.Normal, 24, 0.0),
    bodyMedium = pakuStyle(14, FontWeight.Normal, 21, 0.0),
    bodySmall = pakuStyle(12, FontWeight.Normal, 18, 0.1),
    labelLarge = pakuStyle(14, FontWeight.SemiBold, 20, 0.1),
    labelMedium = pakuStyle(12, FontWeight.Medium, 16, 0.2),
    labelSmall = pakuStyle(11, FontWeight.Medium, 14, 0.3)
)

val PakuShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(18.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(26.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
)

@Composable
fun PakuTheme(
    themeMode: ThemeMode,
    languageMode: LanguageMode = LanguageMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val dark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val strings = remember(languageMode) { languageMode.resolve().strings() }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !dark
            controller.isAppearanceLightNavigationBars = !dark
        }
    }
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = PakuTypography,
        shapes = PakuShapes
    ) {
        CompositionLocalProvider(LocalStrings provides strings, content = content)
    }
}
