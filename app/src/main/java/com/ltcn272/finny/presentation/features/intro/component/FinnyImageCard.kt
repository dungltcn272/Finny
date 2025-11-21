package com.ltcn272.finny.presentation.features.intro.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ImageCard(
    image: Painter,
    modifier: Modifier = Modifier,
    imageShape: RoundedCornerShape = RoundedCornerShape(28.dp),
    cardModifier: Modifier = Modifier,
    cardShape: RoundedCornerShape = RoundedCornerShape(28.dp),
    cardColor: Color = Color.White,
    cardElevation: Dp = 16.dp,
    valueText: String? = null,
    descriptionText: String? = null,
    valueStyle: TextStyle = TextStyle(
        fontWeight = FontWeight.Black,
        fontSize = 24.sp,
        color = Color.Black
    ),
    descriptionStyle: TextStyle = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        color = Color.Black
    ),
    icon: Painter? = null,
    iconTint: Color = Color.White,
    iconBg: Color = Color(0xFF444444),
    iconSize: Dp = 45.dp,
    cardAlign: Alignment = Alignment.TopStart,
    cardWidthFraction: Float = 0.56f,
    cardHeightFraction: Float = 1f
) {
    Box(modifier = modifier) {
        Image(
            painter = image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(imageShape)
        )
        Card(
            modifier = cardModifier
                .align(cardAlign)
                .fillMaxWidth(cardWidthFraction)
                .fillMaxHeight(cardHeightFraction),
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(cardElevation)
        ) {
            Box(Modifier.fillMaxSize()) {
                if (valueText != null && descriptionText != null) {
                    Column(
                        modifier = Modifier.padding(
                            start = 20.dp,
                            top = 20.dp,
                            end = iconSize + 20.dp,
                            bottom = 20.dp
                        )
                    ) {
                        Text(
                            valueText,
                            style = valueStyle
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            descriptionText,
                            style = descriptionStyle
                        )
                    }
                }
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(
                                end = 20.dp,
                                bottom = 20.dp
                            )
                            .size(iconSize)
                            .background(iconBg, shape = CircleShape)
                            .clip(CircleShape)
                            .shadow(
                                elevation = 20.dp,
                                shape = RoundedCornerShape(28.dp),
                                ambientColor = Color.Black.copy(alpha = 0.25f),
                                spotColor = Color.Black.copy(alpha = 0.25f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = icon,
                            contentDescription = null,
                            tint = iconTint
                        )
                    }
                }
            }
        }
    }
}


