package com.utfpr.psil.cartrack.data.datasources

import android.app.Activity
import com.utfpr.psil.cartrack.data.model.PhoneAuthResultEvent
import kotlinx.coroutines.flow.Flow

interface AuthDataSource {
    fun phoneAuthFlow(): Flow<PhoneAuthResultEvent>

    fun sendVerificationCode(phoneNumber: String, activity: Activity?)

    suspend fun trySignInWithVerificationCode(verificationId: String, code: String): Result<Any?>

    fun signOut()

    fun isUserLoggedIn(): Boolean

    suspend fun signInWithGoogle(idToken: String): Result<Any?>
}