package com.ltcn272.finny.presentation.common.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.Category
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.domain.model.TransactionType
import com.ltcn272.finny.presentation.common.util.formatCurrency
import com.ltcn272.finny.presentation.common.util.formatDateTime
import java.time.ZonedDateTime

@Composable
fun TransactionItem(
    transaction: Transaction,
    modifier: Modifier = Modifier,
    currencyCode: String,
    onClick: (() -> Unit)? = null
) {
    val isIncome = transaction.type == TransactionType.INCOME

    val amountColor = if (isIncome) Color(0xFF2E7D32) else Color(0xFFC62828)
    val sign = if (isIncome) "+" else "-"

    val iconBgStrong = if (isIncome) Color(0xFF4CAF50) else Color(0xFFEF5350)
    val iconBgLight = iconBgStrong.copy(alpha = 0.15f)

    val amountText = formatCurrency(transaction.amount, currencyCode)
    val dateText = formatDateTime(transaction.dateTime)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable { onClick() }
                else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        color = Color.White,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            /** LEFT ICON **/
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

            /** CENTER **/
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

                Row {
                    Text(
                        text = dateText,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )

                    if (transaction.image != null) {
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = stringResource(
                                R.string.transaction_image_count,
                                1
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            /** AMOUNT **/
            Text(
                text = stringResource(
                    R.string.transaction_amount_format,
                    sign,
                    amountText
                ),
                color = amountColor,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}


@Preview(showBackground = true, name = "Outcome Transaction")
@Composable
fun TransactionItemOutcomePreview() {
    val sampleTransaction = Transaction(
        serverId = "1",
        name = "Ăn tối cùng bạn bè",
        budgetId = "b1",
        type = TransactionType.OUTCOME,
        amount = 150000,
        dateTime = ZonedDateTime.now(),
        description = null,
        image = "has_image", // Giả sử có ảnh để hiển thị "1 Attachment"
        category = Category("c1", "Ăn uống", isDefault = true),
        isRecurring = false,
        recurringInfo = null
    )
    TransactionItem(
        transaction = sampleTransaction,
        currencyCode = "VND",
        onClick = {}
    )
}

@Preview(showBackground = true, name = "Income Transaction")
@Composable
fun TransactionItemIncomePreview() {
    val sampleTransaction = Transaction(
        serverId = "2",
        name = "Tiền lương tháng 10",
        budgetId = "b2",
        type = TransactionType.INCOME,
        amount = 10000000,
        dateTime = ZonedDateTime.now().minusDays(2),
        description = null,
        image = null, // Không có ảnh
        category = Category("c1", "Ăn uống", isDefault = true),
        isRecurring = false,
        recurringInfo = null
    )
    TransactionItem(
        transaction = sampleTransaction,
        currencyCode = "VND",
        onClick = {}
    )
}
