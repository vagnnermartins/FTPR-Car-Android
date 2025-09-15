package com.example.myapitest.data.repository

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

interface AuthRepository {
    fun sendVerificationCode(activity: Activity, phoneNumber: String): Flow<PhoneAuthResult>
    suspend fun signInWithCredential(credential: PhoneAuthCredential): Boolean
    fun logout()
    suspend fun signInWithGoogle(idToken: String): Boolean
}

sealed class PhoneAuthResult {
    data class CodeSent(val verificationId: String) : PhoneAuthResult()
    data class VerificationCompleted(val credential: PhoneAuthCredential) : PhoneAuthResult()
    data class Error(val message: String) : PhoneAuthResult()
}


class AuthRepositoryImpl : AuthRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun sendVerificationCode(
        activity: Activity,
        phoneNumber: String
    ): Flow<PhoneAuthResult> {
        return callbackFlow {
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    trySend(PhoneAuthResult.VerificationCompleted(credential))
                }

                override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                    trySend(
                        PhoneAuthResult.Error(
                            e.localizedMessage ?: "Erro desconhecido."
                        )
                    )
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    trySend(PhoneAuthResult.CodeSent(verificationId))
                }
            }

            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)

            awaitClose { }
        }
    }

    override suspend fun signInWithCredential(credential: PhoneAuthCredential): Boolean {
        return try {
            !firebaseAuth.signInWithCredential(credential).isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Boolean {
        return try {
            val credential =
                com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
            firebaseAuth.signInWithCredential(credential).await()
            firebaseAuth.currentUser != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun logout() {
        firebaseAuth.signOut()
    }
}