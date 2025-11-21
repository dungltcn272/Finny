package com.ltcn272.finny.presentation.features.home.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ltcn272.finny.R
import com.ltcn272.finny.presentation.features.home.TimeFilter
import java.text.NumberFormat

@Composable
fun BalanceCard(
    balance: Double,
    income: Double,
    expense: Double,
    currencyCode: String,
    selectedTimeFilter: TimeFilter,
    onCycleFilter: () -> Unit,
) {
    val formatter: (Double) -> String = { amount ->
        val format = NumberFormat.getCurrencyInstance()
        try {
            format.currency = java.util.Currency.getInstance(currencyCode)
        } catch (_: Exception) {
        }
        format.format(amount)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF2A2A37),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Total Balance",
                    style = MaterialTheme.typography.titleMedium, // Increased font size
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    onClick = onCycleFilter,
                    tonalElevation = 4.dp
                ) {
                    AnimatedContent(
                        targetState = selectedTimeFilter.displayName,
                        transitionSpec = {
                             (slideInVertically(animationSpec = tween(300)) { height -> height } + fadeIn(animationSpec = tween(300)))
                                 .togetherWith(slideOutVertically(animationSpec = tween(300)) { height -> -height } + fadeOut(animationSpec = tween(300)))
                                 .using(SizeTransform(clip = false))
                        }, label = "FilterTextAnimation"
                    ) { targetText ->
                        Text(
                            text = targetText,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
            AnimatedNumberText(value = balance, formatter = formatter)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_trending_up),
                        contentDescription = "Income",
                        tint = Color(0xFF28E0B4)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            "Income",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        AnimatedNumberText(
                            value = income,
                            formatter = formatter,
                            color = Color(0xFF28E0B4),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                HorizontalDivider(
                    modifier = Modifier
                        .height(30.dp)
                        .width(1.dp),
                    color = Color.Gray.copy(alpha = 0.5f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_trending_down),
                        contentDescription = "Expense",
                        tint = Color(0xFFF95B5B)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            "Expense",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                        AnimatedNumberText(
                            value = expense,
                            formatter = formatter,
                            color = Color(0xFFF95B5B),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedNumberText(
    value: Double,
    formatter: (Double) -> String,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    fontSize: androidx.compose.ui.unit.TextUnit = 36.sp,
    fontWeight: FontWeight = FontWeight.ExtraBold
) {
    var oldValue by remember { mutableDoubleStateOf(value) }
    SideEffect {
        oldValue = value
    }

    val formattedValue = formatter(value)

    Row(modifier = modifier) {
        formattedValue.forEach { char ->
            AnimatedContent(
                targetState = char,
                transitionSpec = {
                     if (char.isDigit()) {
                        (slideInVertically(animationSpec = tween(400)) { it } + fadeIn(animationSpec = tween(400)))
                            .togetherWith(slideOutVertically(animationSpec = tween(400)) { -it } + fadeOut(animationSpec = tween(400)))
                    } else {
                        fadeIn(animationSpec = tween(200, 200)) togetherWith fadeOut(animationSpec = tween(200))
                    }.using(SizeTransform(clip = false))
                }, label = "char_anim"
            ) { animatedChar ->
                Text(
                    text = animatedChar.toString(),
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    color = color,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}