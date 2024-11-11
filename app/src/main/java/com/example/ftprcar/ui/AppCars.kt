package com.example.ftprcar.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ftprcar.ui.car.detail.CarDetailScreen
import com.example.ftprcar.ui.car.form.CarFormScreen
import com.example.ftprcar.ui.login.FirebaseAuthApp

private object Screens {
    const val HOME = "home"
    const val CAR_FORM = "carForm"
    const val CAR_DETAIL = "carDetail"
}

object Arguments {
    const val CAR_ID = "carId"
    const val CAR = "car"
}

private object Routes {
    const val HOME = Screens.HOME
    const val CAR_FORM = Screens.CAR_FORM
    const val CAR_DETAIL = "${Screens.CAR_DETAIL}?${Arguments.CAR_ID}={${Arguments.CAR_ID}}"
}

@Composable
fun AppCars(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screens.HOME
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(route = Routes.HOME) {
            FirebaseAuthApp(
                onAddCarPressed = { navController.navigate(Screens.CAR_FORM) },
                onCarPressed = { car -> navController.navigate("${Screens.CAR_DETAIL}?${Arguments.CAR_ID}=${car.id}") },
            )
        }
        composable(route = Routes.CAR_FORM) {
            CarFormScreen(onBackPressed = { navController.popBackStack() })
        }
        composable(
            route = Routes.CAR_DETAIL,
            arguments = listOf(
                navArgument(name = Arguments.CAR_ID) { type = NavType.StringType; nullable = true },
            )
        ) { backStackEntry ->
            CarDetailScreen(
                onBackPressed = { navController.popBackStack() },
                navController = navController,
                carId = backStackEntry.arguments?.getString(Arguments.CAR_ID) ?: ""
            )
        }
    }
}