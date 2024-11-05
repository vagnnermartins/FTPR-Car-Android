package com.example.myapitest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapitest.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>


    private var verificationId = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupGoogleLogin()
        setupView()
    }

    private fun setupView() {
        binding.loginSocialButton.setOnClickListener {
            signIn()
        }
        binding.btnSendSms.setOnClickListener {
            sendVerificationCode()
        }
        binding.btnVerifySms.setOnClickListener {
            verifyCode()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)

    }

    private fun setupGoogleLogin() {
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1090783472355-j3vu4lkd5esm6iqlr7jrn24e92289gmv.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = FirebaseAuth.getInstance()
        googleSignInLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
                try {
                    val account = task.getResult(ApiException::class.java)

                    account.idToken?.let { idToken ->
                        Log.d("LoginActivity", "firebaseAuthWithGoogle: $idToken")
                        firebaseAuthWithGoogle(account?.idToken!!)
                    }


                } catch (e: ApiException) {
                    Log.e("LoginActivity", "Google sign in failed", e)
                }
            }
    }

    private fun navigateToMainActivity() {
        startActivity(MainActivity.newIntent(this))
        finish()
    }


    private fun firebaseAuthWithGoogle(idToken: String) {
        val credntial = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credntial)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d("LoginActivity", "signInWithCredential:success $user")
                    navigateToMainActivity()
                } else {
                    Log.w("LoginActivity", "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun sendVerificationCode() {
        val phoneNumber = binding.cellphone.text.toString()
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(
                        this@LoginActivity,
                        "${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    this@LoginActivity.verificationId = verificationId
                    Toast.makeText(
                        this@LoginActivity,
                        "Código de verificação enviado",
                        Toast.LENGTH_SHORT
                    ).show()
                    binding.btnVerifySms.visibility = View.VISIBLE
                    binding.veryfyCode.visibility = View.VISIBLE
                }

            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyCode() {
        val verificationCode = binding.veryfyCode.text.toString()
        val credential = PhoneAuthProvider.getCredential(verificationId, verificationCode)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                onCredentialCompleteListener(task, "Phone Number")
            }
    }

    private fun onCredentialCompleteListener(
        task: Task<AuthResult>,
        loginType: String
    ) {
        if (task.isSuccessful) {
            val user = auth.currentUser
            Log.d("LoginActivity", "LoginType: $loginType User: ${user?.uid}")
            navigateToMainActivity()
        } else {
            Toast.makeText(
                this,
                "${task.exception?.localizedMessage}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    companion object {
        fun newIntent(context: AppCompatActivity): Intent {
            return Intent(context, LoginActivity::class.java)
        }
    }
}