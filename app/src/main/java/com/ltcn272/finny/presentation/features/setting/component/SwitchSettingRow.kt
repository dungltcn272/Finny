package com.ltcn272.finny.presentation.features.setting.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.muazkadan.switchycompose.ISwitch

@Composable
fun SwitchSettingRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 16.sp)
        Spacer(modifier = Modifier.weight(1f))
        ISwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            buttonHeight = 30.dp,
            positiveColor = Color(0xFF35C759),
            negativeColor = Color(0xFFE9E9EA),
        )
    }
}