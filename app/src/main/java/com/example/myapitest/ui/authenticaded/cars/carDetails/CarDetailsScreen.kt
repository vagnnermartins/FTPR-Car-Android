package com.example.myapitest.ui.authenticaded.cars.carDetails

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myapitest.MainActivity
import com.example.myapitest.network.StatusService
import com.example.myapitest.ui.composables.AppBar
import com.example.myapitest.ui.composables.FormInput
import com.example.myapitest.ui.theme.Grey800
import com.example.myapitest.ui.theme.LightGray
import com.example.myapitest.ui.theme.White
import com.example.myapitest.ui.theme.White30
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun CarDetailsScreen(
    modifier: Modifier = Modifier,
    carId: String,
    viewModel: CarDetailsViewModel = viewModel(),
    onBackPressed: () -> Unit = {}
) {
    LaunchedEffect(carId) {
        if (carId.isNotEmpty()) {
            viewModel.loadCarDetails(carId)
        }
    }

    when (val state = viewModel.loadCarsState) {
        is StatusService.Loading -> {
            LoadingScreen(onBackPressed = onBackPressed)
        }

        is StatusService.Success<*> -> {
            viewModel.car?.let { car ->
                CarDetailsContent(
                    viewModel = viewModel,
                    modifier = modifier,
                    onBackPressed = onBackPressed
                )
            } ?: ErrorScreen(
                message = "Car data not available",
                onRetry = { viewModel.loadCarDetails(carId) },
                onBackPressed = onBackPressed
            )
        }

        is StatusService.UnknownError -> {
            ErrorScreen(
                message = "Failed to load car details",
                onRetry = { viewModel.loadCarDetails(carId) },
                onBackPressed = onBackPressed
            )
        }

        is StatusService.Idle -> {
            // Initial state - could show a placeholder or trigger loading
            LoadingScreen(onBackPressed = onBackPressed)
        }

        is StatusService.Error -> TODO()
    }
}

@Composable
private fun LoadingScreen(
    onBackPressed: () -> Unit
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(White, LightGray)
                )
            ),
        containerColor = White30,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            AppBar(
                title = "Car Details",
                onBackPressed = onBackPressed
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Loading car details...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    onBackPressed: () -> Unit
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(White, LightGray)
                )
            ),
        containerColor = White30,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            AppBar(
                title = "Car Details",
                onBackPressed = onBackPressed
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Try Again")
                }
            }
        }
    }
}

@Composable
private fun CarDetailsContent(
    viewModel: CarDetailsViewModel,
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit
) {
    val scrollState = rememberScrollState()

    val carLocation = LatLng(viewModel.car!!.lat, viewModel.car!!.long)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(carLocation, 15f)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(White, LightGray)
                )
            ),
        containerColor = White30,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            AppBar(
                title = viewModel.car!!.name,
                onBackPressed = onBackPressed,
                action = {
                    when (viewModel.deleteCarState) {
                        is StatusService.Loading -> CircularProgressIndicator()
                        is StatusService.Success<*> -> {
                            viewModel.updateDeleteCarState(StatusService.Idle)
                            onBackPressed()
                        }
                        is StatusService.Error -> {
                            Toast.makeText(
                                LocalContext.current,
                                "An unknown error occurred while deleting the car.",
                                Toast.LENGTH_SHORT
                            ).show()
                            IconButton(
                                onClick = { viewModel.deleteCar() },
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = Color.Red
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                        }
                        is StatusService.Idle -> {
                            IconButton(
                                onClick = { viewModel.deleteCar() },
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = Color.Red
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        is StatusService.UnknownError -> {
                            Toast.makeText(
                                LocalContext.current,
                                "An unknown error occurred while deleting the car.",
                                Toast.LENGTH_SHORT
                            ).show()
                            IconButton(
                                onClick = { viewModel.deleteCar() },
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = Color.Red
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .weight(1.0f, fill = false)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            modifier = Modifier.fillMaxSize(),
                            model = viewModel.car!!.imageUrl,
                            contentDescription = "Car image",
                            contentScale = ContentScale.Crop,
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.3f)
                                        ),
                                        startY = 0f,
                                        endY = Float.POSITIVE_INFINITY
                                    )
                                )
                        )
                    }
                }

                // Car Information Section
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FormInput(
                        value = viewModel.car!!.name,
                        onValueChanged = { viewModel.updateCarName(it) },
                        label = "Name",
                    )
                    // Year Information
                    FormInput(
                        value = viewModel.car!!.year,
                        onValueChanged = { viewModel.updateCarYear(it) },
                        label = "Year",
                    )

                    // License Information
                    FormInput(
                        value = viewModel.car!!.licence,
                        onValueChanged = { viewModel.updateCarLicence(it) },
                        label = "License",
                    )
                }

                // Location Section
                if (viewModel.car!!.lat != 0.0 && viewModel.car!!.long != 0.0) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Location Header
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        text = "Location",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Map
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                GoogleMap(
                                    modifier = Modifier.fillMaxSize(),
                                    cameraPositionState = cameraPositionState,
                                    properties = MapProperties(
                                        mapType = MapType.NORMAL,
                                        isMyLocationEnabled = false
                                    ),
                                    uiSettings = MapUiSettings(
                                        zoomControlsEnabled = false,
                                        mapToolbarEnabled = true,
                                        myLocationButtonEnabled = false,
                                        compassEnabled = true,
                                        scrollGesturesEnabled = true,
                                        zoomGesturesEnabled = true
                                    )
                                ) {
                                    Marker(
                                        state = MarkerState(position = carLocation),
                                        title = viewModel.car!!.name,
                                        snippet = "Vehicle Location"
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Button Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = White30)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .padding(
                        bottom = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding()
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (viewModel.carsUpdateState) {
                    is StatusService.Loading -> CircularProgressIndicator()
                    is StatusService.Success<*> -> {
                        viewModel.updateCarsUpdateState(StatusService.Idle)
                        onBackPressed()
                        ContinueButton(viewModel)
                    }

                    is StatusService.Error -> ContinueButton(viewModel)
                    is StatusService.Idle -> ContinueButton(viewModel)
                    is StatusService.UnknownError -> Text("Error")
                }
            }
        }
    }
}

@Composable
fun ContinueButton(viewModel: CarDetailsViewModel) {
    val ctx = LocalContext.current
    val activity = ctx as MainActivity
    Button(
        onClick = {
            viewModel.updateCar()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .alpha(1f),
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Grey800,
            contentColor = Color.White,
            disabledContainerColor = Grey800,
            disabledContentColor = Color.White
        )
    ) {
        Text(
            text = "Update car",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )
    }
}