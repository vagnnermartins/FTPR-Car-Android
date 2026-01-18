package com.utfpr.psil.cartrack.ui.screens

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.utfpr.psil.cartrack.R
import com.utfpr.psil.cartrack.ui.viewmodels.CarFormViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun CarFormScreen(
    @StringRes title: Int, @StringRes bottomButtonText: Int, onBackButtonPress: () -> Unit
) {
    var tempPhotoUri by remember { mutableStateOf<Uri>(Uri.EMPTY) }

    val context = LocalContext.current
    val viewModel: CarFormViewModel = hiltViewModel()

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val permissionRequests = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.CAMERA
        )
    )
    LaunchedEffect(Unit) {
        if (!permissionRequests.allPermissionsGranted) {
            permissionRequests.launchMultiplePermissionRequest()
        }
    }

    val cameraPositionState = rememberCameraPositionState()
    LaunchedEffect(uiState.selectedCoordinates) {
        uiState.selectedCoordinates?.let { coordinates ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(
                coordinates, 15f
            )
        }
    }
    val isSuggestionBoxExpanded = uiState.addressSuggestions.isNotEmpty()

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(), onResult = { success ->
            if (success && tempPhotoUri != Uri.EMPTY) {
                viewModel.onPhotoCaptured(context, tempPhotoUri)
            }
        }
    )

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearErrorMessage()
        }
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onBackButtonPress()
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }, topBar = {
        TopAppBar(
            windowInsets = WindowInsets.statusBars,
            title = { Text(stringResource(title)) },
            navigationIcon = {
                IconButton(onClick = onBackButtonPress) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = null
                    )
                }
            },
            colors = TopAppBarColors(
                containerColor = Color(0xff1e1f57),
                titleContentColor = Color.White,
                actionIconContentColor = Color.White,
                scrolledContainerColor = Color(0xff1e1f57),
                navigationIconContentColor = Color.White
            )
        )
    }, bottomBar = {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Button(
                onClick = { viewModel.onSaveCar() },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xff1e1f57), contentColor = Color.White
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White, modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = stringResource(bottomButtonText),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = uiState.photoBitmap ?: uiState.imageUrl,
                        contentDescription = null,
                        placeholder = painterResource(R.drawable.ic_no_photo),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Button(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 16.dp),
                        onClick = {
                            if (permissionRequests.allPermissionsGranted) {
                                tempPhotoUri = viewModel.createPhotUri(context)
                                cameraLauncher.launch(tempPhotoUri)
                            } else {
                                permissionRequests.launchMultiplePermissionRequest()
                            }
                        }, shape = MaterialTheme.shapes.small, colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White, contentColor = Color(0xff1e1f57)
                        ), content = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    modifier = Modifier.padding(end = 8.dp),
                                    painter = painterResource(R.drawable.ic_camera),
                                    contentDescription = null
                                )
                                Text("Tirar Foto")
                            }
                        })
                }
            }

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                value = uiState.carName,
                onValueChange = { viewModel.onCarNameChange(it) },
                label = { Text(stringResource(R.string.label_tf_car_name)) },
            )

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                value = uiState.carYear,
                onValueChange = { viewModel.onCarYearChange(it) },
                label = { Text(stringResource(R.string.label_tf_car_year)) },
            )

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                value = uiState.carPlate,
                onValueChange = { viewModel.onCarPlateChange(it) },
                label = { Text(stringResource(R.string.label_tf_car_plate)) },
            )

            ExposedDropdownMenuBox(
                expanded = isSuggestionBoxExpanded, onExpandedChange = {
                    if (!it) viewModel.onDismissSuggestions()
                }) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .menuAnchor(MenuAnchorType.PrimaryEditable, enabled = false),
                    value = uiState.addressInput,
                    onValueChange = { newText ->
                        viewModel.onAddressInputChange(newText)
                    },
                    singleLine = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = isSuggestionBoxExpanded
                        )
                    },
                    label = { Text(stringResource(R.string.label_tf_car_address)) },
                )
                if (uiState.addressSuggestions.isNotEmpty()) {
                    ExposedDropdownMenu(
                        expanded = isSuggestionBoxExpanded,
                        onDismissRequest = { viewModel.onDismissSuggestions() },
                        content = {
                            uiState.addressSuggestions.forEach { suggestion ->
                                DropdownMenuItem(
                                    text = { Text(suggestion.description) },
                                    onClick = {
                                        viewModel.onSuggestionSelected(suggestion)
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        })
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(top = 16.dp)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(
                        isMyLocationEnabled = permissionRequests.allPermissionsGranted
                    )
                ) {
                    uiState.selectedCoordinates?.let { coordinates ->
                        val markerState = rememberUpdatedMarkerState(position = coordinates)
                        Marker(
                            state = markerState,
                            snippet = uiState.addressInput,
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun CarFormScreenAddPreview() {
    CarFormScreen(
        title = R.string.car_form_add_title_top_bar,
        onBackButtonPress = {},
        bottomButtonText = R.string.btn_label_add_car
    )
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
fun CarFormScreenEditPreview() {
    CarFormScreen(
        title = R.string.car_form_edit_title_top_bar,
        onBackButtonPress = {},
        bottomButtonText = R.string.btn_label_edit_car
    )
}
