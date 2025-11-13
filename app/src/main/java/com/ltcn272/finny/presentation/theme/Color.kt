package com.ltcn272.finny.presentation.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
val PurplePrimary = Color(0xFF654EA3)
val PinkAccent = Color(0xFFEAAFC8)


val IntroBackgroundBrush = Brush.verticalGradient(
    colorStops = arrayOf(
        0.52f to PurplePrimary.copy(alpha = 0.2f), // 52%
        0.93f to PinkAccent.copy(alpha = 0.2f)   // 93%
    )
)