package com.ltcn272.finny.presentation.common.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.ltcn272.finny.domain.model.TransactionCategory

object CategoryUtils {

    data class Style(val emoji: String, val icon: ImageVector, val color: Color, val backgroundColor: Color)

    private val categoryStyles = mapOf(
        TransactionCategory.FOOD to Style("🍔", Icons.Default.Restaurant, Color(0xFFF57F17), Color(0xFFFFF8E1)),
        TransactionCategory.LUNCH to Style("🍱", Icons.Default.LunchDining, Color(0xFFF57F17), Color(0xFFFFF8E1)),
        TransactionCategory.COFFEE to Style("☕", Icons.Default.Coffee, Color(0xFF6D4C41), Color(0xFFEFEBE9)),
        TransactionCategory.TRANSPORTATION to Style("🚗", Icons.Default.DirectionsCar, Color(0xFF1976D2), Color(0xFFE3F2FD)),
        TransactionCategory.SHOPPING to Style("🛍️", Icons.Default.ShoppingCart, Color(0xFF8E24AA), Color(0xFFF3E5F5)),
        TransactionCategory.HOUSING to Style("🏠", Icons.Default.Home, Color(0xFF388E3C), Color(0xFFE8F5E9)),
        TransactionCategory.UTILITIES to Style("💡", Icons.Default.Lightbulb, Color(0xFFFBC02D), Color(0xFFFFFDE7)),
        TransactionCategory.HEALTHCARE to Style("❤️", Icons.Default.Favorite, Color(0xFFD81B60), Color(0xFFFCE4EC)),
        TransactionCategory.ENTERTAINMENT to Style("🎬", Icons.Default.Movie, Color(0xFF303F9F), Color(0xFFE8EAF6)),
        TransactionCategory.EDUCATION to Style("🎓", Icons.Default.School, Color(0xFF00796B), Color(0xFFE0F2F1)),
        TransactionCategory.SALARY to Style("💰", Icons.Default.MonetizationOn, Color(0xFF689F38), Color(0xFFF1F8E9)),
        TransactionCategory.GIFT to Style("🎁", Icons.Default.CardGiftcard, Color(0xFFEF6C00), Color(0xFFFFF3E0)),
        TransactionCategory.OTHER to Style("📎", Icons.Default.Category, Color(0xFF757575), Color(0xFFFAFAFA))
    )

    fun getStyle(category: TransactionCategory): Style {
        return categoryStyles[category] ?: Style("❓", Icons.Default.Category, Color.Gray, Color.LightGray)
    }
}