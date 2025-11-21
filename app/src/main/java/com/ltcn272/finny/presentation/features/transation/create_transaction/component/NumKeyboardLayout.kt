package com.ltcn272.finny.presentation.features.transation.create_transaction.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R

@Composable
fun NumKeyboardLayout(
    onKeyClick: (String) -> Unit,
    onBackspace: () -> Unit
) {
    val keys = listOf(
        "1", "2", "3",
        "4", "5", "6",
        "7", "8", "9",
        ",", "0"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            keys.subList(0, 3).forEach { key ->
                NumKeyboardKey(
                    text = key,
                    onClick = { onKeyClick(key) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            keys.subList(3, 6).forEach { key ->
                NumKeyboardKey(
                    text = key,
                    onClick = { onKeyClick(key) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            keys.subList(6, 9).forEach { key ->
                NumKeyboardKey(
                    text = key,
                    onClick = { onKeyClick(key) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            keys.subList(9, 11).forEach { key ->
                NumKeyboardKey(
                    text = key,
                    onClick = { onKeyClick(key) },
                    modifier = Modifier.weight(1f)
                )
            }

            NumKeyboardKey(
                icon = R.drawable.ic_backspace,
                onClick = onBackspace,
                modifier = Modifier.weight(1f),
                iconTint = Color.White,
                backgroundColor = Color.DarkGray
            )
        }

    }
}

@Composable
fun NumKeyboardKey(
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: Int? = null,
    onClick: () -> Unit,
    iconTint: Color = Color.Black,
    backgroundColor: Color = Color(0xFFE0E0E0)
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = backgroundColor,
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxHeight(),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            text?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.Black
                )
            }
            icon?.let {
                Icon(
                    painter = painterResource(it),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

}