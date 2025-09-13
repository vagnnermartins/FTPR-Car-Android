package com.example.myapitest.presentation.ui.addcar

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.example.myapitest.data.repository.ImageUploadRepository
import com.example.myapitest.domain.model.Car
import com.example.myapitest.domain.model.Place
import com.example.myapitest.presentation.ui.components.LocationPickerMap
import com.example.myapitest.presentation.viewmodel.CarViewModel
import com.google.android.gms.maps.model.LatLng
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCarScreen(
    onBackClick: () -> Unit,
    onCarAdded: () -> Unit,
    carViewModel: CarViewModel = viewModel()
) {
    var carId by remember { mutableStateOf("") }
    var carName by remember { mutableStateOf("") }
    var carYear by remember { mutableStateOf("") }
    var carLicence by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }

    var showYearPicker by remember { mutableStateOf(false) }

    val loading by carViewModel.loading.observeAsState(false)
    val error by carViewModel.error.observeAsState()
    val car by carViewModel.car.observeAsState()
    val scrollState = rememberScrollState()

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var firebaseImageUrl by remember { mutableStateOf("") }
    var imageUploading by remember { mutableStateOf(false) }
    var imageUploadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(car) {
        car?.let {
            onCarAdded()
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            imageUploadError = null
        }
    }

    LaunchedEffect(selectedImageUri) {
        selectedImageUri?.let { uri ->
            imageUploading = true
            try {
                val imageUploadRepository = ImageUploadRepository()
                val result = imageUploadRepository.uploadCarImage(uri)
                result.onSuccess { url ->
                    firebaseImageUrl = url
                    imageUploadError = null
                    imageUrl = url
                }.onFailure { exception ->
                    imageUploadError = "Erro ao enviar imagem: ${exception.message}"
                }
            } catch (e: Exception) {
                imageUploadError = "Erro inesperado: ${e.message}"
            } finally {
                imageUploading = false
            }
        }
    }

    if (showYearPicker) {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (1950..currentYear + 5).toList().reversed()

        AlertDialog(
            onDismissRequest = { showYearPicker = false },
            title = { Text("Selecionar Ano") },
            text = {
                LazyColumn(
                    modifier = Modifier.height(300.dp)
                ) {
                    items(years.size) { index ->
                        val year = years[index]
                        TextButton(
                            onClick = {
                                carYear = year.toString()
                                showYearPicker = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = year.toString(),
                                style = if (year == currentYear)
                                    MaterialTheme.typography.bodyLarge.copy(
                                        color = MaterialTheme.colorScheme.primary
                                    ) else MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showYearPicker = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adicionar Carro") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = carId,
                onValueChange = { carId = it },
                label = { Text("ID do Carro") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading
            )

            OutlinedTextField(
                value = carName,
                onValueChange = { carName = it },
                label = { Text("Nome do Carro") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading
            )

            OutlinedTextField(
                value = carYear,
                onValueChange = { },
                label = { Text("Ano") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showYearPicker = true },
                enabled = false,
                placeholder = { Text("Selecione o ano") },
                trailingIcon = {
                    IconButton(onClick = { showYearPicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Selecionar ano")
                    }
                }
            )

            OutlinedTextField(
                value = carLicence,
                onValueChange = { carLicence = it },
                label = { Text("Placa (ex: ABC-1234)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading
            )

            Text(
                text = "Imagem do Carro *",
                style = MaterialTheme.typography.titleMedium
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable {
                        if (!imageUploading) {
                            imagePickerLauncher.launch("image/*")
                        }
                    },
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (firebaseImageUrl.isEmpty())
                        MaterialTheme.colorScheme.surfaceVariant
                    else MaterialTheme.colorScheme.surface
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        imageUploading -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator()
                                Text(
                                    text = "Enviando para Firebase...",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }

                        firebaseImageUrl.isNotEmpty() -> {
                            SubcomposeAsyncImage(
                                model = firebaseImageUrl,
                                contentDescription = "Imagem selecionada",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        else -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBox,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Toque para selecionar imagem",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                Text(
                                    text = "Será salva no Firebase Storage",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            LocationPickerMap(
                selectedLocation = selectedLocation,
                onLocationSelected = { latLng ->
                    selectedLocation = latLng
                }
            )

            error?.let {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "Erro: $it",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Button(
                onClick = {
                    if (carId.isNotBlank() && carName.isNotBlank() && carYear.isNotBlank() &&
                        carLicence.isNotBlank() && imageUrl.isNotBlank()
                        && selectedLocation != null
                    ) {
                        val finalImageUrl = firebaseImageUrl.ifEmpty { imageUrl }

                        val newCar = Car(
                            id = carId,
                            name = carName,
                            year = carYear,
                            licence = carLicence,
                            imageUrl = finalImageUrl,
                            place = Place(
                                lat = selectedLocation!!.latitude,
                                long = selectedLocation!!.longitude
                            )
                        )
                        carViewModel.addCar(newCar)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !loading && carId.isNotBlank() && carName.isNotBlank() &&
                        carYear.isNotBlank() && carLicence.isNotBlank() &&
                        imageUrl.isNotBlank() && selectedLocation != null
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Adicionar Carro")
                }
            }
        }
    }
}