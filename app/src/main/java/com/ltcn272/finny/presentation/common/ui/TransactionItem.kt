package com.ltcn272.finny.presentation.common.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.domain.model.TransactionType
import com.ltcn272.finny.presentation.common.util.formatCurrency
import com.ltcn272.finny.presentation.common.util.formatDate
import kotlin.math.max

@Composable
fun TransactionItem(
    transaction: Transaction,
    modifier: Modifier = Modifier,
    currencyCode: String,
    onClick: (() -> Unit) = {}
) {
    val isIncome = transaction.type == TransactionType.INCOME

    val amountColor = if (isIncome) Color(0xFF2E7D32) else Color(0xFFC62828)
    val sign = if (isIncome) "+" else "-"

    val iconBgStrong = if (isIncome) Color(0xFF4CAF50) else Color(0xFFEF5350)
    val iconBgLight = iconBgStrong.copy(alpha = 0.15f)

    val amountText = formatCurrency(transaction.amount, currencyCode)
    val dateText = formatDate(transaction.dateTime)

    val attachmentString = if (!transaction.image.isNullOrEmpty())
        " • ${stringResource(R.string.transaction_image_count, 1)}"
    else ""
    val secondaryInfoText = "$dateText$attachmentString"
    val adaptiveAmountFontSize = remember(amountText) {
        calculateAdaptiveFontSize(amountText)
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        color = Color.White,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // LEFT ICON
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(iconBgLight, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(iconBgStrong, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isIncome)
                            Icons.Default.ArrowDownward
                        else
                            Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            // CENTER
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.name,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = secondaryInfoText,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // AMOUNT
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(
                        R.string.transaction_amount_format,
                        sign,
                        amountText
                    ),
                    color = amountColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = adaptiveAmountFontSize,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
                transaction.category?.name?.let { categoryName ->
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = categoryName,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}

private fun calculateAdaptiveFontSize(amountText: String): TextUnit {
    val baseSize = 16.sp
    val minSize = 12.sp

    val digitCount = amountText.count { it.isDigit() }

    if (digitCount <= 6) {
        return baseSize
    }

    val reductionSteps = (digitCount - 6) / 2
    val reducedSizeValue = baseSize.value - (reductionSteps * 1.5f)
    val reducedSize = reducedSizeValue.sp
    return max(minSize.value, reducedSize.value).sp
}

@Composable
fun TransactionItemShimmer(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFF0F0F0)), // Lighter border for unloaded feel
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT ICON SHIMMER
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .shimmerEffect()
            )

            Spacer(Modifier.width(12.dp))

            // CENTER TEXT SHIMMER
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )

                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
            }

            // RIGHT AMOUNT SHIMMER
            Column(horizontalAlignment = Alignment.End) {
                // Simulate Amount
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )

                Spacer(Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEffect()
                )
            }

            Spacer(Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .shimmerEffect()
            )
        }
    }
}