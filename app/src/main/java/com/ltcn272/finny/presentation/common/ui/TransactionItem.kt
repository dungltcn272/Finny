package com.ltcn272.finny.presentation.common.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.domain.model.TransactionCategory
import com.ltcn272.finny.domain.model.TransactionType
import java.text.NumberFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
private fun getCategoryIcon(category: TransactionCategory): Int {
    return when (category) {
        TransactionCategory.COFFEE -> R.drawable.ic_coffee
        TransactionCategory.LUNCH, TransactionCategory.FOOD -> R.drawable.ic_restaurant
        TransactionCategory.SHOPPING -> R.drawable.ic_shopping
        TransactionCategory.TRANSPORTATION -> R.drawable.ic_car
        TransactionCategory.HOUSING -> R.drawable.ic_home
        TransactionCategory.UTILITIES -> R.drawable.ic_bill
        TransactionCategory.HEALTHCARE -> R.drawable.ic_health
        TransactionCategory.ENTERTAINMENT -> R.drawable.ic_entertainment
        TransactionCategory.EDUCATION -> R.drawable.ic_education
        TransactionCategory.SALARY -> R.drawable.ic_salary
        TransactionCategory.GIFT -> R.drawable.ic_gift
        TransactionCategory.OTHER -> R.drawable.ic_other
    }
}

@Composable
private fun getIconBackgroundColor(category: TransactionCategory): Color {
    return when (category) {
        TransactionCategory.COFFEE -> Color(0xFFEFEBE9)
        TransactionCategory.LUNCH, TransactionCategory.FOOD -> Color(0xFFFFF8E1)
        TransactionCategory.SHOPPING -> Color(0xFFF3E5F5)
        TransactionCategory.TRANSPORTATION -> Color(0xFFE3F2FD)
        TransactionCategory.HOUSING -> Color(0xFFE8F5E9)
        TransactionCategory.UTILITIES -> Color(0xFFFFFDE7)
        TransactionCategory.HEALTHCARE -> Color(0xFFFCE4EC)
        TransactionCategory.ENTERTAINMENT -> Color(0xFFE8EAF6)
        TransactionCategory.EDUCATION -> Color(0xFFE0F2F1)
        TransactionCategory.SALARY -> Color(0xFFF1F8E9)
        TransactionCategory.GIFT -> Color(0xFFFFF3E0)
        TransactionCategory.OTHER -> Color(0xFFFAFAFA)
    }
}

@Composable
private fun getIconColor(category: TransactionCategory): Color {
    return when (category) {
        TransactionCategory.COFFEE -> Color(0xFF6D4C41)
        TransactionCategory.LUNCH, TransactionCategory.FOOD -> Color(0xFFF57F17)
        TransactionCategory.SHOPPING -> Color(0xFF8E24AA)
        TransactionCategory.TRANSPORTATION -> Color(0xFF1976D2)
        TransactionCategory.HOUSING -> Color(0xFF388E3C)
        TransactionCategory.UTILITIES -> Color(0xFFFBC02D)
        TransactionCategory.HEALTHCARE -> Color(0xFFD81B60)
        TransactionCategory.ENTERTAINMENT -> Color(0xFF303F9F)
        TransactionCategory.EDUCATION -> Color(0xFF00796B)
        TransactionCategory.SALARY -> Color(0xFF689F38)
        TransactionCategory.GIFT -> Color(0xFFEF6C00)
        TransactionCategory.OTHER -> Color(0xFF757575)
    }
}


@Composable
fun TransactionItem(
    transaction: Transaction,
    currency: String,
    modifier: Modifier = Modifier,
    onClick: (Transaction) -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = { onClick(transaction) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(getIconBackgroundColor(transaction.category)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(getCategoryIcon(transaction.category)),
                    contentDescription = transaction.category.name,
                    tint = getIconColor(transaction.category),
                    modifier = Modifier
                        .padding(10.dp)
                        .size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                val transactionTitle = "${transaction.category.name.replaceFirstChar { it.titlecase() }} - ${transaction.name}"
                Text(
                    text = transactionTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee(),
                    overflow = TextOverflow.Ellipsis
                )

                val localDateTime = transaction.dateTime.withZoneSameInstant(ZoneId.systemDefault())
                val time = localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
                val description = transaction.description ?: ""
                val location = transaction.location

                val subtitleParts = mutableListOf<String>()
                if (time.isNotEmpty()) subtitleParts.add(time)
                if (description.isNotEmpty()) subtitleParts.add(description)

                val timeAndDescText = subtitleParts.joinToString(" - ")
                val locationText = "" + location?.name.let { if (it.isNullOrBlank()) "" else "· $it" }

                val fullSubtitleText = if (timeAndDescText.isNotEmpty() && locationText.isNotEmpty()) {
                    "$timeAndDescText $locationText"
                } else {
                    timeAndDescText + locationText.replaceFirst("· ", "")
                }


                if (fullSubtitleText.isNotBlank()) {
                    Text(
                        text = fullSubtitleText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(),
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                val sign = if (transaction.type == TransactionType.OUTCOME) "-" else "+"
                val formattedAmount =
                    NumberFormat.getIntegerInstance(Locale.US).format(transaction.amount.toLong())

                Text(
                    text = "$sign$formattedAmount $currency",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = if (transaction.type == TransactionType.OUTCOME) Color.Black else Color(0xFF4CAF50)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    painter = painterResource(R.drawable.ic_right),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
