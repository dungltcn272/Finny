package com.ltcn272.finny.presentation.features.chat.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R

@Composable
fun MessageInput(
    text: String,
    onTextChanged: (String) -> Unit,
    onMessageSent: (String) -> Unit,
    enabled: Boolean,
    isListening: Boolean,
    onMicPress: () -> Unit,
    onMicRelease: () -> Unit,
) {
    val showSendButton = text.isNotBlank()
    val interactionSource = remember { MutableInteractionSource() }
    val isMicPressed by interactionSource.collectIsPressedAsState()
    LaunchedEffect(interactionSource) {
        var wasPressed = false
        snapshotFlow { isMicPressed }
            .collect { pressed ->
                if (pressed && !wasPressed) {
                    onMicPress()
                    wasPressed = true
                } else if (!pressed && wasPressed) {
                    onMicRelease()
                    wasPressed = false
                }
            }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChanged,
                placeholder = { Text(stringResource(if (isListening) R.string.listening else R.string.ask_anything)) },
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp),
                shape = RoundedCornerShape(30.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                ),
                enabled = enabled
            )

            if (showSendButton) {
                IconButton(
                    onClick = {
                        if (text.isNotBlank()) {
                            onMessageSent(text)
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary),
                    enabled = enabled
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = stringResource(R.string.send),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            // ------------------------------------

            AnimatedVisibility(visible = !showSendButton, enter = fadeIn(), exit = fadeOut()) {
                val micScale by animateFloatAsState(targetValue = if (isMicPressed) 1.2f else 1.0f, label = "mic_scale")
                IconButton(
                    onClick = { /* Bỏ trống vì đã xử lý bằng interactionSource */ },
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .size(48.dp)
                        .scale(micScale)
                        .background(
                            if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary,
                            CircleShape
                        ),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.onSecondary),
                    enabled = enabled
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice input",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
