package com.example.myapitest.data.repository

import com.example.myapitest.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class AuthRepository {
    private val firebaseAuth = FirebaseAuth.getInstance()

    fun sendVerificationCode(
        phoneNumber: String,
        activity: MainActivity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyPhoneNumber(
        verificationId: String,
        code: String,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithCredential(credential, onComplete)
    }

    fun signInWithGoogle(
        account: GoogleSignInAccount,
        onComplete: (Boolean, String?) -> Unit
    ) {
        val credential =
            com.google.firebase.auth.GoogleAuthProvider.getCredential(account.idToken, null)
        signInWithCredential(credential, onComplete)
    }

    fun signInWithCredential(
        credential: com.google.firebase.auth.AuthCredential,
        onComplete: (Boolean, String?) -> Unit
    ) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, null)
                } else {
                    onComplete(false, task.exception?.message)
                }
            }
    }

    fun getCurrentUser() = firebaseAuth.currentUser

    fun signOut() = firebaseAuth.signOut()
}