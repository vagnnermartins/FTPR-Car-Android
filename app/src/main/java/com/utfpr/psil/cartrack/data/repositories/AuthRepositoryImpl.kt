package com.utfpr.psil.cartrack.data.repositories

import android.app.Activity
import com.utfpr.psil.cartrack.data.datasources.AuthDataSource
import com.utfpr.psil.cartrack.data.model.PhoneAuthResultEvent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(private val authDataSource: AuthDataSource) :
    AuthRepository {
    override fun getAuthEvents(): Flow<PhoneAuthResultEvent> = authDataSource.phoneAuthFlow()

    override fun sendCode(phoneNumber: String, activity: Activity?) =
        authDataSource.sendVerificationCode(phoneNumber, activity)

    override suspend fun signIn(verificationId: String, code: String): Result<Any?> {
        return authDataSource.trySignInWithVerificationCode(verificationId, code)
    }

    override fun signOut() = authDataSource.signOut()

    override fun isUserLoggedIn() = authDataSource.isUserLoggedIn()

    override suspend fun signInWithGoogle(idToken: String) =
        authDataSource.signInWithGoogle(idToken)
}