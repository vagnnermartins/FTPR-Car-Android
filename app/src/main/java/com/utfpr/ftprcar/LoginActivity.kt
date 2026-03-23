package com.utfpr.ftprcar

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.utfpr.ftprcar.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // If user is already signed in, go directly to MainActivity
        if (auth.currentUser != null) {
            goToMain()
            return
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnSendCode.setOnClickListener {
            val phone = binding.etPhone.text.toString().trim()
            if (phone.isEmpty()) {
                binding.tilPhone.error = "Please enter a phone number"
                return@setOnClickListener
            }
            binding.tilPhone.error = null
            sendVerificationCode(phone)
        }

        binding.btnVerify.setOnClickListener {
            val code = binding.etOtp.text.toString().trim()
            if (code.isEmpty()) {
                binding.tilOtp.error = "Please enter the verification code"
                return@setOnClickListener
            }
            binding.tilOtp.error = null
            val vId = storedVerificationId
            if (vId != null) {
                val credential = PhoneAuthProvider.getCredential(vId, code)
                signInWithPhoneAuthCredential(credential)
            }
        }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        showProgress(true)

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-retrieval or instant verification
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                showProgress(false)
                Log.e("LoginActivity", "Verification failed", e)
                Toast.makeText(
                    this@LoginActivity,
                    "Verification failed: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                storedVerificationId = verificationId
                resendToken = token
                showProgress(false)

                // Show OTP section
                binding.tilOtp.visibility = View.VISIBLE
                binding.btnVerify.visibility = View.VISIBLE
                binding.btnSendCode.text = "Resend Code"
                Toast.makeText(this@LoginActivity, "Code sent!", Toast.LENGTH_SHORT).show()
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        showProgress(true)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                showProgress(false)
                if (task.isSuccessful) {
                    goToMain()
                } else {
                    Log.e("LoginActivity", "Sign in failed", task.exception)
                    Toast.makeText(
                        this,
                        "Sign in failed: ${task.exception?.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showProgress(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSendCode.isEnabled = !show
        binding.btnVerify.isEnabled = !show
    }
}
