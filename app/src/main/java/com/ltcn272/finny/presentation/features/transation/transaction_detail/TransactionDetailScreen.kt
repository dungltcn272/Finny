//package com.ltcn272.finny.presentation.features.transation.transaction_detail
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.WindowInsets
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.safeDrawing
//import androidx.compose.foundation.layout.windowInsetsPadding
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.AlertDialog
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.Card
//import androidx.compose.material3.CardDefaults
//import androidx.compose.material3.CircularProgressIndicator
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.DpOffset
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.window.Dialog
//import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
//import coil.compose.AsyncImage
//import com.ltcn272.finny.R
//import com.ltcn272.finny.domain.model.TransactionType
//import com.ltcn272.finny.presentation.common.ui.AnimatedMoreMenu
//import com.ltcn272.finny.presentation.common.ui.CircleIconButton
//import com.ltcn272.finny.presentation.features.transation.transaction_detail.component.DetailRow
//import com.ltcn272.finny.presentation.features.transation.transaction_detail.component.StaticMap
//import com.ltcn272.finny.presentation.theme.MainBackgroundBrush
//import java.text.NumberFormat
//import java.time.format.DateTimeFormatter
//import java.util.Locale
//
//private val OutcomeColor = Color(0xFFD0021B)
//private val IncomeColor = Color(0xFF28B583)
//private val CategoryColor = Color(0xFF4A90E2)
//private val DateColor = Color(0xFFF5A623)
//private val TimeColor = Color(0xFF9013FE)
//
//@Composable
//fun TransactionDetailScreen(
//    viewModel: TransactionDetailViewModel = hiltViewModel(),
//    onBack: () -> Unit,
//    onEditClick: (String) -> Unit
//) {
//    val uiState by viewModel.uiState.collectAsState()
//    var showMenu by remember { mutableStateOf(false) }
//
//    LaunchedEffect(viewModel) {
//        viewModel.navigateBack.collect {
//            onBack()
//        }
//    }
//
//    if (uiState.showDeleteConfirmation) {
//        AlertDialog(
//            onDismissRequest = { viewModel.onDismissDeleteDialog() },
//            title = { Text(text = "Delete Confirmation") },
//            text = { Text("Are you sure you want to delete this transaction?") },
//            confirmButton = {
//                Button(
//                    onClick = { viewModel.onDeleteTransaction() },
//                    shape = RoundedCornerShape(8.dp),
//                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
//                ) {
//                    Text("Delete")
//                }
//            },
//            dismissButton = {
//                Button(
//                    onClick = { viewModel.onDismissDeleteDialog() },
//                    shape = RoundedCornerShape(8.dp),
//                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
//                ) {
//                    Text("Cancel", color = Color.Black)
//                }
//            },
//            shape = RoundedCornerShape(16.dp)
//        )
//    }
//
//    if (uiState.isDeleting) {
//        Dialog(onDismissRequest = {}) {
//            Box(
//                modifier = Modifier
//                    .width(100.dp)
//                    .height(100.dp)
//                    .background(Color.White, shape = RoundedCornerShape(8.dp)),
//                contentAlignment = Alignment.Center
//            ) {
//                CircularProgressIndicator()
//            }
//        }
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(MainBackgroundBrush)
//    ) {
//        if (uiState.isLoading) {
//            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
//        } else if (uiState.error != null) {
//            Text(
//                text = uiState.error!!,
//                modifier = Modifier.align(Alignment.Center),
//                color = MaterialTheme.colorScheme.error
//            )
//        } else {
//            val transaction = uiState.transaction ?: return
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .windowInsetsPadding(WindowInsets.safeDrawing)
//                    .padding(vertical = 8.dp)
//                    .verticalScroll(rememberScrollState())
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    CircleIconButton(onClick = onBack, icon = R.drawable.ic_left)
//                    Box {
//                        CircleIconButton(
//                            onClick = { showMenu = true },
//                            icon = R.drawable.ic_menu
//                        )
//                        AnimatedMoreMenu(
//                            expanded = showMenu,
//                            onDismissRequest = { showMenu = false },
//                            onEditClick = { onEditClick(transaction.id) },
//                            onDeleteClick = viewModel::onDeleteConfirmation,
//                            offset = DpOffset(x = 0.dp, y = 38.dp)
//                        )
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                Card(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 16.dp),
//                    shape = RoundedCornerShape(24.dp),
//                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.6f)),
//                    elevation = CardDefaults.cardElevation(0.dp)
//                ) {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(16.dp),
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ) {
//                        Text(
//                            text = transaction.name,
//                            style = MaterialTheme.typography.headlineSmall,
//                            fontWeight = FontWeight.Bold,
//                            color = Color.Black
//                        )
//                        Spacer(modifier = Modifier.height(4.dp))
//                        Text(
//                            text = transaction.dateTime.format(DateTimeFormatter.ofPattern("E, MMM dd")),
//                            style = MaterialTheme.typography.bodyMedium,
//                            color = Color.Gray
//                        )
//                        Spacer(modifier = Modifier.height(8.dp))
//                        Text(
//                            text = "${if (transaction.type == TransactionType.INCOME) "+" else "-"}${NumberFormat.getCurrencyInstance(
//                                    Locale("vi", "VN")
//                                ).format(transaction.amount)}",
//                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
//                            color = if (transaction.type == TransactionType.INCOME) IncomeColor else OutcomeColor
//                        )
//                        Spacer(modifier = Modifier.height(4.dp))
//                        transaction.description?.takeIf { it.isNotBlank() }?.let {
//                            Text(
//                                text = "\"$it\"",
//                                style = MaterialTheme.typography.bodyMedium,
//                                color = Color.Gray
//                            )
//                        }
//                    }
//                }
//
//
//                Spacer(modifier = Modifier.height(24.dp))
//
//                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
//                    Text(
//                        text = "Transaction Details",
//                        style = MaterialTheme.typography.titleLarge,
//                        fontWeight = FontWeight.Bold,
//                        color = Color.Black
//                    )
//                    Spacer(modifier = Modifier.height(16.dp))
//                    Surface(
//                        modifier = Modifier
//                            .fillMaxWidth(),
//                        color = Color.White,
//                        shape = RoundedCornerShape(16.dp)
//                    ) {
//                        Column(
//                            modifier = Modifier.padding(16.dp),
//                            verticalArrangement = Arrangement.spacedBy(16.dp)
//                        ) {
//
//                            DetailRow(
//                                icon = R.drawable.ic_tag,
//                                label = "Category",
//                                value = transaction.category.name,
//                                iconTint = CategoryColor,
//                                iconBackgroundColor = CategoryColor.copy(alpha = 0.2f)
//                            )
//                            val typeColor = if (transaction.type == TransactionType.INCOME) IncomeColor else OutcomeColor
//                            DetailRow(
//                                icon = if (transaction.type == TransactionType.INCOME) R.drawable.ic_trending_up else R.drawable.ic_trending_down,
//                                label = "Type",
//                                value = transaction.type.name.lowercase()
//                                    .replaceFirstChar { it.titlecase() },
//                                iconTint = typeColor,
//                                iconBackgroundColor = typeColor.copy(alpha = 0.2f)
//                            )
//                            DetailRow(
//                                icon = R.drawable.ic_calendar,
//                                label = "Date",
//                                value = transaction.dateTime.format(DateTimeFormatter.ofPattern("E, MMM dd, yyyy")),
//                                iconTint = DateColor,
//                                iconBackgroundColor = DateColor.copy(alpha = 0.2f)
//                            )
//                            DetailRow(
//                                icon = R.drawable.ic_clock,
//                                label = "Created at",
//                                value = transaction.dateTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME),
//                                iconTint = TimeColor,
//                                iconBackgroundColor = TimeColor.copy(alpha = 0.2f)
//                            )
//                        }
//                    }
//
//                    transaction.localImagePath?.takeIf { it.isNotBlank() }?.let { imageUrl ->
//                        Spacer(modifier = Modifier.height(24.dp))
//                        Text(
//                            text = "Photo",
//                            style = MaterialTheme.typography.titleLarge,
//                            fontWeight = FontWeight.Bold,
//                            color = Color.Black
//                        )
//                        Spacer(modifier = Modifier.height(16.dp))
//                        AsyncImage(
//                            model = imageUrl,
//                            contentDescription = "Transaction Image",
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(200.dp)
//                                .clip(RoundedCornerShape(16.dp)),
//                            contentScale = ContentScale.Crop
//                        )
//                    }
//
//                    transaction.location?.let { location ->
//                        Spacer(modifier = Modifier.height(24.dp))
//                        Text(
//                            text = "Location",
//                            style = MaterialTheme.typography.titleLarge,
//                            fontWeight = FontWeight.Bold,
//                            color = Color.Black
//                        )
//                        Spacer(modifier = Modifier.height(16.dp))
//                        StaticMap(lat = location.lat, lng = location.lng)
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//            }
//        }
//    }
//}