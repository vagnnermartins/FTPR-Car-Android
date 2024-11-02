package com.example.myapitest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var auth: FirebaseAuth
    private var verificationId = ""


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()


      verifyLoggedUser()

    }

    private fun setupView() {
        binding.btEnviaSMS.setOnClickListener {
            enviaCodigoSMS()
        }
        binding.btVerificarSMS.setOnClickListener {
            verificaCodigoSMS ()
        }
    }

    private fun verificaCodigoSMS() {
        val codigoVerificacao = binding.etSMS.text.toString()
        val credencial = PhoneAuthProvider.getCredential(verificationId, codigoVerificacao)
        auth.signInWithCredential(credencial)
            .addOnCompleteListener(this) { task ->
                onCredencialCompleteListener(task)

            }
    }

    private fun onCredencialCompleteListener(task: Task<AuthResult>) {
        if (task.isSuccessful) {
            navigateToMainActivity()
        } else {
            Toast.makeText(this, "${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun enviaCodigoSMS() {
        auth = FirebaseAuth.getInstance()
        val numeroTelefone = binding.etPhone.text.toString()
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(numeroTelefone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object: PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                }

                override fun onVerificationFailed(e: FirebaseException) {
                 Toast.makeText(this@LoginActivity, "${e.message}",Toast.LENGTH_SHORT).show()
                }

                override fun onCodeSent(idVerificacao: String, token: PhoneAuthProvider.ForceResendingToken) {
                    this@LoginActivity.verificationId = idVerificacao
                    Toast.makeText(this@LoginActivity, getString(R.string.codigo_enviado),
                        Toast.LENGTH_SHORT).show()
                    binding.btVerificarSMS.visibility = View.VISIBLE
                    binding.etSMS.visibility = View.VISIBLE
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyLoggedUser() {
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null){
            navigateToMainActivity()
        }
    }
    private fun navigateToMainActivity() {
        startActivity(MainActivity.newIntent(this))
        finish()
    }

    companion object{
        fun newIntent(context: Context) = Intent(context, LoginActivity::class.java)
    }


}