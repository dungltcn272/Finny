package com.ltcn272.finny.presentation.features.transation.create_transaction.component

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R

@Preview
@Composable
fun NumKeyboardLayoutPreview() {
    NumKeyboardLayout(
        onKeyClick = {},
        onBackspace = {},
        onClear = {},
        onDone = {}
    )
}


@Composable
fun NumKeyboardLayout(
    onKeyClick: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit, // Bổ sung OnClear
    onDone: () -> Unit // Bổ sung OnDone cho nút "Done"
) {
    val keys = listOf(
        "1", "2", "3", // Hàng 1
        "4", "5", "6", // Hàng 2
        "7", "8", "9", // Hàng 3
             "0"       // Hàng 4
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {

        // --- Hàng 1: 1, 2, 3, "000" ---
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
            // Nút "000"
            NumKeyboardKey(
                icon = R.drawable.ic_close, // Sử dụng ic_close làm icon clear
                onClick = onClear,
                modifier = Modifier.weight(1f),
                iconTint = Color.DarkGray,
                backgroundColor = Color(0xFFF0F0F0) // Màu sáng hơn
            )
        }

        // --- Hàng 2: 4, 5, 6, "Clear (icon)" ---
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
            // Nút Clear
            NumKeyboardKey(
                icon = R.drawable.ic_backspace,
                onClick = onBackspace,
                modifier = Modifier.weight(1f),
                iconTint = Color.White,
                backgroundColor = MaterialTheme.colorScheme.primary // Màu chủ đạo/nổi bật
            )
        }

        // --- Hàng 3: 7, 8, 9, "Xóa (Backspace)" ---
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
            // Nút Backspace (Xóa)
            NumKeyboardKey(
                text = ".",
                onClick = { onKeyClick(".") },
                modifier = Modifier.weight(1f)
            )
        }

        // --- Hàng 4: ",", "0", "Done" (chiếm 2 phần) ---
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Nút ","
            NumKeyboardKey(
                text = "",
                onClick = { onKeyClick(keys[9]) },
                modifier = Modifier.weight(1f),
                backgroundColor = Color.Gray
            )
            // Nút "0"
            NumKeyboardKey(
                text = keys[9],
                onClick = { onKeyClick(keys[9]) },
                modifier = Modifier.weight(1f)
            )
            NumKeyboardKey(
                text = "",
                onClick = { onKeyClick(keys[9]) },
                modifier = Modifier.weight(1f),
                backgroundColor = Color.Gray
            )
            // Nút Done (chiếm 2 phần)
            NumKeyboardKey(
                icon = R.drawable.ic_check,
                onClick = onDone,
                modifier = Modifier.weight(1f),
                iconTint = Color.White,
                backgroundColor = Color.Green,
                iconSize = 32.dp
            )
        }
    }
}

// --- NumKeyboardKey (Cập nhật để hỗ trợ Text Style/Color) ---

@Composable
fun NumKeyboardKey(
    modifier: Modifier = Modifier,
    text: String? = null,
    icon: Int? = null,
    onClick: () -> Unit,
    iconTint: Color = Color.Black,
    backgroundColor: Color = Color(0xFFE0E0E0),
    textColor: Color = Color.Black,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineLarge,
    iconSize : Dp = 24.dp
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
                    style = textStyle,
                    color = textColor
                )
            }
            icon?.let {
                Icon(
                    painter = painterResource(it),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}