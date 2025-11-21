package com.ltcn272.finny.presentation.features.intro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.ltcn272.finny.presentation.theme.IntroBackgroundBrushWithOpacity
import com.ltcn272.finny.ui.feature.on_boarding.component.DotIndicatorWithFade
import com.ltcn272.finny.presentation.features.intro.component.IntroSlideOne
import com.ltcn272.finny.presentation.features.intro.component.IntroPage
import com.ltcn272.finny.presentation.features.intro.component.IntroSlideTwo
import kotlinx.coroutines.delay

@Composable
fun IntroScreen(
    onBoardSeen: () -> Unit,
    onGetStartedClick: () -> Unit
) {
    onBoardSeen()
    val pagerState = rememberPagerState(pageCount = { 2 })
    var showGuide by remember { mutableStateOf(false) }
    var lastInteraction by remember { mutableLongStateOf(System.currentTimeMillis()) }

    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("swipe_left.json"))
    val lottieAnimState = animateLottieCompositionAsState(
        composition, isPlaying = showGuide, iterations = LottieConstants.IterateForever
    )

    // Inactivity timer
    LaunchedEffect(lastInteraction) {
        showGuide = false
        delay(2500)
        if (System.currentTimeMillis() - lastInteraction >= 2500) {
            showGuide = true
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IntroBackgroundBrushWithOpacity)
            .pointerInput(Unit) {
                while (true) {
                    awaitPointerEventScope {
                        awaitPointerEvent()
                        lastInteraction = System.currentTimeMillis()
                        showGuide = false
                    }
                }
            }) {
        HorizontalPager(state = pagerState) { page ->
            val pageOffset =
                ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).coerceIn(
                    -1f,
                    1f
                )
            val scale = 0.85f + (1 - kotlin.math.abs(pageOffset)) * 0.15f
            val alpha = 0.5f + (1 - kotlin.math.abs(pageOffset)) * 0.5f

            IntroPage(scale = scale, alpha = alpha) {
                when (page) {
                    0 -> IntroSlideOne()
                    1 -> IntroSlideTwo(onGetStarted = onGetStartedClick)
                }
            }
        }

        val showIndicator =
            pagerState.currentPage + pagerState.currentPageOffsetFraction < 0.5f
        if (showIndicator) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
            ) {
                DotIndicatorWithFade(pagerState = pagerState, pageCount = 2)
            }
        }

        // Overlay Lottie guide (Intro 1)
        val isOnIntro1 =
            pagerState.currentPage == 0 && pagerState.currentPageOffsetFraction < 0.5f
        if (showGuide && isOnIntro1) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = composition,
                    progress = { lottieAnimState.progress },
                    modifier = Modifier.size(180.dp)
                )
            }
        }
    }
}