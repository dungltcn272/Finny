package com.ltcn272.finny.presentation.features.setting

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
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
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ltcn272.finny.R
import com.ltcn272.finny.presentation.common.ui.AnimatedCurrencyMenu
import com.ltcn272.finny.presentation.features.snackbar.LocalSnackbarManager
import com.ltcn272.finny.presentation.features.setting.component.SettingItem
import com.ltcn272.finny.presentation.features.setting.component.SettingSwitchItem
import com.ltcn272.finny.presentation.theme.MainBackgroundBrush
import com.ltcn272.finny.util.PermissionUtils
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingScreen(
    viewModel: SettingViewModel = hiltViewModel(),
    onNavigateToProfile: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onLoggedOut: () -> Unit = {}
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarManager = LocalSnackbarManager.current

    if (uiState.showNotificationAccessDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissNotificationAccessDialog,
            title = { Text(stringResource(R.string.notification_access_dialog_title)) },
            text = { Text(stringResource(R.string.notification_access_dialog_text)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onDismissNotificationAccessDialog()
                        PermissionUtils.openNotificationAccessSettings(context)
                    }
                ) {
                    Text(stringResource(R.string.go_to_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::onDismissNotificationAccessDialog) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    val saveAndOpenFile: (String) -> Unit = { base64Pdf ->
        try {
            val pdfData = Base64.decode(base64Pdf, Base64.DEFAULT)
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "Finny_Statement_$timeStamp.pdf"

            val fileUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentResolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    contentResolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write(pdfData)
                    }
                }
                uri
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!downloadsDir.exists()) downloadsDir.mkdirs()
                val pdfFile = File(downloadsDir, fileName)
                FileOutputStream(pdfFile).use { it.write(pdfData) }
                FileProvider.getUriForFile(context, "${context.packageName}.provider", pdfFile)
            }

            if (fileUri == null) {
                viewModel.showPdfExportError(snackbarManager)
            } else {
                val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(fileUri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                try {
                    context.startActivity(viewIntent)
                    viewModel.showPdfExportSuccess(snackbarManager, fileName)
                } catch (e: ActivityNotFoundException) {
                    viewModel.showNoPdfViewerFound(snackbarManager)
                }
            }
        } catch (e: Exception) {
            viewModel.showPdfExportError(snackbarManager)
            e.printStackTrace()
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> viewModel.onNotificationPermissionResult(snackbarManager, isGranted) }
    )

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.syncNotificationStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
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
                        viewModel.setEnableNotifications(snackbarManager, true)
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
                            text = uiState.username ?: stringResource(id = R.string.user_name_placeholder),
                            onClick = onNavigateToProfile
                        )
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                        Box {
                            SettingItem(
                                text = stringResource(id = R.string.currency),
                                value = uiState.selectedCurrency,
                                onClick = { showCurrencyMenu = true }
                            )
                            AnimatedCurrencyMenu(
                                expanded = showCurrencyMenu,
                                onDismissRequest = { showCurrencyMenu = false },
                                currencies = listOf("VND", "USD"),
                                selectedCurrency = uiState.selectedCurrency,
                                onCurrencyClick = {
                                    viewModel.onCurrencySelected(it)
                                    showCurrencyMenu = false
                                },
                                offset = DpOffset(x = 0.dp, y = 38.dp)
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
                    SettingsCard {
                        SettingSwitchItem(
                            text = stringResource(R.string.enable_notifications),
                            checked = uiState.actualNotificationStatus,
                            onCheckedChange = { viewModel.onEnableNotificationsToggled() }
                        )
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                        SettingSwitchItem(
                            text = stringResource(R.string.auto_read_notifications),
                            checked = uiState.hasNotificationAccess,
                            onCheckedChange = viewModel::onEnableAutoReadNotificationsToggled
                        )
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                        SettingItem(
                            text = stringResource(R.string.export_data),
                            onClick = {
                                if (!uiState.isExportingPdf) {
                                    viewModel.exportStatement(snackbarManager, onSuccess = saveAndOpenFile)
                                }
                            },
                            trailingContent = {
                                if (uiState.isExportingPdf) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                }
                            }
                        )
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                        SettingItem(
                            text = stringResource(R.string.suggest_feature),
                            onClick = {
                                Toast.makeText(context, R.string.feature_in_development, Toast.LENGTH_SHORT).show()
                            }
                        )
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f))
                        SettingItem(
                            text = stringResource(R.string.rate_app),
                            onClick = {
                                Toast.makeText(context, R.string.feature_in_development, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }

                item {
                    Button(
                        onClick = viewModel::logout,
                        enabled = !uiState.isLoggingOut,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        if (uiState.isLoggingOut) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.error)
                        } else {
                            Text(stringResource(id = R.string.logout), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
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
        Column(content = content)
    }
}
