package com.ltcn272.finny.presentation.features.setting

import StackedSnackbarHost
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ltcn272.finny.R
import com.ltcn272.finny.presentation.common.ui.AnimatedCurrencyMenu
import com.ltcn272.finny.presentation.features.setting.component.PremiumCard
import com.ltcn272.finny.presentation.features.setting.component.SettingItem
import com.ltcn272.finny.presentation.features.setting.component.SubscriptionBottomSheet
import com.ltcn272.finny.presentation.features.setting.component.SettingSwitchItem
import com.ltcn272.finny.presentation.theme.MainBackgroundBrush
import com.ltcn272.finny.util.PermissionUtils
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import rememberStackedSnackbarHostState

@Composable
fun SettingScreen(
    viewModel: SettingViewModel = hiltViewModel(),
    onNavigateToProfile: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onLoggedOut: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val stackedSnackbarHostState = rememberStackedSnackbarHostState(maxStack = 2)

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            coroutineScope.launch {
                if (granted) {
                    viewModel.setEnableNotifications(true)
                } else {
                    viewModel.setEnableNotifications(false)
                    stackedSnackbarHostState.showErrorSnackbar(
                        title = context.getString(R.string.notification_permission_denied_title),
                        description = context.getString(R.string.notification_permission_denied),
                        actionTitle = context.getString(R.string.open_settings),
                        action = { PermissionUtils.openAppNotificationSettings(context) },
                        duration = StackedSnackbarDuration.Long
                    )
                }
            }
        }
    )

    var showCurrencyMenu by remember { mutableStateOf(false) }
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()

    val username by viewModel.username.collectAsState()
    val isLoggingOut by viewModel.isLoggingOut.collectAsState()

    val enableNotifications by viewModel.enableNotifications.collectAsState()
    val authenticationEnabled by viewModel.authenticationEnabled.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.logoutEvent.collect { onLoggedOut() }
    }

    LaunchedEffect(Unit) {
        viewModel.notificationEvent.collect { resId ->
            when (resId) {
                R.string.notification_sent_success -> stackedSnackbarHostState.showSuccessSnackbar(
                    title = context.getString(R.string.notification_sent_success_title),
                    description = context.getString(R.string.notification_sent_success),
                    duration = StackedSnackbarDuration.Short
                )

                R.string.notification_test_sent_success -> stackedSnackbarHostState.showSuccessSnackbar(
                    title = context.getString(R.string.notification_test_sent_success_title),
                    description = context.getString(R.string.notification_test_sent_success),
                    duration = StackedSnackbarDuration.Short
                )

                R.string.notification_sent_failed -> stackedSnackbarHostState.showErrorSnackbar(
                    title = context.getString(R.string.notification_sent_failed_title),
                    description = context.getString(R.string.notification_sent_failed),
                    duration = StackedSnackbarDuration.Short
                )

                R.string.notification_test_sent_failed -> stackedSnackbarHostState.showErrorSnackbar(
                    title = context.getString(R.string.notification_test_sent_failed_title),
                    description = context.getString(R.string.notification_test_sent_failed),
                    duration = StackedSnackbarDuration.Short
                )

                R.string.notification_permission_denied -> stackedSnackbarHostState.showErrorSnackbar(
                    title = context.getString(R.string.notification_permission_denied_title),
                    description = context.getString(R.string.notification_permission_denied),
                    actionTitle = context.getString(R.string.open_settings),
                    action = { PermissionUtils.openAppNotificationSettings(context) },
                    duration = StackedSnackbarDuration.Short
                )

                else -> stackedSnackbarHostState.showInfoSnackbar(
                    title = context.getString(resId),
                    duration = StackedSnackbarDuration.Short
                )
            }
        }
    }

    var showSubscriptionSheet by remember { mutableStateOf(false) }
    var selectedPeriodIndex by remember { mutableStateOf(0) }
    var selectedPlanId by remember { mutableStateOf<String?>(null) }

    val pricePlans by viewModel.pricePlans.collectAsState()
    val isLoadingPrices by viewModel.isLoadingPrices.collectAsState()
    val isSubscribing by viewModel.isSubscribing.collectAsState()

    val tiers = pricePlans.map { it.planName }.distinct().ifEmpty { listOf("Pro", "Premium") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MainBackgroundBrush)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(vertical = 8.dp, horizontal = 16.dp),
    ) {

        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = 70.dp)
        ) {
            item {
                if (isLoadingPrices) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                PremiumCard(onSubscribeClick = {
                    val defaultPlan =
                        pricePlans.firstOrNull { it.planName == tiers.getOrNull(selectedPeriodIndex) }
                            ?: pricePlans.firstOrNull()
                    selectedPlanId = defaultPlan?.id
                    showSubscriptionSheet = true
                })

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToProfile() }
                                .padding(vertical = 16.dp, horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val value =
                                if (username.isNullOrBlank()) stringResource(id = R.string.user_name_placeholder) else username
                            if (value != null) Text(
                                text = value,
                                color = Color.Black,
                                fontSize = 16.sp
                            )
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        Box {
                            SettingItem(
                                text = stringResource(id = R.string.currency),
                                value = selectedCurrency,
                                onClick = { showCurrencyMenu = true })
                            AnimatedCurrencyMenu(
                                expanded = showCurrencyMenu,
                                onDismissRequest = { showCurrencyMenu = false },
                                currencies = listOf("VND", "USD"),
                                selectedCurrency = selectedCurrency,
                                onCurrencyClick = {
                                    viewModel.onCurrencySelected(it)
                                    showCurrencyMenu = false
                                },
                                offset = DpOffset(x = 0.dp, y = 38.dp)
                            )
                        }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingItem(
                            text = stringResource(id = R.string.categories),
                            onClick = onNavigateToCategories
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(id = R.string.app_preferences),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column {
                        SettingSwitchItem(
                            text = stringResource(id = R.string.enable_notifications),
                            checked = enableNotifications,
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) notificationPermissionLauncher.launch(
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) else viewModel.setEnableNotifications(true)
                                } else viewModel.setEnableNotifications(false)
                            })
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingSwitchItem(
                            text = stringResource(id = R.string.authentication),
                            checked = authenticationEnabled,
                            onCheckedChange = { viewModel.setAuthenticationEnabled(it) })
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column {
                        SettingItem(
                            text = stringResource(id = R.string.export_data),
                            onClick = { viewModel.onExportDataClicked() },
                            textColor = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingItem(
                            text = stringResource(id = R.string.ask_for_feature),
                            onClick = { viewModel.onSuggestFeatureClicked() },
                            textColor = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingItem(
                            text = stringResource(id = R.string.review_the_app),
                            onClick = { viewModel.onRateAppClicked() },
                            textColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    onClick = { viewModel.onLogoutClicked() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isLoggingOut) CircularProgressIndicator(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .height(20.dp)
                                .width(20.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = stringResource(id = R.string.log_out),
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        StackedSnackbarHost(hostState = stackedSnackbarHostState)
    }


    SubscriptionBottomSheet(
        visible = showSubscriptionSheet,
        onDismiss = { showSubscriptionSheet = false },
        pricePlans = pricePlans,
        periodOptions = tiers,
        selectedPeriodIndex = selectedPeriodIndex,
        onPeriodSelected = { selectedPeriodIndex = it },
        selectedPlanId = selectedPlanId,
        onPlanSelected = { selectedPlanId = it },
        isSubscribing = isSubscribing,
        onSubscribe = { planId, periodIndex ->
            viewModel.subscribe(
                planId,
                periodIndex
            ) { success, _ -> if (success) showSubscriptionSheet = false }
        })

    LaunchedEffect(enableNotifications) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val has = PermissionUtils.hasNotificationPermission(context)
            if (enableNotifications && !has) viewModel.setEnableNotifications(false)
        }
    }
}
