package com.ltcn272.finny.presentation.features.chat.component

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R

private fun playSound(context: Context, soundId: Int, isLooping: Boolean = false): MediaPlayer? {
    return try {
        MediaPlayer.create(context, soundId).apply {
            this.isLooping = isLooping
            start()
            if (!isLooping) {
                setOnCompletionListener { it.release() }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun TypingIndicator() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val mediaPlayer = playSound(context, R.raw.typing_sound, isLooping = true)
        onDispose {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "typing_indicator")
    val dots = List(3) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, delayMillis = index * 150, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ), label = "dot_$index"
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        Surface(
            modifier = Modifier.widthIn(max = 100.dp),
            color = Color.White,
            shape = RoundedCornerShape(15.dp),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                dots.forEach { anim ->
                    Box(
                        Modifier
                            .padding(horizontal = 3.dp)
                            .size(8.dp)
                            .scale(if (anim.value < 0.5f) (anim.value * 2) else (1 - (anim.value - 0.5f) * 2))
                            .alpha(if (anim.value < 0.1f || anim.value > 0.9f) 0.5f else 1f)
                            .background(Color.Gray, CircleShape)
                    )
                }
            }
        }
    }
}
