package com.example.myapitest

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthViewModel : ViewModel() {
    private val auth = Firebase.auth
    var currentUser by mutableStateOf(auth.currentUser)
        private set
    var isLoading by mutableStateOf(false)

    init {
        currentUser = auth.currentUser
    }

    fun updateUserData() {
        currentUser = auth.currentUser
    }

    fun login(email: String, psw: String, onResult: (Boolean) -> Unit) {
        isLoading = true
        auth.signInWithEmailAndPassword(email, psw).addOnCompleteListener {
            isLoading = false
            onResult(it.isSuccessful)
        }
    }

    fun loginWithGoogle(token: String, onResult: (Boolean) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(token, null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                currentUser = auth.currentUser
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    fun logout(googleSignInClient: GoogleSignInClient) {
        auth.signOut()
        googleSignInClient.signOut().addOnCompleteListener {
            currentUser = null
        }
    }
}
