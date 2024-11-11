package com.example.ftprcar.ui.login


import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.ftprcar.model.Car
import com.example.ftprcar.ui.car.list.CarListScreen
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

@Composable
fun FirebaseAuthApp(onAddCarPressed: () -> Unit, onCarPressed: (Car) -> Unit) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    var user by remember { mutableStateOf(auth.currentUser) }

    if (user != null) {
        CarListScreen(
            onAddPressed = onAddCarPressed,
            onCarPressed = onCarPressed,
            onSignOut = {
                AuthUI.getInstance().signOut(context)
                user = null
            }
        )
    } else {
        FirebaseAuthUIExample { signedInUser ->
            user = signedInUser
        }
    }
}

@Composable
fun FirebaseAuthUIExample(onSignIn: (FirebaseUser?) -> Unit) {
    val auth = FirebaseAuth.getInstance()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val user = auth.currentUser
            onSignIn(user)
        } else {
            val response = IdpResponse.fromResultIntent(result.data)
            if (response == null) {
                println("Sign-in canceled")
            } else {
                println("Sign-in error: ${response.error?.errorCode}")
            }
        }
    }

    // Primeira vez que abrir a tela, inicia o processo de sign-in
    LaunchedEffect(Unit) {
        launcher.launch(createSignInIntent())
    }

    Row(
        modifier = Modifier
            .fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { launcher.launch(createSignInIntent()) }
        ) {
            Text("Abrir tela de login")
        }
    }

}

fun createSignInIntent(): Intent {
    val providers = arrayListOf(
        AuthUI.IdpConfig.GoogleBuilder().build(),
        AuthUI.IdpConfig.PhoneBuilder()
            .setWhitelistedCountries(listOf("+55"))
//            .setDefaultNumber("+55 11 91234-5678")
            .build()
    )

    return AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(providers)
        .setIsSmartLockEnabled(false)
        .build()
}
