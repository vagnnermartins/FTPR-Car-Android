package com.utfpr.psil.cartrack.data.datasources

import android.app.Activity
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.utfpr.psil.cartrack.data.model.PhoneAuthResultEvent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class AuthDataSourceImpl: AuthDataSource {
    private val auth = FirebaseAuth.getInstance()

    private var authCallback: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null

    init {
        auth.useAppLanguage()
    }

    override fun phoneAuthFlow(): Flow<PhoneAuthResultEvent> = callbackFlow {
        authCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                close()
            }

            override fun onVerificationFailed(authException: FirebaseException) {
                trySend(PhoneAuthResultEvent.Error(authException))
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                trySend(PhoneAuthResultEvent.CodeSent(verificationId))
            }
        }
        awaitClose {
            authCallback = null
        }
    }

    override fun sendVerificationCode(phoneNumber: String, activity: Activity?) {
        authCallback?.let { callback ->
            activity?.let {
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phoneNumber)
                    .setTimeout(AUTH_TIMEOUT_MINUTES, TimeUnit.MINUTES)
                    .setActivity(activity)
                    .setCallbacks(callback)
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            }
        }
    }

    override suspend fun trySignInWithVerificationCode(verificationId: String, code: String): Result<Any?> {
        try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            val task = auth.signInWithCredential(credential).await()
            return Result.success(task.user)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override fun signOut() = auth.signOut()

    override fun isUserLoggedIn(): Boolean {
        Log.d("AuthDataSource", "isUserLoggedIn: ${auth.currentUser != null}")
        return auth.currentUser != null
    }

    override suspend fun signInWithGoogle(idToken: String): Result<Any?> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            Result.success(authResult.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private companion object {
        const val AUTH_TIMEOUT_MINUTES = 2L
    }
}
