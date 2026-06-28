package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

data class StreambertColors(
    val bg: Color,          // Main background
    val surface: Color,     // Cards, surfaces
    val surface2: Color,    // Elevated surfaces, input fields
    val surface3: Color,    // Higher elevation
    val border: Color,      // Outlines, dividers
    val text: Color,        // Primary text
    val text2: Color,       // Secondary text
    val text3: Color,       // Tertiary/muted text
    val accent: Color,      // Primary accent
    val accentDim: Color,   // Accent at 33% alpha
    val accentGlow: Color   // Accent at 20% alpha
)

object AccentPresets {
    val Red = Color(0xFFE50914)
    val Blue = Color(0xFF2563EB)
    val Purple = Color(0xFF7C3AED)
    val Green = Color(0xFF059669)
    val Orange = Color(0xFFD97706)
    val Pink = Color(0xFFDB2777)

    val all = mapOf(
        "Red" to Red,
        "Blue" to Blue,
        "Purple" to Purple,
        "Green" to Green,
        "Orange" to Orange,
        "Pink" to Pink
    )

    fun fromHex(hex: String): Color {
        return try {
            if (hex.startsWith("#")) {
                Color(android.graphics.Color.parseColor(hex))
            } else {
                Color(android.graphics.Color.parseColor("#$hex"))
            }
        } catch (e: Exception) {
            Red // fallback
        }
    }
}

object ThemePresets {
    val Dark = StreambertColors(
        bg = Color(0xFF0A0A0A),
        surface = Color(0xFF111111),
        surface2 = Color(0xFF1A1A1A),
        surface3 = Color(0xFF222222),
        border = Color(0xFF2A2A2A),
        text = Color(0xFFF0F0F0),
        text2 = Color(0xFFAAAAAA),
        text3 = Color(0xFF666666),
        accent = AccentPresets.Red,
        accentDim = AccentPresets.Red.copy(alpha = 0.33f),
        accentGlow = AccentPresets.Red.copy(alpha = 0.20f)
    )

    val Amoled = StreambertColors(
        bg = Color(0xFF000000),
        surface = Color(0xFF080808),
        surface2 = Color(0xFF111111),
        surface3 = Color(0xFF1A1A1A),
        border = Color(0xFF222222),
        text = Color(0xFFFFFFFF),
        text2 = Color(0xFFBBBBBB),
        text3 = Color(0xFF777777),
        accent = AccentPresets.Red,
        accentDim = AccentPresets.Red.copy(alpha = 0.33f),
        accentGlow = AccentPresets.Red.copy(alpha = 0.20f)
    )

    val Mocha = StreambertColors(
        bg = Color(0xFF0E0B09),
        surface = Color(0xFF1A1410),
        surface2 = Color(0xFF231C16),
        surface3 = Color(0xFF2D241C),
        border = Color(0xFF3D3228),
        text = Color(0xFFF0E8DF),
        text2 = Color(0xFFB0A898),
        text3 = Color(0xFF7A7060),
        accent = AccentPresets.Orange,
        accentDim = AccentPresets.Orange.copy(alpha = 0.33f),
        accentGlow = AccentPresets.Orange.copy(alpha = 0.20f)
    )

    val Slate = StreambertColors(
        bg = Color(0xFF0D1117),
        surface = Color(0xFF161B22),
        surface2 = Color(0xFF1F2937),
        surface3 = Color(0xFF2D3748),
        border = Color(0xFF374151),
        text = Color(0xFFE6EDF3),
        text2 = Color(0xFFA0AEC0),
        text3 = Color(0xFF636E7C),
        accent = AccentPresets.Blue,
        accentDim = AccentPresets.Blue.copy(alpha = 0.33f),
        accentGlow = AccentPresets.Blue.copy(alpha = 0.20f)
    )

    val Light = StreambertColors(
        bg = Color(0xFFEBEBED),
        surface = Color(0xFFF8F8FA),
        surface2 = Color(0xFFEEEEF0),
        surface3 = Color(0xFFE0E0E2),
        border = Color(0xFFD0D0D2),
        text = Color(0xFF111113),
        text2 = Color(0xFF555557),
        text3 = Color(0xFF999999),
        accent = AccentPresets.Red,
        accentDim = AccentPresets.Red.copy(alpha = 0.33f),
        accentGlow = AccentPresets.Red.copy(alpha = 0.20f)
    )

    fun get(themeId: String, accentHex: String): StreambertColors {
        val base = when (themeId.lowercase()) {
            "amoled" -> Amoled
            "mocha" -> Mocha
            "slate" -> Slate
            "light" -> Light
            else -> Dark
        }
        val accentColor = AccentPresets.fromHex(accentHex)
        return base.copy(
            accent = accentColor,
            accentDim = accentColor.copy(alpha = 0.33f),
            accentGlow = accentColor.copy(alpha = 0.20f)
        )
    }
}

val LocalStreambertColors = staticCompositionLocalOf { ThemePresets.Dark }

val StreambertTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun StreambertTheme(
    themeId: String = "dark",
    accentHex: String = "#e50914",
    content: @Composable () -> Unit
) {
    val streamColors = ThemePresets.get(themeId, accentHex)
    val isLight = themeId.lowercase() == "light"

    val colorScheme = if (isLight) {
        lightColorScheme(
            primary = streamColors.accent,
            onPrimary = Color.White,
            background = streamColors.bg,
            onBackground = streamColors.text,
            surface = streamColors.surface,
            onSurface = streamColors.text,
            surfaceVariant = streamColors.surface2,
            onSurfaceVariant = streamColors.text2,
            outline = streamColors.border,
            secondary = streamColors.surface2,
            onSecondary = streamColors.text
        )
    } else {
        darkColorScheme(
            primary = streamColors.accent,
            onPrimary = Color.White,
            background = streamColors.bg,
            onBackground = streamColors.text,
            surface = streamColors.surface,
            onSurface = streamColors.text,
            surfaceVariant = streamColors.surface2,
            onSurfaceVariant = streamColors.text2,
            outline = streamColors.border,
            secondary = streamColors.surface2,
            onSecondary = streamColors.text
        )
    }

    CompositionLocalProvider(LocalStreambertColors provides streamColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = StreambertTypography,
            content = content
        )
    }
}
