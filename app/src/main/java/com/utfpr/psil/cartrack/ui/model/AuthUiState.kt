package com.utfpr.psil.cartrack.ui.model

data class AuthUiState (
    val verificationId: String? = null,
    val isUserLoggedIn: Boolean = false,
    val user: String? = null,
    val phoneNumber: String? = null
)