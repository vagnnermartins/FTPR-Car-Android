import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapitest.domain.model.Car
import com.example.myapitest.presentation.viewmodel.AuthViewModel
import com.example.myapitest.presentation.viewmodel.CarViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    onSignOut: () -> Unit,
    onCarClick: (String) -> Unit,
    onAddCarClick: () -> Unit,
    carViewModel: CarViewModel = viewModel(),
    onDeleteCarClick: (String) -> Unit,
) {
    val user = FirebaseAuth.getInstance().currentUser
    val cars by carViewModel.cars.observeAsState(emptyList())
    val loading by carViewModel.loading.observeAsState(false)
    val error by carViewModel.error.observeAsState()

    val carsList by remember {
        derivedStateOf { cars.toList() }
    }

    LaunchedEffect(Unit) {
        carViewModel.loadCars()
    }

    val handleDeleteCar = { carId: String ->
        carViewModel.deleteCar(carId)
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCarClick
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Adicionar Carro"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Bem-vindo!",
                    style = MaterialTheme.typography.headlineLarge
                )

                user?.let {
                    Text(
                        text = "Logado com: ${it.phoneNumber ?: "Usuário"}",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Button(
                    onClick = onSignOut,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    Text("Sair")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Meus Carros",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when {
                loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                error != null -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Erro: $error",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = { carViewModel.loadCars() },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text("Tentar Novamente")
                        }
                    }
                }

                cars.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhum carro cadastrado",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(carsList, key = { it.id }) { car ->
                            CarItem(
                                car = car,
                                onCarClick = onCarClick,
                                onDeleteCarClick = handleDeleteCar
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CarItem(car: Car, onCarClick: (String) -> Unit, onDeleteCarClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onCarClick(car.id) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row {
                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = car.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Ano: ${car.year}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Text(
                        text = "Placa: ${car.licence}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                IconButton(
                    onClick = { onDeleteCarClick(car.id) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir carro",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}