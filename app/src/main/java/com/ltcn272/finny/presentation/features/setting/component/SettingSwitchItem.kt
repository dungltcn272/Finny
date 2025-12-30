package com.ltcn272.finny.presentation.features.setting.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.muazkadan.switchycompose.ISwitch

@Composable
fun SettingSwitchItem(
    text: String,
    modifier: Modifier = Modifier,
    checked: Boolean? = null,
    defaultChecked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {},
    textColor: Color = Color.Black
) {
    var internalChecked by remember { mutableStateOf(defaultChecked) }
    val isChecked = checked ?: internalChecked

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, fontSize = 16.sp, color = textColor, modifier = Modifier.weight(1f))

        ISwitch(
            checked = isChecked,
            onCheckedChange = { newChecked ->
                if (checked == null) internalChecked = newChecked
                onCheckedChange(newChecked)
            },
            buttonHeight = 25.dp,
            innerPadding = 2.dp
        )
    }
}