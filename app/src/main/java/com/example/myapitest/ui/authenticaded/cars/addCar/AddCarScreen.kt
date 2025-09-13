package com.example.myapitest.ui.authenticaded.cars.addCar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapitest.MainActivity
import com.example.myapitest.network.StatusService
import com.example.myapitest.ui.authenticaded.composables.ImagePicker
import com.example.myapitest.ui.composables.AppBar
import com.example.myapitest.ui.composables.FormInput
import com.example.myapitest.ui.login.loginForm.ContinueButton
import com.example.myapitest.ui.login.loginForm.LoginViewModel
import com.example.myapitest.ui.theme.Grey800
import com.example.myapitest.ui.theme.LightGray
import com.example.myapitest.ui.theme.MyApiTestTheme
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
fun AddCarScreen(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
    viewModel: AddCarViewModel = viewModel(),
) {
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var showLocationInfo by remember { mutableStateOf(false) }

    // Default camera position (you can change this to user's current location)
    val defaultLocation = LatLng(-23.5505, -46.6333) // São Paulo, Brazil
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 10f)
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
                title = "New Car",
                onBackPressed = onBackPressed
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .weight(1.0f, fill = false)
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                FormInput(
                    value = viewModel.name,
                    label = "Name",
                    onValueChanged = { viewModel.updateName(it) },
                    placeholder = {
                        Text(text = "Enter")
                    },
                )
                Spacer(modifier = Modifier.height(12.dp))
                FormInput(
                    value = viewModel.license,
                    label = "License",
                    onValueChanged = { viewModel.updateLicense(it) },
                    placeholder = {
                        Text(text = "Enter")
                    },
                )
                Spacer(modifier = Modifier.height(12.dp))
                FormInput(
                    value = viewModel.year,
                    label = "Year",
                    onValueChanged = { viewModel.updateYear(it) },
                    placeholder = {
                        Text(text = "Enter")
                    },
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Image",
                    fontWeight = FontWeight.SemiBold
                )
                ImagePicker(
                    folder = "cars",
                    onImageSelected = { url ->
                        viewModel.updateImageUrl(url)
                    },
                    onImageRemoved = {
                        viewModel.updateImageUrl("")
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Location",
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Location Info Card
                selectedLocation?.let { location ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Selected Location",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Lat: ${String.format("%.6f", location.latitude)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Lng: ${String.format("%.6f", location.longitude)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Map Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            properties = MapProperties(
                                mapType = MapType.NORMAL,
                                isMyLocationEnabled = false
                            ),
                            uiSettings = MapUiSettings(
                                zoomControlsEnabled = true,
                                mapToolbarEnabled = false
                            ),
                            onMapClick = { latLng ->
                                selectedLocation = latLng
                                showLocationInfo = true
                                viewModel.updateLocation(latLng.latitude, latLng.longitude)
                            }
                        ) {
                            selectedLocation?.let { location ->
                                Marker(
                                    state = MarkerState(position = location),
                                    title = "Selected Location",
                                    snippet = "Lat: ${String.format("%.4f", location.latitude)}, Lng: ${String.format("%.4f", location.longitude)}"
                                )
                            }
                        }

                        if (selectedLocation == null) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                    Text(
                                        text = "Tap on the map to select location",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Bottom Button Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = White30)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (viewModel.additionState) {
                    is StatusService.Loading -> CircularProgressIndicator()
                    is StatusService.Success<*> -> {
                        viewModel.updateState(StatusService.Idle)
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

@Preview(showBackground = true)
@Composable
fun AddCarScreenPreview(modifier: Modifier = Modifier) {
    MyApiTestTheme {
        AddCarScreen()
    }
}

@Composable
fun ContinueButton(viewModel: AddCarViewModel) {
    val ctx = LocalContext.current
    val activity = ctx as MainActivity
    Button(
        onClick = {
            viewModel.addCar()
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
            text = "Add new car",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )
    }
}