package com.example.myapitest.ui.screens

import android.Manifest
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myapitest.ui.viewmodel.AddEditCarViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.material.icons.filled.AddAPhoto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCarScreen(
    navController: NavController,
    viewModel: AddEditCarViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current


    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                viewModel.fetchCurrentUserLocation(context)
            } else {
                Toast.makeText(context, "Permissão de localização negada", Toast.LENGTH_SHORT).show()
            }
        }
    )
    LaunchedEffect(Unit) {
        if (!viewModel.uiState.value.isEditing && viewModel.uiState.value.location == null) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    LaunchedEffect(uiState.isEditing) {
        if (!uiState.isEditing) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            uri?.let { viewModel.onImagePicked(it) }
        }
    )

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.screenTitle) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                ImagePicker(
                    imageUri = uiState.imageUri,
                    existingImageUrl = uiState.imageUrl,
                    error = uiState.imageError,
                    onClick = {
                        imagePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Nome (Marca e Modelo)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.nameError != null,
                    supportingText = { uiState.nameError?.let { Text(it) } }
                )
            }
            item {
                OutlinedTextField(
                    value = uiState.year,
                    onValueChange = viewModel::onYearChange,
                    label = { Text("Ano") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = uiState.yearError != null,
                    supportingText = { uiState.yearError?.let { Text(it) } }
                )
            }
            item {
                OutlinedTextField(
                    value = uiState.licence,
                    onValueChange = viewModel::onLicenceChange,
                    label = { Text("Placa") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.licenceError != null,
                    supportingText = { uiState.licenceError?.let { Text(it) } }
                )
            }

            item {
                ClickableMap(
                    location = uiState.location,
                    onLocationChange = viewModel::onLocationChange,
                    error = uiState.locationError
                )
            }

            item {
                Button(
                    onClick = { viewModel.saveCar() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp)) else Text(
                        "Salvar Carro"
                    )
                }
            }
        }
    }
}

@Composable
fun ImagePicker(imageUri: Uri?, existingImageUrl: String, error: String?, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val imageModifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.5f
                ),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)

        Box(modifier = imageModifier, contentAlignment = Alignment.Center) {
            val imageToShow =
                imageUri ?: if (existingImageUrl.isNotBlank()) existingImageUrl else null
            if (imageToShow == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.AddAPhoto,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp)
                    )
                    Text("Toque para adicionar uma imagem")
                }
            } else {
                AsyncImage(
                    model = imageToShow,
                    contentDescription = "Imagem do Carro",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        error?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
fun ClickableMap(location: LatLng?, onLocationChange: (LatLng) -> Unit, error: String?) {
    val defaultCameraPosition = LatLng(-23.5505, -46.6333)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            location ?: defaultCameraPosition,
            10f
        )
    }

    LaunchedEffect(location) {
        location?.let {
            cameraPositionState.animate(
                com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(it, 15f)
            )
        }
    }

    Column {
        Text(
            "Localização",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    1.dp,
                    if (error != null) MaterialTheme.colorScheme.error else Color.Transparent,
                    RoundedCornerShape(12.dp)
                ),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(zoomControlsEnabled = false),
            onMapClick = { latLng ->
                onLocationChange(latLng)
            }
        ) {
            location?.let {
                Marker(
                    state = MarkerState(position = it),
                    title = "Localização Selecionada"
                )
            }
        }
        error?.let {
            Text(
                it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}