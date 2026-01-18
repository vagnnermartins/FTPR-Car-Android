package com.utfpr.psil.cartrack.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.utfpr.psil.cartrack.R
import com.utfpr.psil.cartrack.ui.model.AppScreens
import com.utfpr.psil.cartrack.ui.screens.CarDetailsScreen
import com.utfpr.psil.cartrack.ui.screens.CarFormScreen
import com.utfpr.psil.cartrack.ui.screens.CarListScreen
import com.utfpr.psil.cartrack.ui.screens.LoginScreen
import com.utfpr.psil.cartrack.ui.screens.SmsCodeVerificationScreen
import com.utfpr.psil.cartrack.ui.screens.SmsNumberScreen
import com.utfpr.psil.cartrack.ui.viewmodels.AuthViewModel

@Composable
fun CarTrackApp() {

    val navController = rememberNavController()

    val authViewModel: AuthViewModel = hiltViewModel()
    val authUiState by authViewModel.uiAuthState.collectAsState()

    NavHost(
        navController,
        startDestination = if (authUiState.isUserLoggedIn) AppScreens.CarList.name else
            AppScreens.Login.name
    ) {

        composable(AppScreens.Login.name) {
            LoginScreen(
                authViewModel = authViewModel,
                onSuccessfullyLoginOnGoogle = { navController.navigate(AppScreens.CarList.name) },
                onSmsLogin = { navController.navigate(AppScreens.SmsNumber.name) }
            )
        }
        composable(AppScreens.SmsNumber.name) {
            SmsNumberScreen(
                authViewModel = authViewModel,
                onSendCodeClick = { navController.navigate(AppScreens.SmsCodeVerification.name) {popUpTo(0)} } ,
                onBackButtonPress = { navController.navigate(AppScreens.Login.name) }
            )
        }
        composable(AppScreens.SmsCodeVerification.name) {
            SmsCodeVerificationScreen(
                onBackButtonPress = { navController.navigate(AppScreens.Login.name) {popUpTo(0)} },
                onSuccessfulVerification = { navController.navigate(AppScreens.CarList.name) },
                authViewModel = authViewModel
            )
        }
        composable(AppScreens.CarList.name) {
            CarListScreen(
                onLogoutButtonPress = {
                    navController.navigate(AppScreens.Login.name) { popUpTo(0) }
                },
                onAddNewCarButtonPress = { navController.navigate(AppScreens.CarForm.name) },
                onCarClick = { carId ->
                    navController.navigate("${AppScreens.CarDetails.name}/$carId")
                },
                authViewModel = authViewModel
            )
        }
        composable(
            route = "${AppScreens.CarForm.name}?carId={carId}",
            arguments = listOf(
                navArgument("carId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId")

            CarFormScreen(
                title = if (carId == null) R.string.car_form_add_title_top_bar else R.string.car_form_edit_title_top_bar,
                bottomButtonText = if (carId == null) R.string.btn_label_add_car else R.string.btn_label_edit_car,
                onBackButtonPress = { navController.popBackStack() }
            )
        }
        composable(
            route = "${AppScreens.CarDetails.name}/{carId}",
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { _ ->
            CarDetailsScreen(
                onBackButtonPress = { navController.popBackStack() },
                onEditButtonPress = { carId ->
                    navController.navigate("${AppScreens.CarForm.name}?carId=$carId")
                },
            )
        }
    }
}