package com.ltcn272.finny.presentation.theme

import androidx.compose.ui.geometry.Offset
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

val LightGradientBlue = Color(0xFFCFDBF8)
val BackgroundBottom = Color(0xFFF5F6FA)

val PinkTop = Color(0xFFF8BBD0)

val IntroBackgroundBrush= Brush.verticalGradient(
    colorStops = arrayOf(
        0.52f to PurplePrimary.copy(alpha = 0.2f),
        0.93f to PinkAccent.copy(alpha = 0.2f)
    )
)

val MainBackgroundBrush: Brush = Brush.linearGradient(
    colorStops = arrayOf(
        0.3f to LightGradientBlue.copy(alpha = 0.3f), // Start with light blue
        1.0f to BackgroundBottom // End with light gray instead of white
    )
)
val BudgetBackgroundBrush = Brush.linearGradient(
    colorStops = arrayOf(
        0.0f to PinkTop,
        0.9f to Color(0xFFF5F2F4),
    ),
    start = Offset.Zero,
    end = Offset(0f, Float.POSITIVE_INFINITY)
)
val TransactionBackgroundBrush = Brush.linearGradient(
    colorStops = arrayOf(
        0.0f to PinkTop,
        0.9f to Color(0xFFF5F2F4),
    ),
    start = Offset.Zero,
    end = Offset(0f, Float.POSITIVE_INFINITY)
)
