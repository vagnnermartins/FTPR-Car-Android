package com.utfpr.psil.cartrack.data.repositories

import android.app.Activity
import com.utfpr.psil.cartrack.data.model.PhoneAuthResultEvent
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    fun getAuthEvents(): Flow<PhoneAuthResultEvent>

    fun sendCode(phoneNumber: String, activity: Activity?)

    suspend fun signIn(verificationId: String, code: String): Result<Any?>

    fun signOut()

    fun isUserLoggedIn(): Boolean

    suspend fun signInWithGoogle(idToken: String): Result<Any?>
}