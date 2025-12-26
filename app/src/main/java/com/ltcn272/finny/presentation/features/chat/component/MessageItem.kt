package com.ltcn272.finny.presentation.features.chat.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.Chat
import com.ltcn272.finny.domain.model.MessageCard
import com.ltcn272.finny.presentation.common.util.formatCurrency
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun formatTimeAgo(time: LocalDateTime): String {
    val zonedServerTime = time.atZone(ZoneId.of("UTC"))
    val zonedNow = ZonedDateTime.now()
    val duration = Duration.between(zonedServerTime, zonedNow)

    return when {
        duration.seconds < 10 -> stringResource(R.string.time_ago_now)
        duration.seconds < 60 -> stringResource(R.string.time_ago_seconds)
        duration.toMinutes() < 60 -> stringResource(R.string.time_ago_minutes, duration.toMinutes())
        duration.toHours() < 24 -> stringResource(R.string.time_ago_hours, duration.toHours())
        zonedServerTime.toLocalDate() == zonedNow.toLocalDate().minusDays(1) -> stringResource(R.string.time_ago_yesterday)
        else -> time.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }
}

@Composable
fun MessageItem(
    message: Chat,
    isFirstInGroup: Boolean,
    isLastInGroup: Boolean,
    showTimestamp: Boolean,
    currencyCode: String
) {
    val isUserMessage = message.isFromUser
    val alignment = if (isUserMessage) Alignment.End else Alignment.Start
    val backgroundColor = if (isUserMessage) MaterialTheme.colorScheme.primary else Color.White
    val textColor = if (isUserMessage) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface


    val shape = when {
        isUserMessage -> RoundedCornerShape(
            topStart = 15.dp,
            topEnd = if (isFirstInGroup) 15.dp else 4.dp,
            bottomStart = 15.dp,
            bottomEnd = if (isLastInGroup) 15.dp else 4.dp
        )
        else -> RoundedCornerShape(
            topStart = if (isFirstInGroup) 15.dp else 4.dp,
            topEnd = 15.dp,
            bottomStart = if (isLastInGroup) 15.dp else 4.dp,
            bottomEnd = 15.dp
        )
    }

    val verticalPadding = if (isLastInGroup) 12.dp else 2.dp

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        if (message.text.isNotBlank()) {
            Surface(
                modifier = Modifier.widthIn(max = 300.dp),
                color = backgroundColor,
                shape = shape,
                shadowElevation = 1.dp
            ) {
                Text(
                    text = message.text,
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }

        if (message.cards.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Column(
                modifier = Modifier.widthIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                message.cards.forEach { card ->
                    MessageTransactionCard(card = card, currencyCode = currencyCode)
                }
            }
        }


        if (showTimestamp) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatTimeAgo(time = message.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(verticalPadding))
    }
}


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
    val typeText = if (isOutcome) stringResource(R.string.chat_card_outcome) else stringResource(R.string.chat_card_income)

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
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f))
                    .padding(2.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = typeText,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = card.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
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
