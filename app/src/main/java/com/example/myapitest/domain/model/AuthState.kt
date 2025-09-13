package com.example.myapitest.domain.model

sealed class AuthState {
    object Loading : AuthState()
    object CodeSent : AuthState()
    object Success : AuthState()
    object SignedOut : AuthState()

    data class Error(val message: String) : AuthState()
}