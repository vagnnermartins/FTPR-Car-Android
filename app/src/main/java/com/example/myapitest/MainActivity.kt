package com.example.myapitest

import HomeScreen
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapitest.domain.model.AuthState
import com.example.myapitest.presentation.ui.addcar.AddCarScreen
import com.example.myapitest.presentation.ui.auth.LoginScreen
import com.example.myapitest.presentation.ui.cardetail.CarDetailScreen
import com.example.myapitest.presentation.viewmodel.AuthViewModel
import com.example.myapitest.presentation.viewmodel.CarViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object CarDetail : Screen("car_details")
}

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val carViewModel: CarViewModel by viewModels()

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            authViewModel.signInWithGoogle(account)
        } catch (e: ApiException) {
            Log.w("MainActivity", "Google sign in failed", e)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyApiTestApp(
                authViewModel = authViewModel,
                carViewModel = carViewModel,
                onGoogleSignIn = { startGoogleSignIn() },
                activity = this
            )
        }
    }

    private fun startGoogleSignIn() {
        try {
            val webClientId = "CHANGE_ME"

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()

            val googleSignInClient = GoogleSignIn.getClient(this, gso)
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)

        } catch (e: Exception) {
            Log.e("MainActivity", "Google Sign-In configuration error: ${e.message}")
            Log.e("MainActivity", "Make sure you have google-services.json configured properly")
        }
    }
}

@Composable
fun MyApiTestApp(
    authViewModel: AuthViewModel,
    carViewModel: CarViewModel,
    onGoogleSignIn: () -> Unit,
    activity: MainActivity
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.observeAsState()

    val currentUser = FirebaseAuth.getInstance().currentUser
    val startDestination = if (currentUser != null) Screen.Home.route else Screen.Login.route

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }
            }

            is AuthState.SignedOut -> {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                }
            }

            else -> { /* Handle other states */
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onGoogleSignIn = onGoogleSignIn,
                activity = activity
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                authViewModel = authViewModel,
                onSignOut = {
                    authViewModel.signOut()
                },
                onCarClick = { carId ->
                    navController.navigate("car_detail/$carId")
                },
                onAddCarClick = {
                    navController.navigate("add_car")
                },
                onDeleteCarClick = { id ->
                    carViewModel.deleteCar(id)
                }
            )
        }

        composable(
            "car_detail/{carId}",
            arguments = listOf(navArgument("carId") { type = NavType.StringType })
        ) { backStackEntry ->
            val carId = backStackEntry.arguments?.getString("carId") ?: ""
            CarDetailScreen(
                carId = carId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("add_car") {
            AddCarScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onCarAdded = {
                    navController.popBackStack()
                }
            )
        }

    }
}