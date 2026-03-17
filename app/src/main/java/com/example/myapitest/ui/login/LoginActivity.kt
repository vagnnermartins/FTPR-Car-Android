package com.example.myapitest.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapitest.R
import com.example.myapitest.ui.main.MainActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

// RZ - LoginActivity: A porta de entrada do app.
// Fluxo atualizado com feedback visual de carregamento e dicas claras.

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null

    private lateinit var etPhoneNumber: EditText
    private lateinit var etVerificationCode: EditText
    private lateinit var btnSendCode: Button
    private lateinit var btnLogin: Button
    private lateinit var tvLoginError: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnCloseApp: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        etVerificationCode = findViewById(R.id.etVerificationCode)
        btnSendCode = findViewById(R.id.btnSendCode)
        btnLogin = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.loginProgressBar)
        btnCloseApp = findViewById(R.id.btnCloseApp)
        
        tvLoginError = TextView(this).apply {
            visibility = View.GONE
            setTextColor(android.graphics.Color.BLACK)
            textSize = 14f
            gravity = android.view.Gravity.CENTER
            setPadding(0, 16, 0, 16)
        }
        
        // RZ - Busca o container linear interno para adicionar a mensagem de erro
        val container = findViewById<LinearLayout>(R.id.loginContainer)
        container?.addView(tvLoginError, container.indexOfChild(progressBar))

        btnSendCode.setOnClickListener {
            val phoneNumber = etPhoneNumber.text.toString()
            if (phoneNumber.isNotEmpty()) {
                tvLoginError.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
                btnSendCode.isEnabled = false
                startPhoneNumberVerification(phoneNumber)
            } else {
                Toast.makeText(this, "Digite o número", Toast.LENGTH_SHORT).show()
            }
        }

        btnLogin.setOnClickListener {
            val code = etVerificationCode.text.toString()
            if (code.isNotEmpty() && verificationId != null) {
                progressBar.visibility = View.VISIBLE
                btnLogin.isEnabled = false
                signInWithPhoneAuthCredential(PhoneAuthProvider.getCredential(verificationId!!, code))
            }
        }

        // RZ - Fecha o aplicativo se o usuário clicar no ícone de sair
        btnCloseApp.setOnClickListener {
            finishAffinity()
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    progressBar.visibility = View.GONE
                    btnSendCode.isEnabled = true
                    tvLoginError.text = "NUMERO NÃO CADASTRADO\nUSO O NÚMERO EXEMPLO\nOU PEÇA O CADASTRO\nAO ADMINISTRADOR."
                    tvLoginError.visibility = View.VISIBLE
                }

                override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    progressBar.visibility = View.GONE
                    this@LoginActivity.verificationId = verificationId
                    etVerificationCode.visibility = View.VISIBLE
                    btnLogin.visibility = View.VISIBLE
                    btnSendCode.visibility = View.GONE
                    Toast.makeText(this@LoginActivity, "Código enviado!", Toast.LENGTH_SHORT).show()
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true
                if (task.isSuccessful) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    tvLoginError.text = "CÓDIGO INVÁLIDO"
                    tvLoginError.visibility = View.VISIBLE
                }
            }
    }
}
