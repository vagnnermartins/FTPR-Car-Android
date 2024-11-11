package com.example.ftprcar.ui.car.form

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ftprcar.model.Car
import com.example.ftprcar.model.Place
import com.example.ftprcar.ui.car.list.CarListViewModel
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarFormScreen(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},  // Callback function to navigate back
    viewModel: CarListViewModel = viewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.processed) {
        if (state.processed) {
            onBackPressed()
        }
    }
    LaunchedEffect(snackbarHostState, state.error) {
        state.error
            .takeIf { it.isNotBlank() }
            ?.let {
                snackbarHostState.showSnackbar(it)
                viewModel.clearError()
            }
    }
    val onSave = { newCar: Car, imageUri: Uri? -> viewModel.addCar(newCar, imageUri) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = { AppBar(onBackPressed = onBackPressed) }
    ) { paddingValues ->
        FormContent(
            modifier = modifier.padding(paddingValues),
            onSave = onSave,
            loading = state.loading
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBar(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit,
) {
    TopAppBar(
        modifier = modifier.fillMaxWidth(),
        title = { Text("Add Car") },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
            navigationIconContentColor = MaterialTheme.colorScheme.primary,
            actionIconContentColor = MaterialTheme.colorScheme.primary
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormContent(
    modifier: Modifier = Modifier,
    car: Car? = null,
    onSave: (Car, Uri?) -> Unit,
    loading: Boolean = false
) {
    var carId by remember { mutableStateOf(car?.id ?: Random.nextInt(100000).toString()) }
    var imageUrl by remember { mutableStateOf(car?.imageUrl ?: "") }
    var year by remember { mutableStateOf(car?.year ?: "") }
    var name by remember { mutableStateOf(car?.name ?: "") }
    var license by remember { mutableStateOf(car?.licence ?: "") }
    var lat by remember { mutableDoubleStateOf(car?.place?.lat ?: 0.0) }
    var long by remember { mutableDoubleStateOf(car?.place?.long ?: 0.0) }

    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        selectedFileUri = uri
        imageUrl = uri.toString()
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .imePadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = { Text("Image URL") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { filePickerLauncher.launch(arrayOf("image/*")) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text("Select a File")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = year,
            onValueChange = { year = it },
            label = { Text("Year") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = license,
            onValueChange = { license = it },
            label = { Text("License") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = lat.toString(),
            onValueChange = { lat = it.toDoubleOrNull() ?: 0.0 },
            label = { Text("Latitude") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = long.toString(),
            onValueChange = { long = it.toDoubleOrNull() ?: 0.0 },
            label = { Text("Longitude") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val newCar = Car(
                    id = carId,
                    imageUrl = imageUrl,
                    year = year,
                    name = name,
                    licence = license,
                    place = Place(lat, long)
                )
                onSave(newCar, selectedFileUri)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            Text(text = "Add Car")
        }
    }
}
