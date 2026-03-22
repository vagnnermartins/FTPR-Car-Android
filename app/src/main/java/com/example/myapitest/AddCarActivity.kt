package com.example.myapitest

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapitest.models.Car
import com.example.myapitest.models.Place
import com.example.myapitest.ui.theme.CarLocationAppTheme
import java.io.File
import java.util.UUID

class AddCarActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CarLocationAppTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Adicionar Carro") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar")
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    AddCarScreen(
                        modifier = Modifier.padding(innerPadding),
                        onSuccess = { finish() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCarScreen(
    modifier: Modifier = Modifier,
    viewModel: CarViewModel = viewModel(),
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    var carId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var licence by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            isSaving = true
            viewModel.uploadImageToFirebase(tempImageUri!!) { url ->
                isSaving = false
                if (url != null) imageUrl = url
                else Toast.makeText(context, "Erro no upload", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(value = carId, onValueChange = { carId = it }, label = { Text("ID") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Ano") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = licence, onValueChange = { licence = it }, label = { Text("Placa") }, modifier = Modifier.fillMaxWidth())

        Button(
            onClick = {
                val file = File(context.externalCacheDir, "${UUID.randomUUID()}.jpg")
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                tempImageUri = uri
                cameraLauncher.launch(uri)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Tirar Foto")
        }

        OutlinedTextField(value = imageUrl, onValueChange = {}, label = { Text("URL da Imagem") }, modifier = Modifier.fillMaxWidth(), readOnly = true)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(value = latitude, onValueChange = { latitude = it }, label = { Text("Lat") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = longitude, onValueChange = { longitude = it }, label = { Text("Long") }, modifier = Modifier.weight(1f))
        }

        Button(
            onClick = {
                isSaving = true
                val car = Car(carId, imageUrl, year, name, licence, Place(latitude.toDoubleOrNull() ?: 0.0, longitude.toDoubleOrNull() ?: 0.0))
                viewModel.saveCar(car) { error ->
                    isSaving = false
                    if (error == null) onSuccess()
                    else Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving && imageUrl.isNotBlank()
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Salvar")
            }
        }
    }
}