//package com.ltcn272.finny.presentation.features.transation.create_transaction
//
//import android.Manifest
//import android.content.Context
//import android.content.pm.PackageManager
//import android.net.Uri
//import android.os.Build
//import androidx.activity.compose.rememberLauncherForActivityResult
//import androidx.activity.result.PickVisualMediaRequest
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.gestures.detectTapGestures
//import androidx.compose.foundation.layout.*
//import androidx.compose.material3.Icon
//import androidx.compose.material3.SnackbarHost
//import androidx.compose.material3.SnackbarHostState
//import androidx.compose.runtime.*
//import androidx.compose.runtime.saveable.rememberSaveable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.input.pointer.pointerInput
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalFocusManager
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.unit.dp
//import androidx.core.content.ContextCompat
//import androidx.core.content.FileProvider
//import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
//import com.ltcn272.finny.R
//import com.ltcn272.finny.domain.model.Budget
//import com.ltcn272.finny.domain.model.TransactionCategory
//import com.ltcn272.finny.presentation.common.ui.CircleLoadingView
//import com.ltcn272.finny.presentation.common.ui.LocationPickerBottomSheet
//import com.ltcn272.finny.presentation.common.ui.SelectionDialog
//import com.ltcn272.finny.presentation.common.ui.WheelDateTimePickerDialog
//import com.ltcn272.finny.presentation.features.transation.create_transaction.component.*
//import kotlinx.coroutines.launch
//import java.io.File
//import java.text.SimpleDateFormat
//import java.util.Date
//import java.util.Locale
//
//// Hàm Helper để tạo Uri cho Camera (Giữ nguyên)
//private fun createImageUriWithFileProvider(context: Context): Uri? {
//    val imagePath = File(context.cacheDir, "images")
//    if (!imagePath.exists()) {
//        imagePath.mkdirs()
//    }
//
//    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
//    val imageFile = File(imagePath, "finny_${timeStamp}.jpg")
//
//    return FileProvider.getUriForFile(
//        context,
//        "${context.packageName}.provider",
//        imageFile
//    )
//}
//
//@OptIn(ExperimentalLayoutApi::class)
//@Composable
//fun CreateTransactionScreen(
//    onBack: () -> Unit,
//    onTransactionCreated: () -> Unit,
//    viewModel: CreateTransactionViewModel = hiltViewModel()
//) {
//    val focusManager = LocalFocusManager.current
//    val isKeyboardVisible = WindowInsets.isImeVisible
//
//    val formState by viewModel.formState.collectAsState()
//    val createUiState by viewModel.createUiState.collectAsState()
//    val budgets by viewModel.budgets.collectAsState()
//
//    val snackbarHostState = remember { SnackbarHostState() }
//    val scope = rememberCoroutineScope()
//    val context = LocalContext.current
//
//    val isSaving = createUiState is CreateTransactionUiState.Loading
//
//    // --- Tối ưu: Lấy tên Budget trực tiếp từ đối tượng đã chọn ---
//    val selectedBudget = formState.selectedBudget
//
//    // --- Launchers & Permissions (Giữ nguyên) ---
//
//    var hasLocationPermission by remember {
//        mutableStateOf(
//            ContextCompat.checkSelfPermission(
//                context, Manifest.permission.ACCESS_FINE_LOCATION
//            ) == PackageManager.PERMISSION_GRANTED
//        )
//    }
//
//    val locationPermissionLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestMultiplePermissions(),
//        onResult = { permissions ->
//            val isGranted =
//                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
//                        permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
//            hasLocationPermission = isGranted
//
//            viewModel.setDialogVisibility(DialogType.LOCATION, true)
//            if (!isGranted) {
//                scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.location_permission_denied_snackbar)) }
//            }
//        }
//    )
//
//    val imagePickerLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.GetContent(),
//        onResult = { uri: Uri? ->
//            if (uri != null) {
//                viewModel.onImageSelected(uri)
//                scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.image_selected)) }
//            } else {
//                scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.no_image_selected)) }
//            }
//        }
//    )
//    val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//        Manifest.permission.READ_MEDIA_IMAGES
//    } else {
//        Manifest.permission.READ_EXTERNAL_STORAGE
//    }
//    val pickVisualMediaLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.PickVisualMedia(),
//        onResult = { uri ->
//            if (uri != null) {
//                viewModel.onImageSelected(uri)
//                scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.image_selected)) }
//            } else {
//                scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.no_image_selected)) }
//            }
//        }
//    )
//    val permissionLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestPermission(),
//        onResult = { granted: Boolean ->
//            if (granted) {
//                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
//                    imagePickerLauncher.launch("image/*")
//                } else {
//                    pickVisualMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
//                }
//            } else {
//                scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.permission_denied)) }
//            }
//        }
//    )
//    var pendingCameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }
//    val takePictureLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.TakePicture(),
//        onResult = { success ->
//            val uri = pendingCameraUri
//            if (success && uri != null) {
//                viewModel.onImageSelected(uri)
//                scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.photo_captured_successfully)) }
//            } else {
//                scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.capture_cancelled_or_failed)) }
//            }
//            pendingCameraUri = null
//        }
//    )
//
//    // --- Launched Effect for Side Effects (Giữ nguyên) ---
//
//    LaunchedEffect(createUiState) {
//        when (val state = createUiState) {
//            is CreateTransactionUiState.Success -> {
//                scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.transaction_saved_successfully)) }
//                onTransactionCreated()
//                viewModel.resetCreateState()
//            }
//
//            is CreateTransactionUiState.Error -> {
//                scope.launch { snackbarHostState.showSnackbar(state.message) }
//                viewModel.resetCreateState()
//            }
//
//            else -> Unit
//        }
//    }
//
//    // --- Dialogs & Bottom Sheets ---
//
//    if (isSaving) {
//        CircleLoadingView()
//    }
//
//    if (formState.showLocationPicker) {
//        LocationPickerBottomSheet(
//            visible = true,
//            onDismiss = { viewModel.setDialogVisibility(DialogType.LOCATION, false) },
//            onLocationSelected = { location ->
//                viewModel.onLocationSelected(location)
//            },
//            hasLocationPermission = hasLocationPermission,
//            initialLocation = formState.location
//        )
//    }
//
//    SelectionDialog<Budget>(
//        title = stringResource(id = R.string.select_budget),
//        items = budgets,
//        visible = formState.showBudgetDialog,
//        onDismiss = { viewModel.setDialogVisibility(DialogType.BUDGET, false) },
//        onSelect = { budget -> viewModel.onBudgetSelect(budget) },
//        itemToString = { it.name },
//        // Sử dụng tên budget hiện tại
//        initialSelection = selectedBudget
//    )
//
//    SelectionDialog(
//        title = stringResource(id = R.string.select_category),
//        items = TransactionCategory.entries,
//        visible = formState.showCategoryDialog,
//        onDismiss = { viewModel.setDialogVisibility(DialogType.CATEGORY, false) },
//        onSelect = { category -> viewModel.onCategoryChange(category) },
//        initialSelection = formState.category,
//        itemToString = { it.name.lowercase().replaceFirstChar { char -> char.titlecase() } },
//        iconBuilder = {
//            val style = CategoryUtils.getStyle(it)
//            Icon(
//                imageVector = style.icon,
//                contentDescription = it.name,
//                tint = style.color,
//                modifier = Modifier.size(24.dp)
//            )
//        }
//    )
//
//    if (formState.showDateTimePicker) {
//        WheelDateTimePickerDialog(
//            startDateTime = formState.dateTime,
//            onDismiss = { viewModel.setDialogVisibility(DialogType.DATETIME, false) },
//            onConfirm = { viewModel.onDateTimeChange(it) }
//        )
//    }
//
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .windowInsetsPadding(WindowInsets.safeDrawing)
//            .pointerInput(Unit) {
//                detectTapGestures(onTap = {
//                    focusManager.clearFocus()
//                })
//            }
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(vertical = 8.dp, horizontal = 16.dp),
//        ) {
//            // 1. Header Component
//            CreateTransactionHeader(
//                onBack = onBack,
//                selectedType = formState.type,
//                onTypeSelected = viewModel::onTransactionTypeChange,
//                onSaveClick = viewModel::onSaveTransaction,
//                isSaving = isSaving
//            )
//
//            Spacer(modifier = Modifier.height(20.dp))
//
//            Box(modifier = Modifier.weight(1f)) {
//                // 2. Image and Location Controls Component
//                ImageLocationControls(
//                    formState = formState,
//                    permissionToRequest = permissionToRequest,
//                    context = context,
//                    hasLocationPermission = hasLocationPermission,
//                    onImagePicker = {
//                        pickVisualMediaLauncher.launch(
//                            PickVisualMediaRequest(
//                                ActivityResultContracts.PickVisualMedia.ImageOnly
//                            )
//                        )
//                    },
//                    onImagePickerLegacy = {
//                        imagePickerLauncher.launch("image/*")
//                    },
//                    onPermissionRequest = { permissionLauncher.launch(permissionToRequest) },
//                    onCreateImageUri = {
//                        createImageUriWithFileProvider(context).also { pendingCameraUri = it }
//                    },
//                    onTakePicture = { takePictureLauncher.launch(it) },
//                    onImageClear = { viewModel.onImageSelected(null) },
//                    onLocationClick = {
//                        if (hasLocationPermission) {
//                            viewModel.setDialogVisibility(DialogType.LOCATION, true)
//                        } else {
//                            locationPermissionLauncher.launch(
//                                arrayOf(
//                                    Manifest.permission.ACCESS_FINE_LOCATION,
//                                    Manifest.permission.ACCESS_COARSE_LOCATION
//                                )
//                            )
//                        }
//                    },
//                    onShowSnackbar = { scope.launch { snackbarHostState.showSnackbar(it) } }
//                )
//
//                // 3. Main Details Form Component
//                TransactionDetailsForm(
//                    formState = formState,
//                    selectedBudgetName = selectedBudget?.name,
//                    onNameChange = viewModel::onNameChange,
//                    onDescriptionChange = viewModel::onDescriptionChange,
//                    onBudgetClick = { viewModel.setDialogVisibility(DialogType.BUDGET, true) }
//                )
//            }
//
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // 4. Footer Controls Component
//            FooterControls(
//                formState = formState,
//                onDateTimeClick = { viewModel.setDialogVisibility(DialogType.DATETIME, true) },
//                onCategoryClick = { viewModel.setDialogVisibility(DialogType.CATEGORY, true) }
//            )
//
//            Spacer(modifier = Modifier.height(10.dp))
//
//            // 5. Num Keyboard Component
//            if (!isKeyboardVisible) {
//                NumKeyboardLayout(
//                    onKeyClick = viewModel::onAmountKeyPress,
//                    onBackspace = viewModel::onAmountBackspace,
//                    onClear = viewModel::onAmountClear,
//                    onDone = { focusManager.clearFocus() }
//                )
//            }
//        }
//        SnackbarHost(
//            hostState = snackbarHostState,
//            modifier = Modifier.align(Alignment.BottomCenter)
//        )
//    }
//}