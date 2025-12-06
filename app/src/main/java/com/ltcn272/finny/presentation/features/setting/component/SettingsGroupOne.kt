package com.ltcn272.finny.presentation.features.setting.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R

@Composable
fun SettingsGroupOne() {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
    ) {
        SettingRow(label = stringResource(R.string.country), value = "")
        HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
        SettingRow(label = stringResource(R.string.currency), value = "VNĐ")
        HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
        SettingRow(label = stringResource(R.string.language), value = "Vietnamese")
        HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
        SettingRow(label = stringResource(R.string.appearance), value = stringResource(id = R.string.system))
    }
}