package com.example.myapitest.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapitest.MainActivity
import com.example.myapitest.data.repository.AuthRepository
import com.example.myapitest.domain.model.AuthState
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _authState = MutableLiveData<AuthState>()

    val authState: LiveData<AuthState> = _authState

    private val _verificationId = MutableLiveData<String>()

    fun sendPhoneVerification(phoneNumber: String, activity: MainActivity) {
        _authState.value = AuthState.Loading

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _authState.value = AuthState.Error(e.message ?: "Verification failed")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                _verificationId.value = verificationId
                _authState.value = AuthState.CodeSent
            }
        }

        repository.sendVerificationCode(phoneNumber, activity, callbacks)
    }

    fun verifyCode(code: String) {
        _authState.value = AuthState.Loading
        val verificationId = _verificationId.value

        if (verificationId != null) {
            repository.verifyPhoneNumber(verificationId, code) { success, error ->
                if (success) {
                    _authState.value = AuthState.Success
                } else {
                    _authState.value =
                        AuthState.Error(error ?: "Nao conseguimos verificar o código")
                }
            }
        } else {
            _authState.value = AuthState.Error("Identificador de verificação nulo")
        }
    }

    fun signInWithPhoneCredential(credential: PhoneAuthCredential) {
        repository.signInWithCredential(credential) { success, error ->
            if (success) {
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error(error ?: "Sign-in falhou")
            }
        }
    }

    fun signInWithGoogle(account: GoogleSignInAccount) {
        _authState.value = AuthState.Loading
        repository.signInWithGoogle(account) { success, error ->
            if (success) {
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error(error ?: "Google sign in failed")
            }
        }
    }

    fun signOut() {
        repository.signOut()
        _authState.value = AuthState.SignedOut
    }

    fun getCurrentUser() = repository.getCurrentUser()
}