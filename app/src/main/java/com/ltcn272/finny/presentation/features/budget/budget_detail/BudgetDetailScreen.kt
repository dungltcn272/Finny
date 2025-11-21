package com.ltcn272.finny.presentation.features.budget.budget_detail

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ltcn272.finny.R
import com.ltcn272.finny.presentation.common.ui.AnimatedMoreMenu
import com.ltcn272.finny.presentation.common.ui.CircleIconButton
import com.ltcn272.finny.presentation.common.ui.SliderFilterRow
import com.ltcn272.finny.presentation.features.transation.transaction_list.component.DaySectionItem
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.GridProperties.AxisProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.PopupProperties
import java.util.Locale

@Composable
fun BudgetDetailScreen(
    viewModel: BudgetDetailViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onTransactionClick: (String) -> Unit = {},
    onEditClick: () -> Unit = {},
    onAddTransactionClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val groupedTransactions by viewModel.groupedTransactions.collectAsState()
    val chartUiState by viewModel.chartUiState.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.navigateBack.collect {
            onBack()
        }
    }

    if (uiState.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.onDismissDeleteDialog() },
            title = { Text(text = "Xác nhận xóa") },
            text = { Text("Bạn chắc chắn muốn xóa Budget này chứ, tất cả các Transaction liên quan sẽ được xóa sổ theo") },
            confirmButton = {
                Button(
                    onClick = { viewModel.onDeleteBudget() },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(
                    onClick = { viewModel.onDismissDeleteDialog() },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("Cancel", color = Color.Black)
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (uiState.isDeleting) {
        Dialog(onDismissRequest = {}) {
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp)
                    .background(Color.White, shape = RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF6F6))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleIconButton(
                onClick = onBack,
                icon = R.drawable.ic_left,
            )
            Spacer(Modifier.weight(1f))

            Box {
                CircleIconButton(
                    onClick = { showMenu = true },
                    icon = R.drawable.ic_menu,
                )

                // Thay thế bằng AnimatedMoreMenu tùy chỉnh
                // Điều chỉnh offset để menu hiển thị ngay dưới nút
                AnimatedMoreMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    onEditClick = {
                        onEditClick()
                        showMenu = false
                    },
                    onDeleteClick = {
                        viewModel.onDeleteConfirmation()
                        showMenu = false
                    },
                    offset = DpOffset(x = 0.dp, y = 38.dp)
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Column {
            Text(
                text = uiState.budget?.name.orEmpty(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${uiState.transactions.size} Items",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.filterPeriods.isNotEmpty()) {
            SliderFilterRow(
                items = uiState.filterPeriods.filter { it !is FilterPeriod.All },
                selectedItem = if (selectedFilter is FilterPeriod.All) null else selectedFilter,
                onItemSelected = viewModel::setFilter,
                itemToString = { it.getDisplayName(Locale.getDefault()) }
            )
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                if (chartUiState.data.isNotEmpty()) {
                    ColumnChart(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(vertical = 8.dp),
                        data = chartUiState.data,
                        maxValue = chartUiState.maxValue,
                        barProperties = BarProperties(
                            cornerRadius = Bars.Data.Radius.Rectangle(
                                topRight = 6.dp,
                                topLeft = 6.dp
                            ),
                            spacing = 0.dp,
                            thickness = 20.dp
                        ),
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        indicatorProperties = HorizontalIndicatorProperties(
                            contentBuilder = { value ->
                                viewModel.formatCurrency(
                                    value,
                                    Locale.getDefault()
                                )
                            }
                        ),
                        popupProperties = PopupProperties(
                            containerColor = Color.Magenta,
                            contentBuilder = { value ->
                                viewModel.formatCurrency(value.value, Locale.getDefault())
                            }),
                        gridProperties = GridProperties(
                            true,
                            AxisProperties(lineCount = 2),
                            AxisProperties(lineCount = 2)
                        )
                    )
                }
            }

            items(items = groupedTransactions, key = { it.date }) { daySection ->
                DaySectionItem(
                    daySection = daySection,
                    currency = uiState.currency,
                    onCreateNewClick = onAddTransactionClick,
                    onTransactionClick = onTransactionClick
                )
            }
        }
    }
}
