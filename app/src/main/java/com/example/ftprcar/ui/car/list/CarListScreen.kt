package com.example.ftprcar.ui.car.list

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ftprcar.model.Car
import com.example.ftprcar.ui.car.composables.CarAsyncImage
import com.example.ftprcar.ui.theme.MainTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarListScreen(
    onAddPressed: () -> Unit,
    onCarPressed: (Car) -> Unit,
    onSignOut: () -> Unit,
    viewModel: CarListViewModel = viewModel()
) {
    println("viewModel: ${viewModel.hashCode()}")
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (state.loading) {
        Text(text = "Loading...")
    } else if (state.error.isNotBlank()) {
        Text(text = state.error)
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                AppBar(
                    onRefreshPressed = { viewModel.fetchCars() },
                    onSignOutPressed = onSignOut
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = onAddPressed) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Car"
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.Center
        ) { paddingValues ->
            if (state.cars.isEmpty()) {
                Text(
                    modifier = Modifier.padding(paddingValues),
                    text = "No cars found"
                )
            } else {
                CarList(
                    modifier = Modifier.padding(paddingValues),
                    cars = state.cars,
                    onCarPressed = onCarPressed,
                    onPlacePressed = { car ->
                        Toast.makeText(
                            context,
                            "Place: Lat: ${car.place.lat}, Lng: ${car.place.long}",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBar(
    modifier: Modifier = Modifier,
    onRefreshPressed: () -> Unit,
    onSignOutPressed: () -> Unit
) {
    TopAppBar(
        title = { Text("My Garage") },
        modifier = modifier.fillMaxWidth(),
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onSecondary
        ),
        actions = {
            IconButton(onClick = onRefreshPressed) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Refresh"
                )
            }
            IconButton(onClick = onSignOutPressed) {
                Icon(
                    imageVector = Icons.Filled.ExitToApp,
                    contentDescription = "Sign Out"
                )
            }
        }
    )
}

@Composable
fun CarList(
    modifier: Modifier = Modifier,
    cars: List<Car>,
    onCarPressed: (Car) -> Unit,
    onPlacePressed: (Car) -> Unit
) {
    LazyColumn(modifier = modifier) {
        items(cars) { car ->
            CarListItem(
                car = car,
                onCarPressed = onCarPressed,
                onPlacePressed = onPlacePressed
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarListItem(car: Car, onCarPressed: (Car) -> Unit, onPlacePressed: (Car) -> Unit) {
    ListItem(
        modifier = Modifier.clickable { onCarPressed(car) },
        leadingContent = {
            CarAsyncImage(
                modifier = Modifier
                    .width(100.dp)
                    .padding(end = 8.dp),
                imageUrl = car.imageUrl,
                contentScale = ContentScale.Fit
            )
        },
        headlineText = { Text(car.name) },
        overlineText = { Text(car.year) },
        supportingText = { Text(car.licence) },
        trailingContent = {
            IconButton(onClick = { onPlacePressed(car) }) {
                Icon(
                    imageVector = Icons.Filled.Place,
                    contentDescription = "Place"
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MainTheme {
        CarListScreen(
            onAddPressed = {},
            onSignOut = {},
            onCarPressed = {}
        )
    }
}