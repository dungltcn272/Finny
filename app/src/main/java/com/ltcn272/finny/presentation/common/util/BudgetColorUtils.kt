package com.ltcn272.finny.presentation.common.util

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

object BudgetColorUtils {
    fun generateRandomColor(): Color {
        val random = Random.Default
        return Color(
            red = random.nextFloat(),
            green = random.nextFloat(),
            blue = random.nextFloat(),
            alpha = 1f
        )
    }
}
