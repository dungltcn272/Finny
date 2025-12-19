//package com.ltcn272.finny.presentation.features.transation.create_transaction.component
//
//import android.Manifest
//import android.content.Context
//import android.content.pm.PackageManager
//import android.net.Uri
//import android.os.Build
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.res.stringResource
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import coil.compose.AsyncImage
//import com.ltcn272.finny.R
//import com.ltcn272.finny.presentation.common.ui.CircleIconButton
//import com.ltcn272.finny.presentation.features.transation.create_transaction.CreateTransactionFormState
//import java.io.File
//import androidx.compose.foundation.basicMarquee
//import androidx.compose.ui.graphics.Color
//
//@Composable
//fun ImageLocationControls(
//    formState: CreateTransactionFormState,
//    permissionToRequest: String,
//    context: Context,
//    hasLocationPermission: Boolean,
//    onImagePicker: () -> Unit,
//    onImagePickerLegacy: () -> Unit,
//    onPermissionRequest: () -> Unit,
//    onCreateImageUri: () -> Uri?,
//    onTakePicture: (Uri) -> Unit,
//    onImageClear: () -> Unit,
//    onLocationClick: () -> Unit,
//    onShowSnackbar: (String) -> Unit
//) {
//    Row(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalArrangement = Arrangement.SpaceBetween,
//    ) {
//        // --- Image Controls ---
//        Column {
//            CircleIconButton(
//                onClick = {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                        onImagePicker()
//                    } else {
//                        // Tùy chọn legacy cho Android cũ
//                        if (androidx.core.content.ContextCompat.checkSelfPermission(
//                                context,
//                                permissionToRequest
//                            ) == PackageManager.PERMISSION_GRANTED
//                        ) {
//                            onImagePickerLegacy()
//                        } else {
//                            onPermissionRequest()
//                        }
//                    }
//                },
//                icon = R.drawable.ic_image,
//            )
//
//            Spacer(
//                Modifier
//                    .padding(vertical = 5.dp)
//                    .width(28.dp)
//                    .height(2.dp)
//                    .background(Color.Gray)
//            )
//            CircleIconButton(
//                onClick = {
//                    val newUri = onCreateImageUri()
//                    if (newUri != null) {
//                        onTakePicture(newUri)
//                    } else {
//                        onShowSnackbar(context.getString(R.string.could_not_create_image_file))
//                    }
//                },
//                icon = R.drawable.ic_camera
//            )
//
//            formState.imageUri?.let { uri ->
//                Spacer(modifier = Modifier.height(5.dp))
//                Box(
//                    modifier = Modifier
//                        .size(60.dp)
//                        .clip(RoundedCornerShape(8.dp))
//                        .border(
//                            1.dp,
//                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
//                            RoundedCornerShape(8.dp)
//                        )
//                ) {
//                    AsyncImage(
//                        model = uri,
//                        contentDescription = stringResource(id = R.string.selected_image),
//                        modifier = Modifier.fillMaxSize(),
//                        contentScale = ContentScale.Crop
//                    )
//                    IconButton(
//                        onClick = onImageClear,
//                        modifier = Modifier
//                            .align(Alignment.TopEnd)
//                            .padding(4.dp)
//                            .size(10.dp)
//                    ) {
//                        Icon(
//                            painter = painterResource(id = R.drawable.ic_close),
//                            contentDescription = stringResource(id = R.string.clear_image),
//                            tint = Color.White
//                        )
//                    }
//                }
//            }
//        }
//
//        // --- Location Controls ---
//        Column(horizontalAlignment = Alignment.End) {
//            CircleIconButton(
//                onClick = onLocationClick,
//                icon = R.drawable.ic_location
//            )
//            formState.location?.name?.let {
//                Spacer(modifier = Modifier.height(4.dp))
//                Text(
//                    text = it,
//                    style = MaterialTheme.typography.labelSmall,
//                    maxLines = 2,
//                    overflow = TextOverflow.Ellipsis,
//                    modifier = Modifier
//                        .width(60.dp)
//                        .basicMarquee(),
//                    textAlign = TextAlign.Center
//                )
//            }
//        }
//    }
//}