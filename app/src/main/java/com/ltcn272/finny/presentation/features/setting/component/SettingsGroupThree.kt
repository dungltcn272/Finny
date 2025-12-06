package com.ltcn272.finny.presentation.features.setting.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R

@Composable
fun SettingsGroupThree() {
    var enableNotifications by remember { mutableStateOf(true) }
    var authentication by remember { mutableStateOf(false) }
    var icloudSync by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
    ) {
        SwitchSettingRow(
            label = stringResource(R.string.enable_notifications),
            checked = enableNotifications,
            onCheckedChange = { enableNotifications = it }
        )
        HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
        SwitchSettingRow(
            label = stringResource(R.string.authentication),
            checked = authentication,
            onCheckedChange = { authentication = it }
        )
        HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
        SwitchSettingRow(
            label = stringResource(R.string.icloud_sync),
            checked = icloudSync,
            onCheckedChange = { icloudSync = it }
        )
    }
}