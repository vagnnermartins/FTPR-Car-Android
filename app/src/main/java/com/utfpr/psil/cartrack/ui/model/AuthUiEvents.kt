package com.utfpr.psil.cartrack.ui.model

sealed class AuthUiEvents {
    object Idle : AuthUiEvents()
    object Loading : AuthUiEvents()
    object Success : AuthUiEvents()
    data class Error(val message: String) : AuthUiEvents()
}