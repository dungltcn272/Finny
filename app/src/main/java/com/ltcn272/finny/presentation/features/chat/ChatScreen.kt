package com.ltcn272.finny.presentation.features.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.util.ErrorType
import com.ltcn272.finny.presentation.common.ui.CircleNavigationButton
import com.ltcn272.finny.presentation.common.ui.FinnySnackbar
import com.ltcn272.finny.presentation.features.chat.component.MessageInput
import com.ltcn272.finny.presentation.features.chat.component.MessageItem
import com.ltcn272.finny.presentation.features.chat.component.TypingIndicator
import com.ltcn272.finny.presentation.theme.MainBackgroundBrush
import java.time.Duration
import kotlin.math.abs

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val messages = viewModel.messages.collectAsLazyPagingItems()

    val snackbarMessage = uiState.snackbarState.errorType?.let {
        when (it) {
            ErrorType.NETWORK -> stringResource(R.string.error_network)
            ErrorType.TIMEOUT -> stringResource(R.string.error_timeout)
            ErrorType.UNAUTHORIZED -> stringResource(R.string.error_unauthorized)
            ErrorType.SERVER_ERROR -> stringResource(R.string.error_server)
            ErrorType.UNKNOWN -> stringResource(R.string.error_unknown)
        }
    } ?: ""

    LaunchedEffect(uiState.refreshTrigger) {
        if (uiState.refreshTrigger > 0) {
            messages.refresh()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MainBackgroundBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                CircleNavigationButton(
                    icon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                Text(
                    text = stringResource(id = R.string.ai_assistant),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            val listState = rememberLazyListState()
            val focusManager = LocalFocusManager.current

            LaunchedEffect(messages.itemCount, uiState.pendingMessages.size, uiState.isAiTyping) {
                if (listState.firstVisibleItemIndex <= 1) {
                    listState.animateScrollToItem(0)
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { focusManager.clearFocus() })
                    }
            ) {
                val isFirstLoad = messages.loadState.refresh is LoadState.Loading && messages.itemCount == 0
                if (isFirstLoad) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                    )
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    reverseLayout = true
                ) {
                    item(key = "typing_indicator") {
                        if (uiState.isAiTyping) {
                            TypingIndicator()
                        }
                    }

                    items(
                        items = uiState.pendingMessages.reversed(),
                        key = { "pending_${it.id}" }
                    ) { message ->
                        MessageItem(
                            message = message,
                            isFirstInGroup = true,
                            isLastInGroup = true,
                            showTimestamp = true,
                            currencyCode = uiState.currencyCode
                        )
                    }

                    items(
                        count = messages.itemCount,
                        key = { index -> "confirmed_${messages.peek(index)?.id}" }
                    ) { index ->
                        val message = messages[index]
                        if (message != null) {
                            val prevMessage = if (index < messages.itemCount - 1) messages.peek(index + 1) else null
                            val nextMessage = if (index > 0) messages.peek(index - 1) else null

                            val isFirstInGroup = prevMessage == null || prevMessage.isFromUser != message.isFromUser
                            val isLastInGroup = nextMessage == null || nextMessage.isFromUser != message.isFromUser

                            val showTimestamp = isLastInGroup || (nextMessage != null && abs(
                                Duration.between(message.timestamp, nextMessage.timestamp).toMinutes()) >= 1)

                            MessageItem(
                                message = message,
                                isFirstInGroup = isFirstInGroup,
                                isLastInGroup = isLastInGroup,
                                showTimestamp = showTimestamp,
                                currencyCode = uiState.currencyCode
                            )
                        }
                    }

                    if (messages.loadState.append is LoadState.Loading) {
                        item(key = "append_loader") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }

            MessageInput(onMessageSent = viewModel::sendMessage)
        }

        FinnySnackbar(
            visible = uiState.snackbarState.visible,
            message = snackbarMessage,
            type = uiState.snackbarState.type,
            onDismiss = viewModel::onSnackbarDismissed
        )
    }
}
