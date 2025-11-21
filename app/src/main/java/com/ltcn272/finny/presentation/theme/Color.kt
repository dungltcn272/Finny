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
val BackgroundWhite = Color(0xFFFFFFFF)

val PinkTop = Color(0xFFF8BBD0)

val IntroBackgroundBrushWithOpacity = Brush.verticalGradient(
    colorStops = arrayOf(
        0.52f to PurplePrimary.copy(alpha = 0.2f), // 20% Opacity
        0.93f to PinkAccent.copy(alpha = 0.2f)
    )
)

val MainBackgroundBrush: Brush = Brush.linearGradient(
    colorStops = arrayOf(
        0.54f to LightGradientBlue.copy(alpha = 0.5f),
        0.93f to BackgroundWhite.copy(alpha = 0.5f)
    )
)
val BudgetBackground = Brush.linearGradient(
    colorStops = arrayOf(
        0.0f to PinkTop,
        0.9f to Color(0xFFF5F2F4),
    ),
    start = Offset.Zero,
    end = Offset(0f, Float.POSITIVE_INFINITY)
)

// Create Budget Screen Colors
val BudgetName = Color(0xFFB6B6B6)
val BudgetTitle = Color(0xFF000000)
val BudgetSectionBackground = Color(0xFFF5F5F5)
val BudgetChipBorder = Color(0xFFDADADA)
val BudgetPeriodBackground = Color(0xFFE7E7E7)
val BudgetAmountCardBackground = Color.White
val BudgetIconTint = Color(0xFF828282)
val BudgetSecondaryText = Color(0xFFBDBDBD)
val BudgetChipSelectedBackground = Color.White
