package com.ltcn272.finny.presentation.features.transation.create_transaction

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.TransactionCategory
import com.ltcn272.finny.presentation.common.ui.CircleIconButton
import com.ltcn272.finny.presentation.common.ui.CircleLoadingView
import com.ltcn272.finny.presentation.common.ui.LocationPickerBottomSheet
import com.ltcn272.finny.presentation.common.ui.SelectionDialog
import com.ltcn272.finny.presentation.common.ui.WheelDateTimePickerDialog
import com.ltcn272.finny.presentation.features.transation.create_transaction.component.AmountDisplay
import com.ltcn272.finny.presentation.features.transation.create_transaction.component.BudgetSurface
import com.ltcn272.finny.presentation.features.transation.create_transaction.component.CategorySurface
import com.ltcn272.finny.presentation.features.transation.create_transaction.component.DateTimeSurface
import com.ltcn272.finny.presentation.features.transation.create_transaction.component.NumKeyboardLayout
import com.ltcn272.finny.presentation.features.transation.create_transaction.component.TransactionTypeSelector
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Preview
@Composable
fun CreateTransactionScreenPreview() {
    CreateTransactionScreen(
        onBack = {},
        onTransactionCreated = {}
    )
}

private fun createImageUriWithFileProvider(context: Context): Uri? {
    val imagePath = File(context.cacheDir, "images")
    if (!imagePath.exists()) {
        imagePath.mkdirs()
    }

    val timeStamp = SimpleDateFormat(
        "yyyyMMdd_HHmmss",
        Locale.getDefault()
    ).format(Date())
    val imageFile = File(imagePath, "finny_${timeStamp}.jpg")

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        imageFile
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateTransactionScreen(
    onBack: () -> Unit,
    onTransactionCreated: () -> Unit,
    viewModel: CreateTransactionViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current
    val isKeyboardVisible = WindowInsets.isImeVisible

    val formState by viewModel.formState.collectAsState()
    val createUiState by viewModel.createUiState.collectAsState()
    val budgets by viewModel.budgets.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val isGranted =
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) ||
                        permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
            hasLocationPermission = isGranted

            viewModel.openLocationPicker()

            if (!isGranted) {
                scope.launch { snackbarHostState.showSnackbar("Location permission denied, cannot auto-locate.") }
            }
        }
    )

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                viewModel.onImageSelected(uri)
                scope.launch { snackbarHostState.showSnackbar("Image selected") }
            } else {
                scope.launch { snackbarHostState.showSnackbar("No image selected") }
            }
        }
    )
    val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    val pickVisualMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.onImageSelected(uri)
                scope.launch { snackbarHostState.showSnackbar("Image selected") }
            } else {
                scope.launch { snackbarHostState.showSnackbar("No image selected") }
            }
        }
    )
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted: Boolean ->
            if (granted) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    imagePickerLauncher.launch("image/*")
                } else {
                    pickVisualMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }
            } else {
                scope.launch { snackbarHostState.showSnackbar("Permission Denied") }
            }
        }
    )
    var pendingCameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            val uri = pendingCameraUri
            if (success && uri != null) {
                viewModel.onImageSelected(uri)
                scope.launch { snackbarHostState.showSnackbar("Photo captured successfully") }
            } else {
                scope.launch { snackbarHostState.showSnackbar("Capture cancelled or failed") }
            }
            pendingCameraUri = null
        }
    )


    LaunchedEffect(createUiState) {
        when (val state = createUiState) {
            is CreateTransactionUiState.Success -> {
                scope.launch { snackbarHostState.showSnackbar("Transaction saved successfully") }
                onTransactionCreated()
                viewModel.resetCreateState()
            }

            is CreateTransactionUiState.Error -> {
                scope.launch { snackbarHostState.showSnackbar(state.message) }
                viewModel.resetCreateState()
            }

            else -> Unit
        }
    }

    if (createUiState is CreateTransactionUiState.Loading) {
        CircleLoadingView()
    }

    if (formState.showLocationPicker) {
        LocationPickerBottomSheet(
            visible = true,
            onDismiss = { viewModel.dismissLocationPicker() },
            onLocationSelected = { location ->
                viewModel.onLocationSelected(location)
            },
            hasLocationPermission = hasLocationPermission,
            initialLocation = formState.location
        )
    }

    SelectionDialog(
        title = "Select Budget",
        items = budgets.map { it.name },
        visible = formState.showBudgetDialog,
        onDismiss = { viewModel.dismissBudgetDialog() },
        onSelect = { name -> viewModel.onBudgetSelect(name) },
        initialSelection = formState.selectedBudgetName.takeIf { it != "Select Budget" }
    )

    SelectionDialog(
        title = "Select Category",
        items = TransactionCategory.entries.map {
            it.name.lowercase().replaceFirstChar { char -> char.titlecase() }
        },
        visible = formState.showCategoryDialog,
        onDismiss = { viewModel.dismissCategoryDialog() },
        onSelect = {
            viewModel.onCategoryChange(TransactionCategory.valueOf(it.uppercase()))
        },
        initialSelection = formState.category.name.lowercase().replaceFirstChar { it.titlecase() }
    )

    if (formState.showDateTimePicker) {
        WheelDateTimePickerDialog(
            startDateTime = formState.dateTime,
            onDismiss = { viewModel.dismissDateTimePicker() },
            onConfirm = { viewModel.onDateTimeChange(it) }
        )
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp, horizontal = 16.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                CircleIconButton(onClick = onBack, icon = R.drawable.ic_close)
                TransactionTypeSelector(
                    modifier = Modifier.align(Alignment.Center),
                    selectedType = formState.type,
                    onTypeSelected = { viewModel.onTransactionTypeChange(it) }
                )
                Surface(
                    onClick = { viewModel.saveTransaction() },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .height(32.dp),
                    enabled = createUiState !is CreateTransactionUiState.Loading,
                    shape = CircleShape,
                    color = Color.Black,
                    contentColor = Color.White
                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 15.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Save",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        CircleIconButton(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    pickVisualMediaLauncher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                } else {
                                    if (ContextCompat.checkSelfPermission(
                                            context,
                                            permissionToRequest
                                        ) == PackageManager.PERMISSION_GRANTED
                                    ) {
                                        imagePickerLauncher.launch("image/*")
                                    } else {
                                        permissionLauncher.launch(permissionToRequest)
                                    }
                                }
                            },
                            icon = R.drawable.ic_image,
                        )

                        Spacer(
                            Modifier
                                .padding(vertical = 5.dp)
                                .width(28.dp)
                                .height(2.dp)
                                .background(Color.Gray)
                        )
                        CircleIconButton(
                            onClick = {
                                val newUri = createImageUriWithFileProvider(context)
                                if (newUri != null) {
                                    pendingCameraUri = newUri
                                    takePictureLauncher.launch(newUri)
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar("Could not create image file") }
                                }
                            },
                            icon = R.drawable.ic_camera
                        )

                        formState.imageUri?.let { uri ->
                            Spacer(modifier = Modifier.height(5.dp))
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                        RoundedCornerShape(8.dp)
                                    )
                            ) {
                                AsyncImage(
                                    model = uri, // Truyền trực tiếp đối tượng Uri
                                    contentDescription = "Selected Image",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { viewModel.clearImage() },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(10.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_close),
                                        contentDescription = "Clear Image",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }


                    Column(horizontalAlignment = Alignment.End) {
                        CircleIconButton(
                            onClick = {
                                val permissionGranted = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                                hasLocationPermission = permissionGranted

                                if (permissionGranted) {
                                    viewModel.openLocationPicker()
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            },
                            icon = R.drawable.ic_location
                        )
                        formState.location?.name?.let {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .width(60.dp)
                                    .basicMarquee(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    BasicTextField(
                        value = formState.name,
                        onValueChange = { viewModel.onNameChange(it) },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            color = LocalContentColor.current,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        ),
                        cursorBrush = SolidColor(LocalContentColor.current),
                        modifier = Modifier.fillMaxWidth(0.6f),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (formState.name.isEmpty()) {
                                    Text(
                                        text = "Enter name",
                                        style = MaterialTheme.typography.headlineLarge,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    AmountDisplay(
                        amount = formState.amount,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    BudgetSurface(
                        budgetName = formState.selectedBudgetName,
                        onClick = { viewModel.openBudgetDialog() }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    BasicTextField(
                        value = formState.description,
                        onValueChange = { viewModel.onDescriptionChange(it) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = LocalContentColor.current,
                            textAlign = TextAlign.Center
                        ),
                        cursorBrush = SolidColor(LocalContentColor.current),
                        decorationBox = { innerTextField ->
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                if (formState.description.isEmpty()) {
                                    Text(
                                        text = "Enter description",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Color.Gray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateTimeSurface(
                    dateTime = formState.dateTime,
                    onDateTimeClick = { viewModel.openDateTimePicker() },
                    modifier = Modifier.weight(6f)
                )
                CategorySurface(
                    category = formState.category,
                    onClick = { viewModel.openCategoryDialog() },
                    modifier = Modifier.weight(2f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (!isKeyboardVisible) {
                NumKeyboardLayout(
                    onKeyClick = { key -> viewModel.onAmountKeyClick(key) },
                    onBackspace = { viewModel.onAmountBackspace() }
                )
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
