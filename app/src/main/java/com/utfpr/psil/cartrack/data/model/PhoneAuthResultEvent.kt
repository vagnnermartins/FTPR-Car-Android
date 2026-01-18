package com.utfpr.psil.cartrack.data.model

sealed class PhoneAuthResultEvent {
    data class CodeSent(val verificationId: String) : PhoneAuthResultEvent()
    data class Error(val exception: Exception) : PhoneAuthResultEvent()
}