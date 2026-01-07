package com.ltcn272.finny.presentation.features.home.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R
import com.ltcn272.finny.presentation.common.util.NetworkStatus
import kotlinx.coroutines.delay

@Composable
fun NetworkStatusIndicator(
    modifier: Modifier = Modifier,
    networkStatus: NetworkStatus
) {
    var showOnlineStatus by remember { mutableStateOf(false) }
    var previousStatus by remember { mutableStateOf(networkStatus) }

    LaunchedEffect(networkStatus) {
        if (previousStatus != NetworkStatus.Available && networkStatus == NetworkStatus.Available) {
            showOnlineStatus = true
            delay(2000)
            showOnlineStatus = false
        }
        previousStatus = networkStatus
    }

    val isOffline = when (networkStatus) {
        NetworkStatus.Available -> false
        NetworkStatus.Unavailable, NetworkStatus.Losing, NetworkStatus.Lost -> true
    }
    val isOnlineTemporarily = showOnlineStatus

    val backgroundColor = when {
        isOffline -> Color.Black.copy(alpha = 0.8f)
        isOnlineTemporarily -> Color(0xFF28A745).copy(alpha = 0.9f)
        else -> Color.Transparent
    }

    val text = if (isOffline) {
        stringResource(R.string.offline_mode)
    } else {
        stringResource(R.string.online_mode)
    }

    val icon = if (isOffline) {
        Icons.Default.CloudOff
    } else {
        Icons.Default.CloudQueue
    }

    AnimatedVisibility(
        visible = isOffline || isOnlineTemporarily,
        enter = expandVertically(animationSpec = tween(300)),
        exit = shrinkVertically(animationSpec = tween(300))
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
