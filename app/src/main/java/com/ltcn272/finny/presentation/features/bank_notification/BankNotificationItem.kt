package com.ltcn272.finny.presentation.features.bank_notification

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.MessageCard
import com.ltcn272.finny.presentation.common.util.formatCurrency

@Composable
fun BankNotificationItem(
    notification: PendingBankNotification,
    onSend: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header với nút xóa
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = notification.appName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                if (notification.cardResult == null) {
                    IconButton(
                        onClick = onDelete,
                        enabled = !notification.isLoading,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = if (!notification.isLoading) MaterialTheme.colorScheme.error else Color.Gray
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notification.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notification.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Phần nội dung thay đổi
            AnimatedContent(
                targetState = Triple(notification.isLoading, notification.cardResult != null, notification.cardResult),
                transitionSpec = {
                    (slideInVertically { height -> height } + fadeIn())
                        .togetherWith(slideOutVertically { height -> -height } + fadeOut())
                },
                label = "NotificationStateAnimation"
            ) { (isLoading, hasResult, cardResult) ->
                when {
                    isLoading -> { // TRẠNG THÁI LOADING
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Đang phân tích giao dịch...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    hasResult && cardResult != null -> { // TRẠNG THÁI THÀNH CÔNG
                        MessageTransactionCard(card = cardResult, currencyCode = "VND")
                    }
                    else -> { // TRẠNG THÁI BAN ĐẦU
                        // --- SỬA LẠI Ở ĐÂY ---
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start // <-- ĐỔI THÀNH START
                        ) {
                            Button(onClick = onSend, shape = CircleShape) {
                                Text("Ghi nhận")
                            }
                        }
                        // --- KẾT THÚC SỬA ---
                    }
                }
            }
        }
    }
}

// Composable MessageTransactionCard giữ nguyên không đổi
@Composable
fun MessageTransactionCard(
    card: MessageCard,
    currencyCode: String
) {
    val isOutcome = card.type.equals("outcome", ignoreCase = true)
    val color = if (isOutcome) Color(0xFFC62828) else Color(0xFF2E7D32)
    val backgroundColor = color.copy(alpha = 0.1f)
    val icon = if (isOutcome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
    val sign = if (isOutcome) "-" else "+"
    val typeText = if (isOutcome) "Chi tiêu" else "Thu nhập"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = typeText,
                tint = color,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f))
                    .padding(4.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = typeText,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (card.description.isNotBlank()) {
                    Text(
                        text = card.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Text(
                text = "$sign${formatCurrency(card.amount, currencyCode)}",
                color = color,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
