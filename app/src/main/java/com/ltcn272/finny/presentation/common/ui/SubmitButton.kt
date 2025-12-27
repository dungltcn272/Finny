package com.ltcn272.finny.presentation.common.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R

enum class ScreenMode {
    CREATE, EDIT
}


@Composable
fun SubmitButton(
    mode: ScreenMode,
    isSubmitting: Boolean,
    onClick: () -> Unit
) {
    val (text, backgroundColor, contentColor) = if (mode == ScreenMode.CREATE) {
        Triple(stringResource(R.string.create_new), Color(0xFFDEDEDE), Color.DarkGray)
    } else {
        Triple(stringResource(R.string.update), Color.Black, Color.White)
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(56.dp)
            .clip(CircleShape)
            .background(backgroundColor.copy(alpha = if (isSubmitting) 0.7f else 1f))
            .clickable(
                enabled = !isSubmitting,
                onClick = onClick,
                interactionSource = interactionSource,
                indication = null
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = isSubmitting,
            modifier = Modifier.fillMaxWidth(),
            transitionSpec = {
                if (targetState) {
                    (fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn(initialScale = 0.92f))
                        .togetherWith(fadeOut(animationSpec = tween(90)))
                } else {
                    (fadeIn(animationSpec = tween(220, delayMillis = 90)))
                        .togetherWith(fadeOut(animationSpec = tween(90)) + scaleOut(targetScale = 0.92f))
                }
            }, label = "SubmitButtonAnimation"
        ) { submitting ->
            if (submitting) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = contentColor,
                        strokeWidth = 2.5.dp
                    )
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .background(contentColor.copy(alpha = 0.2f))
                            .align(Alignment.CenterEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = contentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
