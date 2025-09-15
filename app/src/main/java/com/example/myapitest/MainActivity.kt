package com.example.myapitest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapitest.ui.navigation.Screen
import com.example.myapitest.ui.screens.LoginScreen
import com.example.myapitest.ui.screens.MainScreen
import com.example.myapitest.ui.theme.MyApiTestTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val startDestination = if (auth.currentUser != null)
            "main_screen"
        else
            Screen.Login.route


        setContent {
            MyApiTestTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable(route = Screen.Login.route) {
                            LoginScreen(navController = navController)
                        }

                        composable(route = "main_screen") {
                            MainScreen(rootNavController = navController)
                        }
                    }
                }
            }
        }
    }
}