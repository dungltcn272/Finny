package com.ltcn272.finny.presentation.features.notification.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.Notification
import com.ltcn272.finny.domain.model.NotificationType
import com.ltcn272.finny.presentation.common.util.formatDate
import java.time.Duration
import java.time.ZonedDateTime

data class NotificationDisplayInfo(
    val icon: ImageVector,
    val color: Color
)

@Composable
private fun rememberNotificationDisplayInfo(type: NotificationType): NotificationDisplayInfo {
    return remember(type) {
        when (type) {
            NotificationType.TRANSACTION_CREATED,
            NotificationType.TRANSACTION_UPDATED,
            NotificationType.TRANSACTION_DELETED -> NotificationDisplayInfo(
                Icons.Default.CreditCard,
                Color(0xFF4A8BFF)
            )

            NotificationType.BUDGET_CREATED -> NotificationDisplayInfo(
                Icons.Default.CheckCircle,
                Color(0xFF28B485)
            )

            NotificationType.BUDGET_THRESHOLD_REACHED -> NotificationDisplayInfo(
                Icons.Default.Error,
                Color(0xFFFD7F6B)
            )

            NotificationType.BUDGET_PERIOD_END -> NotificationDisplayInfo(
                Icons.Default.Flag,
                Color(0xFF8A7BFF)
            )

            NotificationType.INCOME_AUTO_RECURRING -> NotificationDisplayInfo(
                Icons.Default.Sync,
                Color(0xFF4CAF50)
            )

            NotificationType.UNKNOWN -> NotificationDisplayInfo(
                Icons.Default.NotificationsActive,
                Color.Gray
            )
        }
    }
}

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val displayInfo = rememberNotificationDisplayInfo(type = notification.type)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = if (notification.isRead) 1.dp else 4.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(displayInfo.color.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = displayInfo.icon,
                    contentDescription = notification.type.name,
                    tint = displayInfo.color,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .basicMarquee()
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = notification.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .basicMarquee()
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = formatRelativeTime(notification.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            // Unread Indicator
            if (!notification.isRead) {
                val alpha = remember { Animatable(1f) }

                LaunchedEffect(notification.id) {
                    alpha.animateTo(
                        targetValue = 0.2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 1000),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.Bottom)
                        .size(8.dp)
                        .alpha(alpha.value)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
        }
    }
}


@Composable
private fun formatRelativeTime(dateTime: ZonedDateTime): String {
    val now = ZonedDateTime.now()
    val duration = remember(dateTime) { Duration.between(dateTime, now) }

    return when {
        duration.toDays() >= 1 -> formatDate(dateTime)
        duration.toHours() > 0 -> {
            val hours = duration.toHours().toInt()
            pluralStringResource(R.plurals.hours_ago, hours, hours)
        }

        duration.toMinutes() > 0 -> {
            val minutes = duration.toMinutes().toInt()
            pluralStringResource(R.plurals.minutes_ago, minutes, minutes)
        }

        else -> stringResource(R.string.just_now)
    }
}
