package com.ltcn272.finny.presentation.common.ui

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.ltcn272.finny.R
import com.ltcn272.finny.domain.model.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onLocationSelected: (Location) -> Unit,
    hasLocationPermission: Boolean,
    initialLocation: Location? = null
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var canLoadMapAndLocation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        canLoadMapAndLocation = true
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        sheetGesturesEnabled = false
    ) {
        if (canLoadMapAndLocation) {
            LocationPickerContent(
                onDismiss = onDismiss,
                onLocationSelected = onLocationSelected,
                hasLocationPermission = hasLocationPermission,
                initialLocation = initialLocation
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
private fun LocationPickerContent(
    onDismiss: () -> Unit,
    onLocationSelected: (Location) -> Unit,
    hasLocationPermission: Boolean,
    initialLocation: Location?
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val initialLatLng = remember(initialLocation) {
        initialLocation?.let { LatLng(it.lat, it.lng) }
    }
    val hanoiLatLng = LatLng(21.0278, 105.8342)
    val cameraPositionState = rememberCameraPositionState {
        position = if (initialLatLng != null) {
            CameraPosition.fromLatLngZoom(initialLatLng, 17f)
        } else {
            CameraPosition.fromLatLngZoom(hanoiLatLng, 15f)
        }
    }

    val mapStyleOptions = remember {
        try {
            MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style)
        } catch (e: Exception) {
            // Log error or handle gracefully if style file is missing
            null
        }
    }

    var mapLoaded by remember { mutableStateOf(false) }
    val isMapMoving by remember { derivedStateOf { cameraPositionState.isMoving } }
    var geocodingInProgress by remember { mutableStateOf(false) }
    var selectedLocationName by remember {
        mutableStateOf(initialLocation?.name ?: "Di chuyển bản đồ để chọn")
    }
    var geocodingJob by remember { mutableStateOf<Job?>(null) }

    fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    LaunchedEffect(isMapMoving) {
        if (isMapMoving) {
            geocodingJob?.cancel()
            geocodingInProgress = false
            return@LaunchedEffect
        }

        delay(700)

        geocodingJob = scope.launch(Dispatchers.IO) {
            geocodingInProgress = true
            val latLng = cameraPositionState.position.target

            val addressResult = withTimeoutOrNull(3000L) {
                try {
                    val geocoder = Geocoder(context, Locale.forLanguageTag("vi-VN"))
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        var address: android.location.Address? = null
                        geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) {
                            address = it.firstOrNull()
                        }
                        delay(200)
                        address?.getAddressLine(0)
                    } else {
                        @Suppress("DEPRECATION")
                        geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                            ?.firstOrNull()?.getAddressLine(0)
                    }
                } catch (e: Exception) {
                    null
                }
            }

            selectedLocationName = addressResult ?: String.format(
                Locale.US,
                "Lat: %.3f, Lng: %.3f",
                latLng.latitude,
                latLng.longitude
            )
            geocodingInProgress = false
        }
    }

    fun getCurrentLocation() {
        if (!isLocationEnabled()) {
            Toast.makeText(context, "Vui lòng bật vị trí (GPS) để sử dụng tính năng này", Toast.LENGTH_SHORT).show()
            return
        }

        if (hasLocationPermission) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { location: android.location.Location? ->
                location?.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)
                    scope.launch {
                        try {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(userLatLng, 17f)
                            )
                        } catch (_: Exception) {
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(userLatLng, 17f)
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(mapLoaded) {
        if (mapLoaded) {
            if (initialLocation == null && hasLocationPermission && isLocationEnabled()) {
                getCurrentLocation()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            ),
            properties = MapProperties(
                isMyLocationEnabled = hasLocationPermission,
                mapStyleOptions = mapStyleOptions
            ),
            onMapLoaded = {
                mapLoaded = true
            }
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_location_pin),
            contentDescription = "Vị trí đã chọn",
            modifier = Modifier
                .align(Alignment.Center)
                .padding(bottom = 40.dp)
                .size(40.dp),
            tint = Color.Unspecified
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(vertical = 10.dp, horizontal = 15.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            CircleShape
                        )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Chọn vị trí",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isMapMoving) "Đang di chuyển..." else selectedLocationName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier.heightIn(min = 40.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val finalLocation = Location(
                            name = selectedLocationName,
                            lat = cameraPositionState.position.target.latitude,
                            lng = cameraPositionState.position.target.longitude
                        )
                        onLocationSelected(finalLocation)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    enabled = !isMapMoving && !geocodingInProgress
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_check),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Xác nhận vị trí", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        if (geocodingInProgress) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.6f),
                contentColor = Color.White,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.padding(start = 12.dp))
                    Text("Đang tìm địa chỉ...", fontSize = 14.sp)
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CircleIconButton(onClick = onDismiss, icon = R.drawable.ic_close)

            if (hasLocationPermission) {
                FloatingActionButton(
                    onClick = { getCurrentLocation() },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_location),
                        contentDescription = "Vị trí của tôi"
                    )
                }
            }
        }
    }
}
