package com.ltcn272.finny.presentation.features.setting

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ltcn272.finny.R
import com.ltcn272.finny.presentation.common.ui.AnimatedCurrencyMenu
import com.ltcn272.finny.presentation.common.ui.FinnySnackbar
import com.ltcn272.finny.presentation.features.setting.component.SettingItem
import com.ltcn272.finny.presentation.features.setting.component.SettingSwitchItem
import com.ltcn272.finny.presentation.theme.MainBackgroundBrush

@Composable
fun SettingScreen(
    viewModel: SettingViewModel = hiltViewModel(),
    onNavigateToProfile: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onLoggedOut: () -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val uiState by viewModel.uiState.collectAsState()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = viewModel::onNotificationPermissionResult
    )

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.syncNotificationStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var showCurrencyMenu by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.logoutEvent.collect { onLoggedOut() }
    }

    LaunchedEffect(Unit) {
        viewModel.notificationEvent.collect { event ->
            when (event) {
                is SettingNotificationEvent.RequestPermission -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        viewModel.setEnableNotifications(true)
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MainBackgroundBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            Text(
                text = stringResource(id = R.string.settings),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    SettingsCard {
                        SettingItem(
                            // Sử dụng state từ uiState
                            text = uiState.username ?: stringResource(id = R.string.user_name_placeholder),
                            onClick = onNavigateToProfile
                        )
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                        Box {
                            SettingItem(
                                text = stringResource(id = R.string.currency),
                                // Sử dụng state từ uiState
                                value = uiState.selectedCurrency,
                                onClick = { showCurrencyMenu = true }
                            )
                            AnimatedCurrencyMenu(
                                expanded = showCurrencyMenu,
                                onDismissRequest = { showCurrencyMenu = false },
                                currencies = listOf("VND", "USD"),
                                // Sử dụng state từ uiState
                                selectedCurrency = uiState.selectedCurrency,
                                onCurrencyClick = {
                                    viewModel.onCurrencySelected(it)
                                    showCurrencyMenu = false
                                },
                                offset = DpOffset(x = (-16).dp, y = 0.dp)
                            )
                        }
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                        SettingItem(
                            text = stringResource(id = R.string.category_management),
                            onClick = onNavigateToCategories
                        )
                    }
                }

                item {
                    val context = LocalContext.current
                    SettingsCard {
                        SettingSwitchItem(
                            text = stringResource(R.string.enable_notifications),
                            // Sử dụng state từ uiState
                            checked = uiState.actualNotificationStatus,
                            onCheckedChange = { viewModel.onEnableNotificationsToggled() }
                        )
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                        SettingItem(
                            text = stringResource(R.string.export_data),
                            onClick = { Toast.makeText(context, context.getString(R.string.feature_in_development), Toast.LENGTH_SHORT).show() }
                        )
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                        SettingItem(
                            text = stringResource(R.string.suggest_feature),
                            onClick = { Toast.makeText(context, context.getString(R.string.feature_in_development), Toast.LENGTH_SHORT).show() }
                        )
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                        SettingItem(
                            text = stringResource(R.string.rate_app),
                            onClick = { Toast.makeText(context, context.getString(R.string.feature_in_development), Toast.LENGTH_SHORT).show() }
                        )
                    }
                }

                item {
                    Button(
                        onClick = viewModel::logout,
                        // Sử dụng state từ uiState
                        enabled = !uiState.isLoggingOut,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        // Sử dụng state từ uiState
                        if (uiState.isLoggingOut) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.error, strokeWidth = 2.dp)
                        } else {
                            Text(text = stringResource(id = R.string.logout), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        FinnySnackbar(
            // Sử dụng state từ uiState
            visible = uiState.snackbarState.visible,
            message = uiState.snackbarState.message,
            type = uiState.snackbarState.type,
            action = uiState.snackbarState.actionTitle?.let { title ->
                {
                    TextButton(
                        onClick = {
                            uiState.snackbarState.onActionClick?.invoke()
                            viewModel.onSnackbarDismissed()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                    ) {
                        Text(title, fontWeight = FontWeight.Bold)
                    }
                }
            },
            onDismiss = viewModel::onSnackbarDismissed
        )
    }
}

@Composable
private fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            content()
        }
    }
}
