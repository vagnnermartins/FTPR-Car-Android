package com.example.myapitest.ui.authenticaded.cars

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapitest.ui.authenticaded.cars.addCar.AddCarScreen
import com.example.myapitest.ui.authenticaded.cars.carDetails.CarDetailsScreen
import com.example.myapitest.ui.authenticaded.cars.carsLIst.Car
import com.example.myapitest.ui.authenticaded.cars.carsLIst.CarsScreen

@Composable
fun Cars(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    onBottomBarVisibilityChanged: (Boolean) -> Unit = {}
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = "carsList",
    ) {
        composable("carsList") {
            LaunchedEffect(Unit) {
                onBottomBarVisibilityChanged(true)
            }
            CarsScreen(
                navController = navController,
            )
        }
        composable("addCar") {
            LaunchedEffect(Unit) {
                onBottomBarVisibilityChanged(false)
            }
            AddCarScreen(
                onBackPressed = { navController.popBackStack() }
            )
        }
        composable(
            route = "carDetails/{carId}",
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStackEntry ->
            LaunchedEffect(Unit) {
                onBottomBarVisibilityChanged(false)
            }

            val carId = backStackEntry.arguments?.getString("carId") ?: ""

            CarDetailsScreen(
                carId = carId,
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }
    }
}