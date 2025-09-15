package com.example.myapitest.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object Home : Screen("home_screen")
    object Cars : Screen("cars_screen")
    object Profile : Screen("profile_screen")

    object CarDetail : Screen("car_detail/{carId}") {
        fun createRoute(carId: String) = "car_detail/$carId"
    }

    object AddEditCar : Screen("add_edit_car?carId={carId}") {
        fun createRoute(carId: String?) = carId?.let { "add_edit_car?carId=$it" } ?: "add_edit_car"
    }
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem("Início", Icons.Default.Home, Screen.Home.route),
    BottomNavItem("Perfil", Icons.Default.Person, Screen.Profile.route),
    BottomNavItem("Carros", Icons.Filled.DirectionsCar, Screen.Cars.route)
)