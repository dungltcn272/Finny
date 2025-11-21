package com.ltcn272.finny.presentation.features.transation.create_transaction.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.R

@Preview
@Composable
fun BudgetSurfacePreview() {
    BudgetSurface(
        budgetName = "Filet o' Fish",
        onClick = {}
    )
}

@Composable
fun BudgetSurface(
    budgetName: String = "Filet o' Fish",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = { onClick() },
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.8f)),
        shadowElevation = 0.dp,
        modifier = modifier
            .wrapContentSize()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_menu),
                contentDescription = "Budget Menu",
                tint = Color.Gray, // Màu xám cho Icon
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))

            Text(
                text = budgetName,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black.copy(alpha = 0.8f)
            )
        }
    }
}