package com.example.myapitest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import androidx.core.widget.addTextChangedListener
import com.example.myapitest.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var verificationId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkIfUserIsLogged()
        onInit()
    }

    private fun onInit() {
        binding.loginPhone.addTextChangedListener { onLoginPhoneChanged() }
        binding.loginCode.addTextChangedListener { onLoginCodeChanged() }
        binding.submitButton.setOnClickListener { onSubmitPressed() }
        binding.resendCode.setOnClickListener { onResendCodePressed() }
        binding.sendCodeButton.setOnClickListener { onCodeSend() }
    }

    private fun checkIfUserIsLogged() {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            navigateToMainActivity()
        }
    }

    private fun navigateToMainActivity() {
        startActivity(MainActivity.newIntent(this))
        finish()
    }

    private fun onLoginPhoneChanged() {
        val phone = binding.loginPhone.text.toString()
        checkPhoneError(phone)

    }

    private fun onLoginCodeChanged() {
        val code = binding.loginCode.text.toString()
        checkCodeError(code)
    }

    private fun checkCodeError(code: String): Boolean {
        if (code.isEmpty()) {
            binding.loginCode.error = getString(R.string.required_field)
            return true
        }
        return false
    }

    private fun checkPhoneError(phone: String): Boolean {
        if (phone.isEmpty()) {
            binding.loginPhone.error = getString(R.string.phone_error_empty)
            return true
        }
        if (phone.isDigitsOnly()) {
            binding.loginPhone.error = getString(R.string.phone_error_digit)
            return true
        }
        return false
    }

    private fun onSubmitPressed() {
        val phone = binding.loginPhone.text.toString()
        val code = binding.loginCode.text.toString()

        val isPhoneError = checkPhoneError(phone)
        val isCodeError = checkCodeError(code)
        if (!isPhoneError && !isCodeError) {
            if (verificationId == null) {
                attemptPhoneVerification(phone, code)
                return
            }
            signInWithCredential(code)
        }
    }

    private fun signInWithCredential(code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        val auth = FirebaseAuth.getInstance()
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToMainActivity()
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        R.string.error_message_other,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun attemptPhoneVerification(phone: String, code: String) {
        val auth = FirebaseAuth.getInstance()


        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(30L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(createVerificationCallbacks(code))
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun createVerificationCallbacks(expectedCode: String): PhoneAuthProvider.OnVerificationStateChangedCallbacks {
        return object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                val auth = FirebaseAuth.getInstance()
                auth.signInWithCredential(credential)
                    .addOnCompleteListener(this@LoginActivity) { task ->
                        if (task.isSuccessful) {
                            navigateToMainActivity()
                        } else {
                            Toast.makeText(
                                this@LoginActivity,
                                R.string.error_message_other,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(
                    this@LoginActivity,
                    R.string.error_no_validation,
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onCodeSent(
                verificationId: String,
                resendToken: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d("LoginActivity", "Code sent, verificationId: $verificationId")
                this@LoginActivity.verificationId = verificationId
                this@LoginActivity.resendToken = resendToken
                // Automatically proceed with the provided code
                signInWithCredential(expectedCode)
            }
        }
    }

    private fun onCodeSend() {
        val phone = binding.loginPhone.text.toString()
        val hasError = checkPhoneError(phone)
        if (!hasError) {
            val auth = FirebaseAuth.getInstance()
            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(callbacks())
                .build()
            PhoneAuthProvider.verifyPhoneNumber(options)
        }
    }

    private fun callbacks(): PhoneAuthProvider.OnVerificationStateChangedCallbacks {
        val cb = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d("LoginActivity", "onVerificationCompleted:$credential")
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w("LoginActivity", "onVerificationFailed", e)

                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        Toast.makeText(
                            this@LoginActivity,
                            R.string.error_message_invalid_credentials,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                    is FirebaseTooManyRequestsException -> {
                        Toast.makeText(
                            this@LoginActivity,
                            R.string.error_message_exceeded_attempts,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }

                    else -> {
                        Toast.makeText(
                            this@LoginActivity,
                            R.string.error_message_other,
                            Toast.LENGTH_SHORT
                        )
                            .show()
                    }
                }

            }

            override fun onCodeSent(
                verificationId: String,
                resendToken: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(verificationId, resendToken)
                this@LoginActivity.resendToken = resendToken
                this@LoginActivity.verificationId = verificationId
            }
        }

        return cb
    }

    private fun onResendCodePressed() {
        if (resendToken == null) return
        val phone = binding.loginPhone.text.toString()
        val auth = FirebaseAuth.getInstance()
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks())
            .setForceResendingToken(resendToken!!)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }


    companion object {
        fun newIntent(context: Context) = Intent(context, LoginActivity::class.java)
    }

}