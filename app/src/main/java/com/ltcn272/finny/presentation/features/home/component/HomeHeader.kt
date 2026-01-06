package com.ltcn272.finny.presentation.features.home.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ltcn272.finny.R

@Composable
fun HomeHeader(
    username: String,
    currentDate: String,
    onNotificationClick: () -> Unit,
    bankNotificationCount: Int,
    unreadNotificationCount: Int,
    onBankNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {

        // LEFT
        HomeGreeting(
            username = username,
            currentDate = currentDate
        )

        Spacer(modifier = Modifier.weight(1f))
        // RIGHT
        Row(verticalAlignment = Alignment.CenterVertically) {
            HeaderIconButton(
                icon = Icons.Default.AccountBalanceWallet,
                contentDescription = "Bank Notifications",
                onClick = onBankNotificationClick,
                badgeCount = bankNotificationCount
            )
            Spacer(modifier = Modifier.width(8.dp))
            HeaderIconButton(
                icon = Icons.Default.Notifications,
                contentDescription = "Notifications",
                onClick = onNotificationClick,
                badgeCount = unreadNotificationCount
            )
        }
    }
}

@Composable
private fun HomeGreeting(
    username: String,
    currentDate: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(
                R.string.home_greeting,
                username
            ),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = currentDate,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun HeaderIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    badgeCount: Int = 0
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            shadowElevation = 3.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.size(40.dp)
        ) {}

        BadgedBox(
            badge = {
                if (badgeCount > 0) {
                    val badgeText = if (badgeCount > 99) "99+" else badgeCount.toString()
                    Badge { Text(text = badgeText, fontSize = 8.sp) }
                }
            }
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

