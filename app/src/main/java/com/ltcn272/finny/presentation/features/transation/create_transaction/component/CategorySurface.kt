package com.ltcn272.finny.presentation.features.transation.create_transaction.component

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ltcn272.finny.domain.model.TransactionCategory
import com.ltcn272.finny.presentation.common.util.CategoryStyle

@Preview(showBackground = true)
@Composable
fun TypeSurfacePreview() {
    CategorySurface(
        category = TransactionCategory.TRANSPORTATION,
        onClick = {},
        modifier = Modifier.width(150.dp)
    )
}

@Composable
fun CategorySurface(
    category: TransactionCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val style = CategoryStyle.getStyle(category)
    val surfaceColor = style.color
    val textColor = Color.White

    Surface(
        onClick = { onClick() },
        color = surfaceColor,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp,
        modifier = modifier
            .height(48.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = style.emoji +" "+ category.name.lowercase().replaceFirstChar { it.titlecase() },
                style = MaterialTheme.typography.bodyLarge.copy(color = textColor),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                modifier = Modifier.basicMarquee()
            )
        }
    }
}
