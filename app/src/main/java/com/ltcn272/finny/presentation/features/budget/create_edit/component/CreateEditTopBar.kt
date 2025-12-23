package com.ltcn272.finny.presentation.features.budget.create_edit.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.presentation.common.ui.CircleNavigationButton

@Composable
fun CreateEditTopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        CircleNavigationButton(
            icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            onClick = onBack
        )
    }
}
