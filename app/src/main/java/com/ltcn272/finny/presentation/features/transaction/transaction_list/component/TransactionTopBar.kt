package com.ltcn272.finny.presentation.features.transaction.transaction_list.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.ltcn272.finny.presentation.common.ui.CircleNavigationButton

@Composable
fun TransactionTopBar(
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

        TextButton(onClick = onTitleClick) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        CircleNavigationButton(
            icon = Icons.AutoMirrored.Default.KeyboardArrowRight,
            onClick = onNext
        )
    }
}

