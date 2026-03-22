package com.example.myapitest

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapitest.models.Car
import com.example.myapitest.ui.theme.CarLocationAppTheme
import com.squareup.picasso.Picasso

class ListActivity : ComponentActivity() {
    
    private val viewModel: CarViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CarLocationAppTheme {
                val context = LocalContext.current
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text("Lista de Carros") }
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = {
                            context.startActivity(Intent(context, AddCarActivity::class.java))
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Adicionar Carro")
                        }
                    }
                ) { innerPadding ->
                    CarListScreen(modifier = Modifier.padding(innerPadding), viewModel = viewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchCars()
    }
}

@Composable
fun CarListScreen(
    modifier: Modifier = Modifier,
    viewModel: CarViewModel
) {
    val cars = viewModel.cars
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage

    Box(modifier = modifier.fillMaxSize()) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else if (errorMessage != null) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
                Button(onClick = { viewModel.fetchCars() }) {
                    Text("Tentar Novamente")
                }
            }
        } else if (cars.isEmpty()) {
            Text(
                text = "Nenhum carro encontrado.",
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cars) { car ->
                    CarItem(car = car)
                }
            }
        }
    }
}

@Composable
fun CarItem(car: Car) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AndroidView(
                modifier = Modifier
                    .size(100.dp)
                    .padding(4.dp),
                factory = { context ->
                    ImageView(context).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                    }
                },
                update = { imageView ->
                    Picasso.get()
                        .load(car.imageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_report_image)
                        .into(imageView)
                }
            )

            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = car.name ?: "Modelo Desconhecido",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Ano: ${car.year ?: "N/A"}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Placa: ${car.licence ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Localização: Long: ${car.place?.long ?: "N/A"} Lat: ${car.place?.lat ?: "N/A"},",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
