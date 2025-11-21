package com.ltcn272.finny.presentation.common.util

import androidx.compose.ui.graphics.Color
import com.ltcn272.finny.domain.model.TransactionCategory

/**
 * A utility object to provide styling (emoji and color) for different transaction categories.
 */
object CategoryStyle {

    /**
     * Data class to hold the style properties for a category.
     */
    data class Style(val emoji: String, val color: Color)

    /**
     * A map linking each TransactionCategory to its specific style.
     */
    private val categoryStyles = mapOf(
        TransactionCategory.FOOD to Style("🍔", Color(0xFFF44336)),
        TransactionCategory.LUNCH to Style("🍱", Color(0xFFFF9800)),
        TransactionCategory.COFFEE to Style("☕", Color(0xFF795548)),
        TransactionCategory.TRANSPORTATION to Style("🚗", Color(0xFF2196F3)),
        TransactionCategory.SHOPPING to Style("🛍️", Color(0xFF9C27B0)),
        TransactionCategory.HOUSING to Style("🏠", Color(0xFF4CAF50)),
        TransactionCategory.UTILITIES to Style("💡", Color(0xFFFFC107)),
        TransactionCategory.HEALTHCARE to Style("❤️", Color(0xFFE91E63)),
        TransactionCategory.ENTERTAINMENT to Style("🎬", Color(0xFF3F51B5)),
        TransactionCategory.EDUCATION to Style("🎓", Color(0xFF009688)),
        TransactionCategory.SALARY to Style("💰", Color(0xFF8BC34A)),
        TransactionCategory.GIFT to Style("🎁", Color(0xFFFF5722)),
        TransactionCategory.OTHER to Style("📎", Color(0xFF9E9E9E))
    )

    /**
     * Returns the Style (emoji and color) for a given TransactionCategory.
     *
     * @param category The transaction category.
     * @return The corresponding Style, or a default style if the category is not found.
     */
    fun getStyle(category: TransactionCategory): Style {
        return categoryStyles[category] ?: Style("❓", Color.Gray)
    }
}
