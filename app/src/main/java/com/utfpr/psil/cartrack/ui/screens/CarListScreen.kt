package com.utfpr.psil.cartrack.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.utfpr.psil.cartrack.ui.components.CarListItem
import com.utfpr.psil.cartrack.ui.model.CarListUiState
import com.utfpr.psil.cartrack.ui.viewmodels.AuthViewModel
import com.utfpr.psil.cartrack.ui.viewmodels.CarListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarListScreen(
    authViewModel: AuthViewModel,
    onLogoutButtonPress: () -> Unit,
    onAddNewCarButtonPress: () -> Unit,
    onCarClick: (String) -> Unit
) {
    val carListViewModel: CarListViewModel = hiltViewModel()
    val carListState by carListViewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        carListViewModel.fetchCars()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.statusBars,
                title = { Text("Veículos") },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("SAIR")
                        IconButton(
                            onClick = {
                                authViewModel.signOut(context)
                                onLogoutButtonPress()
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                        }
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
        }, floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNewCarButtonPress,
                containerColor = Color(0xff1e1f57),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { innerPadding ->
        when (val state = carListState) {
            is CarListUiState.Loading -> {
                Box {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(50.dp),
                        color = Color(0xff1e1f57)
                    )
                }
            }

            is CarListUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.layout.Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = state.message,
                            color = Color.Red,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        androidx.compose.material3.Button(
                            onClick = { carListViewModel.fetchCars() },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = Color(0xff1e1f57)
                            )
                        ) {
                            Text("Tentar Novamente", color = Color.White)
                        }
                    }
                }
            }

            is CarListUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally

                ) {
                    items(state.cars) { car ->
                        CarListItem(
                            carName = car.name,
                            carYear = car.year,
                            carPlate = car.licence,
                            imageUrl = car.imageUrl,
                            onClick = { car.id?.let { onCarClick(it) } },
                            onDeleteClick = { car.id?.let { carListViewModel.onDeleteCar(it) } }
                        )
                    }
                }
            }

        }
    }
}
