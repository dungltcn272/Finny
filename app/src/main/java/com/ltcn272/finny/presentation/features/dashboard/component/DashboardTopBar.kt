package com.ltcn272.finny.presentation.features.dashboard.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.presentation.common.ui.CircleNavigationButton

@Composable
fun DashboardTopBar(
    modifier: Modifier = Modifier,
    title: String,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onTitleClick: () -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CircleNavigationButton(
            icon = Icons.AutoMirrored.Default.KeyboardArrowLeft,
            onClick = onBack
        )

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onTitleClick
                )
                .padding(vertical = 8.dp, horizontal = 4.dp)
        )

        CircleNavigationButton(
            icon = Icons.AutoMirrored.Default.KeyboardArrowRight,
            onClick = onNext
        )
    }
}