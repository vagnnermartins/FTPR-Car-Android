package com.example.myapitest.presentation.ui.cardetail

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.example.myapitest.data.repository.ImageUploadRepository
import com.example.myapitest.domain.model.Car
import com.example.myapitest.domain.model.Place
import com.example.myapitest.presentation.ui.components.LocationPickerMap
import com.example.myapitest.presentation.viewmodel.CarDetailViewModel
import com.example.myapitest.presentation.viewmodel.CarViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.Calendar

@Composable
fun YearPickerDialog(
    showDialog: Boolean,
    currentYear: String,
    onDismiss: () -> Unit,
    onYearSelected: (String) -> Unit
) {
    if (showDialog) {
        val currentCalendarYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = (1900..currentCalendarYear + 5).map { it.toString() }.reversed()

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Selecionar Ano") },
            text = {
                LazyColumn(
                    modifier = Modifier.height(300.dp)
                ) {
                    items(years.size) { index ->
                        val year = years[index]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = year == currentYear,
                                    onClick = {
                                        onYearSelected(year)
                                        onDismiss()
                                    }
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = year == currentYear,
                                onClick = {
                                    onYearSelected(year)
                                    onDismiss()
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = year,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarDetailScreen(
    carId: String,
    onBackClick: () -> Unit,
    carDetailViewModel: CarDetailViewModel = viewModel(),
    carViewModel: CarViewModel = viewModel()
) {
    val car by carDetailViewModel.car.observeAsState()
    val loading by carDetailViewModel.loading.observeAsState(false)
    val error by carDetailViewModel.error.observeAsState()
    val scrollState = rememberScrollState()

    var isEditing by remember { mutableStateOf(false) }
    var hasChanges by remember { mutableStateOf(false) }
    var showYearPicker by remember { mutableStateOf(false) }

    var editName by remember { mutableStateOf("") }
    var editYear by remember { mutableStateOf("") }
    var editLicence by remember { mutableStateOf("") }
    var editImageUrl by remember { mutableStateOf("") }
    var editLocation by remember { mutableStateOf<LatLng?>(null) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var firebaseImageUrl by remember { mutableStateOf("") }
    var imageUploading by remember { mutableStateOf(false) }
    var imageUploadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(car) {
        car?.let {
            editName = it.name
            editYear = it.year
            editLicence = it.licence
            editImageUrl = it.imageUrl
            editLocation = LatLng(it.place.lat, it.place.long)
            firebaseImageUrl = it.imageUrl
        }
    }

    LaunchedEffect(editName, editYear, editLicence, editImageUrl, editLocation) {
        car?.let { currentCar ->
            hasChanges = editName != currentCar.name ||
                    editYear != currentCar.year ||
                    editLicence != currentCar.licence ||
                    editImageUrl != currentCar.imageUrl ||
                    editLocation?.latitude != currentCar.place.lat ||
                    editLocation?.longitude != currentCar.place.long
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
                    editImageUrl = url
                    imageUploadError = null
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

    LaunchedEffect(carId) {
        carDetailViewModel.loadCarDetails(carId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Carro" else "Detalhes do Carro") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    if (car != null) {
                        IconButton(
                            onClick = {
                                if (isEditing) {
                                    car?.let {
                                        editName = it.name
                                        editYear = it.year
                                        editLicence = it.licence
                                        editImageUrl = it.imageUrl
                                        editLocation = LatLng(it.place.lat, it.place.long)
                                        firebaseImageUrl = it.imageUrl
                                        selectedImageUri = null
                                        imageUploadError = null
                                    }
                                }
                                isEditing = !isEditing
                            }
                        ) {
                            Icon(
                                imageVector = if (isEditing) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Edit,
                                contentDescription = if (isEditing) "Cancelar" else "Editar"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Erro: $error",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = { carDetailViewModel.loadCarDetails(carId) },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Tentar novamente")
                        }
                    }
                }
            }

            car != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    if (isEditing) {
                        Text(
                            text = "Imagem do Carro",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
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
                                containerColor = MaterialTheme.colorScheme.surface
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
                                            contentDescription = "Imagem do carro",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop,
                                            loading = {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    CircularProgressIndicator()
                                                }
                                            }
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "Toque para alterar imagem",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    else -> {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ShoppingCart,
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

                        imageUploadError?.let { errorMsg ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text(
                                    text = errorMsg,
                                    modifier = Modifier.padding(16.dp),
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Nome do Carro") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = editYear,
                            onValueChange = { },
                            label = { Text("Ano") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showYearPicker = true },
                            enabled = false,
                            placeholder = { Text("Selecione o ano") },
                            trailingIcon = {
                                IconButton(onClick = { showYearPicker = true }) {
                                    Icon(
                                        Icons.Default.DateRange,
                                        contentDescription = "Selecionar ano"
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = editLicence,
                            onValueChange = { editLicence = it },
                            label = { Text("Placa") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = editImageUrl,
                            onValueChange = { editImageUrl = it },
                            label = { Text("URL da Imagem") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = true,
                            placeholder = { Text("URL será preenchida automaticamente após upload") }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Localização",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LocationPickerMap(
                            selectedLocation = editLocation,
                            onLocationSelected = { latLng ->
                                editLocation = latLng
                            },
                            initialLocation = LatLng(car!!.place.lat, car!!.place.long)
                        )

                    } else {
                        SubcomposeAsyncImage(
                            model = car!!.imageUrl,
                            contentDescription = "Imagem do ${car!!.name}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop,
                            loading = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            },
                            error = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ShoppingCart,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "Imagem não disponível",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = car!!.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        DetailCard(title = "Ano", value = car!!.year)
                        Spacer(modifier = Modifier.height(8.dp))

                        DetailCard(title = "Placa", value = car!!.licence)
                        Spacer(modifier = Modifier.height(8.dp))

                        DetailCard(
                            title = "Localização",
                            value = "Lat: ${car!!.place.lat}, Long: ${car!!.place.long}"
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Localização no Mapa",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        CarLocationMap(
                            latitude = car!!.place.lat,
                            longitude = car!!.place.long,
                            carName = car!!.name
                        )
                    }

                    if (isEditing && hasChanges) {
                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                editLocation?.let { location ->
                                    val finalImageUrl = firebaseImageUrl.ifEmpty { editImageUrl }

                                    val updatedCar = Car(
                                        id = car!!.id,
                                        name = editName,
                                        year = editYear,
                                        licence = editLicence,
                                        imageUrl = finalImageUrl,
                                        place = Place(
                                            lat = location.latitude,
                                            long = location.longitude
                                        )
                                    )
                                    carDetailViewModel.updateCar(car!!.id, updatedCar)
                                    carViewModel.loadCars()
                                    carDetailViewModel.loadCarDetails(carId)

                                    isEditing = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = editName.isNotBlank() &&
                                    editYear.isNotBlank() &&
                                    editLicence.isNotBlank() &&
                                    editImageUrl.isNotBlank() &&
                                    editLocation != null &&
                                    !imageUploading
                        ) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Salvar Alterações")
                        }
                    }

                    YearPickerDialog(
                        showDialog = showYearPicker,
                        currentYear = editYear,
                        onDismiss = { showYearPicker = false },
                        onYearSelected = { selectedYear ->
                            editYear = selectedYear
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CarLocationMap(latitude: Double, longitude: Double, carName: String) {
    val carPosition = LatLng(latitude, longitude)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(carPosition, 15f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState) {
            Marker(
                state = MarkerState(position = carPosition),
                title = carName,
                snippet = "Localização do carro"
            )
        }
    }
}

@Composable
private fun DetailCard(title: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}