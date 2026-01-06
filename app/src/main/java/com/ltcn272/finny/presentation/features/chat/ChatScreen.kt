package com.ltcn272.finny.presentation.features.chat

import android.Manifest
import com.ltcn272.finny.presentation.features.chat.component.SuggestionChip
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.ltcn272.finny.R
import com.ltcn272.finny.presentation.common.ui.CircleNavigationButton
import com.ltcn272.finny.presentation.features.chat.component.MessageInput
import com.ltcn272.finny.presentation.features.chat.component.MessageItem
import com.ltcn272.finny.presentation.features.chat.component.TypingIndicator
import com.ltcn272.finny.presentation.features.snackbar.LocalSnackbarManager
import com.ltcn272.finny.presentation.features.snackbar.TopSnackbarType
import com.ltcn272.finny.presentation.theme.MainBackgroundBrush
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import java.time.Duration
import kotlin.math.abs

@Composable
fun ChatScreen(
    viewModel: ChatViewModel = hiltViewModel(),
    onBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val messages = viewModel.messages.collectAsLazyPagingItems()
    val snackbarManager = LocalSnackbarManager.current

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> viewModel.onPermissionResult(isGranted, snackbarManager) }
    )

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let { viewModel.onImageSelected(it) } }
    )

    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.errorEvent.collectLatest { message ->
            snackbarManager.showMessage(message, TopSnackbarType.ERROR)
        }
    }

    LaunchedEffect(messages.itemCount) {
        snapshotFlow { messages.loadState.refresh }
            .distinctUntilChanged()
            .filter { it is LoadState.NotLoading }
            .collect {
                if (listState.firstVisibleItemIndex <= 1) {
                    listState.animateScrollToItem(0)
                }
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

            val focusManager = LocalFocusManager.current

            LaunchedEffect(uiState.sessionMessages.size) {
                if (uiState.sessionMessages.isNotEmpty()) {
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
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp),
                    reverseLayout = true
                ) {
                    if (uiState.isAiTyping) {
                        item(key = "typing_indicator") {
                            TypingIndicator()
                        }
                    }

                    items(
                        items = uiState.sessionMessages,
                        key = { "session_${it.id}" }
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
                        key = messages.itemKey { it.id }
                    ) { index ->
                        val message = messages[index]
                        if (message != null) {
                            val prevMessage = if (index < messages.itemCount - 1) messages.peek(index + 1) else null
                            val nextMessage = if (index > 0) messages.peek(index - 1) else null

                            val isFirstInGroup = prevMessage == null || prevMessage.isFromUser != message.isFromUser
                            val isLastInGroup = (nextMessage == null && uiState.sessionMessages.isEmpty()) ||
                                    (nextMessage != null && nextMessage.isFromUser != message.isFromUser)

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

            AnimatedVisibility(
                visible = uiState.showSuggestions,
                exit = shrinkVertically(animationSpec = tween(durationMillis = 200)) +
                        fadeOut(animationSpec = tween(durationMillis = 200))
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.suggestionPrompts) { prompt ->
                        SuggestionChip(
                            text = prompt,
                            onClick = {
                                viewModel.sendMessage(text = prompt, snackbarManager = snackbarManager)
                            }
                        )
                    }
                }
            }

            MessageInput(
                text = uiState.recognizedText,
                onTextChanged = viewModel::onRecognizedTextChanged,
                onMessageSent = { text ->
                    viewModel.sendMessage(text = text, snackbarManager = snackbarManager)
                },
                enabled = !uiState.isAiTyping,
                isListening = uiState.isListening,
                isUploading = uiState.isUploadingImage,
                selectedImageUri = uiState.selectedImageUri,
                onClearSelectedImage = viewModel::clearSelectedImage,
                onMicPress = {
                    if (uiState.hasRecordPermission) {
                        viewModel.startListening()
                    } else {
                        recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onMicRelease = viewModel::stopListening,
                onImagePickerClick = {
                    imagePickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
        }
    }
}
