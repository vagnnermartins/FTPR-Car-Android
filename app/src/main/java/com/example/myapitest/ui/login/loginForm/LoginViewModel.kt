package com.example.myapitest.ui.login.loginForm

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.myapitest.MainActivity
import com.example.myapitest.network.FirebaseService
import com.example.myapitest.network.StatusService
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class LoginViewModel : ViewModel() {
    var phoneNumber by mutableStateOf("")
        private set

    var verificationCode by mutableStateOf("")
        private set

    var verificationId by mutableStateOf("")
        private set

    var loginState: StatusService<*> by mutableStateOf(StatusService.Idle)
        private set

    var isCodeSent by mutableStateOf(false)
        private set

    var isSignedIn by mutableStateOf(false)

    fun updatePhoneNumber(newPhoneNumber: String) {
        phoneNumber = newPhoneNumber
    }

    fun updateVerificationCode(newCode: String) {
        verificationCode = newCode
    }

    fun updateState(newState: StatusService<*>){
        loginState = newState
    }

    fun getIsUserSignedIn(): Boolean {
        return FirebaseService.isUserSignedIn()
    }

    fun sendVerificationCode(activity: MainActivity) {
        if (phoneNumber.isEmpty()) {
            loginState = StatusService.Error(1, "Please enter phone number")
            return
        }

        loginState = StatusService.Loading

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                loginState = StatusService.Loading
                FirebaseService.signInWithCredential(credential) { success, message ->
                    if (success) {
                        loginState = StatusService.Success("Phone authentication successful!")
                        isCodeSent = false
                    } else {
                        loginState = StatusService.Error(2, message ?: "Authentication failed")
                    }
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                loginState = StatusService.Error(3, e.message ?: "Verification failed")
                isCodeSent = false
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                this@LoginViewModel.verificationId = verificationId
                loginState = StatusService.Success("Verification code sent to $phoneNumber")
                isCodeSent = true
            }
        }

        try {
            FirebaseService.sendVerificationCode(phoneNumber, activity, callbacks)
        } catch (e: Exception) {
            loginState = StatusService.Error(4, e.message ?: "Failed to send verification code")
        }
    }

    fun verifyCode() {
        if (verificationId.isEmpty()) {
            loginState = StatusService.Error(5, "No verification ID available")
            return
        }

        if (verificationCode.isEmpty()) {
            loginState = StatusService.Error(6, "Please enter verification code")
            return
        }

        loginState = StatusService.Loading

        FirebaseService.verifyPhoneNumber(verificationId, verificationCode) { success, message ->
            if (success) {
                loginState = StatusService.Success("Phone authentication successful!")
                isCodeSent = false
            } else {
                loginState = StatusService.Error(7, message ?: "Invalid verification code")
            }
        }
    }

    fun resendCode(activity: MainActivity) {
        this.sendVerificationCode(activity)
    }

    fun signOut() {
        FirebaseService.signOut()
        resetState()
    }

    fun checkAuthState(): Boolean {
        return FirebaseService.isUserSignedIn()
    }

    private fun resetState() {
        phoneNumber = ""
        verificationCode = ""
        verificationId = ""
        isCodeSent = false
        loginState = StatusService.Idle
    }
}