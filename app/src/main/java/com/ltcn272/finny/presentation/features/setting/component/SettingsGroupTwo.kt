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
fun SettingsGroupTwo() {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
    ) {
        SettingRow(label = stringResource(R.string.categories), value = "")
        HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
        SettingRow(label = stringResource(R.string.planning), value = stringResource(R.string.pro))
        HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
        SettingRow(label = stringResource(R.string.export_data), value = "")
        HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
        SettingRow(label = stringResource(R.string.ask_for_feature), value = "")
        HorizontalDivider(modifier = Modifier.padding(start = 16.dp))
        SettingRow(label = stringResource(R.string.review_the_app), value = "")
    }
}