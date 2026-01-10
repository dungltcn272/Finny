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
import java.time.ZonedDateTime

@Composable
fun NotificationScreen(
    viewModel: NotificationViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val lazyNotifications: LazyPagingItems<Notification> = viewModel.notificationsPagingFlow.collectAsLazyPagingItems()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MainBackgroundBrush)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
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
                    modifier = Modifier.weight(1f),
                    count = uiState.unreadCount
                )
            }

            if (lazyNotifications.loadState.refresh is LoadState.Loading) {
                LinearProgressIndicator(modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp))
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (lazyNotifications.loadState.refresh is LoadState.NotLoading && lazyNotifications.itemCount == 0) {
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
                }

                items(
                    count = lazyNotifications.itemCount,
                    key = { index ->
                        val item = lazyNotifications.peek(index)
                        val isRead = item?.isRead == true || uiState.readIds.contains(item?.id)
                        "${item?.id}_$isRead"
                    }
                ) { index ->
                    val notification = lazyNotifications[index]
                    if (notification != null) {
                        val isRead = notification.isRead || uiState.readIds.contains(notification.id)
                        val displayNotification = if (isRead && !notification.isRead) {
                            notification.copy(isRead = true)
                        } else notification

                        val prevNotification = if (index > 0) lazyNotifications.peek(index - 1) else null
                        val isPrevRead = prevNotification?.isRead == true || uiState.readIds.contains(prevNotification?.id)
                        val prevDisplayNotification = if (isPrevRead && prevNotification?.isRead == false) {
                            prevNotification.copy(isRead = true)
                        } else prevNotification

                        val showHeader = prevDisplayNotification == null ||
                                prevDisplayNotification.createdAt.toLocalDate() != displayNotification.createdAt.toLocalDate()

                        if (showHeader) {
                            val date = displayNotification.createdAt.toLocalDate()
                            val today = ZonedDateTime.now().toLocalDate()
                            val yesterday = today.minusDays(1)
                            val title = when {
                                date.isEqual(today) -> stringResource(R.string.today)
                                date.isEqual(yesterday) -> stringResource(R.string.yesterday)
                                else -> formatDate(date.atStartOfDay(ZonedDateTime.now().zone))
                            }
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                                )
                            }
                        }

                        NotificationItem(
                            notification = displayNotification,
                            onClick = { viewModel.showNotification(displayNotification) }
                        )
                    }
                }

                if (lazyNotifications.loadState.append is LoadState.Loading) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
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
