package com.ltcn272.finny.presentation.features.notification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.Notification
import com.ltcn272.finny.presentation.common.ui.CircleNavigationButton
import com.ltcn272.finny.presentation.common.util.formatDate
import com.ltcn272.finny.presentation.features.notification.component.NotificationInfoCard
import com.ltcn272.finny.presentation.features.notification.component.NotificationItem
import com.ltcn272.finny.presentation.features.notification.component.NotificationTab
import com.ltcn272.finny.presentation.theme.MainBackgroundBrush
import java.time.LocalDate
import java.time.ZonedDateTime

data class DayGroup(val date: LocalDate, val notifications: List<Notification>)

@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val lazyNotifications: LazyPagingItems<Notification> = viewModel.notificationsPagingFlow.collectAsLazyPagingItems()
    
    val groupedNotifications = remember(lazyNotifications.itemSnapshotList, uiState.notificationToShow) {
        lazyNotifications.itemSnapshotList.items
            .groupBy { it.createdAt.toLocalDate() }
            .map { (date, notifications) -> DayGroup(date, notifications) }
            .sortedByDescending { it.date }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MainBackgroundBrush)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            // Top Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                CircleNavigationButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    onClick = onBack
                )
                Text(
                    stringResource(id = R.string.notifications_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NotificationTab(
                    text = stringResource(R.string.tab_all),
                    isSelected = uiState.selectedTab == NotificationTabState.ALL,
                    onClick = { viewModel.onTabSelected(NotificationTabState.ALL) },
                    modifier = Modifier.weight(1f)
                )
                NotificationTab(
                    text = stringResource(R.string.tab_unread),
                    isSelected = uiState.selectedTab == NotificationTabState.UNREAD,
                    onClick = { viewModel.onTabSelected(NotificationTabState.UNREAD) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Chỉ hiển thị loading indicator khi tải lần đầu
            if (lazyNotifications.loadState.refresh is LoadState.Loading) {
                LinearProgressIndicator(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp))
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (lazyNotifications.loadState.refresh is LoadState.NotLoading) {
                    if (groupedNotifications.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(id = R.string.no_notifications),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 50.dp),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        groupedNotifications.forEach { (date, notifications) ->
                            // Day Header
                            item(key = "header_$date") {
                                val today = ZonedDateTime.now().toLocalDate()
                                val yesterday = today.minusDays(1)
                                val title = when {
                                    date.isEqual(today) -> stringResource(R.string.today)
                                    date.isEqual(yesterday) -> stringResource(R.string.yesterday)
                                    else -> formatDate(date.atStartOfDay(ZonedDateTime.now().zone))
                                }
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            // Notification Items
                            items(
                                count = notifications.size,
                                key = { index -> notifications[index].id }
                            ) { index ->
                                NotificationItem(
                                    notification = notifications[index],
                                    onClick = { viewModel.showNotification(notifications[index]) }
                                )
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = uiState.notificationToShow != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            uiState.notificationToShow?.let {
                NotificationInfoCard(
                    notification = it,
                    isProcessing = uiState.isMarkingAsRead,
                    onDismiss = viewModel::dismissNotification
                )
            }
        }
    }
}
