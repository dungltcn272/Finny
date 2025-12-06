package com.ltcn272.finny.presentation.features.setting

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ltcn272.finny.R
import com.ltcn272.finny.presentation.common.ui.CircleIconButton
import com.ltcn272.finny.presentation.features.setting.component.LogoutButton
import com.ltcn272.finny.presentation.features.setting.component.PremiumCard
import com.ltcn272.finny.presentation.features.setting.component.SettingsGroupOne
import com.ltcn272.finny.presentation.features.setting.component.SettingsGroupThree
import com.ltcn272.finny.presentation.features.setting.component.SettingsGroupTwo

@Composable
fun SettingScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F7))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            CircleIconButton(onClick = {}, icon = R.drawable.ic_left)
            Text(
                text = stringResource(R.string.settings_title),
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 10.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            PremiumCard()
            Spacer(modifier = Modifier.height(16.dp))
            SettingsGroupOne()
            Text(
                text = stringResource(R.string.app_preferences),
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            SettingsGroupThree()
            Spacer(modifier = Modifier.height(16.dp))
            SettingsGroupTwo()
            Spacer(modifier = Modifier.height(16.dp))
            LogoutButton()
        }
    }
}