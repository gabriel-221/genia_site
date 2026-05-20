package com.genoboi.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColorScheme = lightColorScheme(
    primary          = GenoGreen800,
    onPrimary        = GenoWhite,
    primaryContainer = GenoGreen100,
    onPrimaryContainer = GenoGreen900,
    secondary        = GenoGreen600,
    onSecondary      = GenoWhite,
    secondaryContainer = GenoGreen50,
    onSecondaryContainer = GenoGreen800,
    background       = GenoGray50,
    onBackground     = GenoGray900,
    surface          = GenoWhite,
    onSurface        = GenoGray900,
    surfaceVariant   = GenoGray100,
    onSurfaceVariant = GenoGray600,
    error            = GenoRed,
    onError          = GenoWhite,
    outline          = GenoGray200,
)

val GenoTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize   = 28.sp,
        color      = GenoGray900
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize   = 22.sp,
        color      = GenoGray900
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 18.sp,
        color      = GenoGray900
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 15.sp,
        color      = GenoGray900
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp,
        color      = GenoGray900
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        color      = GenoGray600
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp,
        color      = GenoGray400
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 11.sp,
        color      = GenoGray400
    )
)

@Composable
fun GenoBOiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme, // sempre light por ora
        typography  = GenoTypography,
        content     = content
    )
}
