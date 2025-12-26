package com.ltcn272.finny.presentation.features.category.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.ltcn272.finny.domain.model.Category

@Composable
fun CategoryListSection(
    modifier: Modifier = Modifier,
    categories: LazyPagingItems<Category>,
    isEditMode: Boolean,
    onDeleteClick: (Category) -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 2.dp
    ) {
        Column {
            if (categories.loadState.refresh is LoadState.Loading) {
                repeat(3) { index ->
                    CategoryItemShimmer()
                    if (index < 2) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    }
                }
            } else {
                val itemCount = categories.itemCount
                for (index in 0 until itemCount) {
                    val category = categories[index]
                    if (category != null) {
                        CategoryItem(
                            category = category,
                            isEditMode = isEditMode,
                            onDeleteClick = { onDeleteClick(category) }
                        )
                        if (index < itemCount - 1) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                            )
                        }
                    }
                }
            }
        }
    }
}
