package com.example.myapitest.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapitest.ui.authenticaded.HomeScreen
import com.example.myapitest.ui.login.loginForm.LoginScreen
import com.example.myapitest.ui.login.loginForm.LoginViewModel
import com.example.myapitest.ui.login.verificationCode.VerificationCodeScreen

const val SCREEN_TRANSITION_MILLIS = 500

@Composable
fun MyApiTest(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val viewModel: LoginViewModel = viewModel()
    val isUserSignedIn = viewModel.checkAuthState()
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = if (isUserSignedIn) "home" else "login",
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start, tween(
                    SCREEN_TRANSITION_MILLIS
                )
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start, tween(
                    SCREEN_TRANSITION_MILLIS
                )
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End, tween(
                    SCREEN_TRANSITION_MILLIS
                )
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End, tween(
                    SCREEN_TRANSITION_MILLIS
                )
            )
        }
    ) {
        composable(
            route = "login",
        ) {
            LoginScreen(
                onContinuePressed = {
                    navController.navigate("verificationCode")
                },
                viewModel = viewModel
            )
        }
        composable(
            route = "verificationCode"
        ) {
            VerificationCodeScreen(
                onContinuePressed = {
                    navController.navigate("home")
                },
                viewModel = viewModel
            )
        }
        composable(
            route = "home"
        ) {
            HomeScreen(
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}