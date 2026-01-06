package com.ltcn272.finny.presentation.features.chat.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageInput(
    text: String,
    onTextChanged: (String) -> Unit,
    onMessageSent: (String) -> Unit,
    enabled: Boolean,
    isListening: Boolean,
    isUploading: Boolean,
    onMicPress: () -> Unit,
    onMicRelease: () -> Unit,
    onImagePickerClick: () -> Unit
) {
    val micInteractionSource = remember { MutableInteractionSource() }
    val isMicPressed by micInteractionSource.collectIsPressedAsState()

    LaunchedEffect(micInteractionSource) {
        var wasPressed = false
        snapshotFlow { isMicPressed }.collect { pressed ->
                if (pressed && !wasPressed) {
                    onMicPress()
                    wasPressed = true
                } else if (!pressed && wasPressed) {
                    onMicRelease()
                    wasPressed = false
                }
            }
    }

    Surface(
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp), color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val buttonEnabled = enabled && !isUploading

            val micScale by animateFloatAsState(
                targetValue = if (isMicPressed) 1.2f else 1.0f, label = "mic_scale"
            )
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .scale(micScale),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                contentColor = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(
                            enabled = buttonEnabled,
                            onClick = { },
                            interactionSource = micInteractionSource,
                            indication = null
                        ), contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice input",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Surface(
                onClick = { if (buttonEnabled) onImagePickerClick() },
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                enabled = buttonEnabled
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = stringResource(R.string.choose_image),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 42.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    BasicTextField(
                        value = text,
                        onValueChange = onTextChanged,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enabled && !isUploading && !isListening,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        maxLines = 5,
                        decorationBox = { innerTextField ->
                            TextFieldDefaults.DecorationBox(
                                value = text,
                                innerTextField = innerTextField,
                                enabled = enabled && !isUploading && !isListening,
                                singleLine = false,
                                visualTransformation = VisualTransformation.None,
                                interactionSource = remember { MutableInteractionSource() },
                                placeholder = {
                                    Text(
                                        text = stringResource(if (isListening) R.string.listening else if (isUploading) R.string.uploading_image else R.string.ask_anything),
                                        maxLines = 1,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(
                                        alpha = 0.7f
                                    ),
                                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                        alpha = 0.5f
                                    )
                                ),
                                contentPadding = TextFieldDefaults.contentPaddingWithoutLabel(
                                    top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp
                                )
                            )
                        })
                }
            }

            val isSendEnabled = text.isNotBlank() && enabled
            val sendButtonContainerColor by animateColorAsState(
                targetValue = if (isSendEnabled) MaterialTheme.colorScheme.primary else Color.Black,
                label = "send_button_color"
            )
            val sendButtonContentColor by animateColorAsState(
                targetValue = if (isSendEnabled) MaterialTheme.colorScheme.onPrimary else Color.White,
                label = "send_content_color"
            )

            Surface(
                onClick = { if (isSendEnabled) onMessageSent(text) },
                modifier = Modifier.size(42.dp),
                shape = CircleShape,
                color = sendButtonContainerColor,
                contentColor = sendButtonContentColor,
                enabled = enabled
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = stringResource(R.string.send),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
