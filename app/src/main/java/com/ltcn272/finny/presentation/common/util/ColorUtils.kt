package com.ltcn272.finny.presentation.common.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

fun generateHarmonicColors(baseColor: Color, count: Int): List<Color> {
    if (count <= 0) return emptyList()
    if (count == 1) return listOf(baseColor)

    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(baseColor.toArgb(), hsv)
    val baseHue = hsv[0]

    return List(count) { i ->
        val hue = (baseHue + i * (360f / count)) % 360f

        val saturation = if (i % 2 == 0) 0.8f else 0.9f
        val value = if (i % 2 == 0) 0.9f else 0.8f

        Color.hsv(hue, saturation, value)
    }
}
