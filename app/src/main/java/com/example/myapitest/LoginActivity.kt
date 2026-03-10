package com.example.myapitest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapitest.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private var verificationId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.buttonBack.setOnClickListener {
            // Se estiver no estado de verificar código, volta para telefone
            if (binding.verifyInputContainer.visibility == View.VISIBLE) {
                binding.verifyInputContainer.visibility = View.GONE
                binding.resendContainer.visibility = View.GONE
                binding.phoneInputContainer.visibility = View.VISIBLE
                binding.buttonAction.text = "Enviar Código"
            } else {
                finish()
            }
        }

        binding.buttonAction.setOnClickListener {
            if (binding.phoneInputContainer.visibility == View.VISIBLE) {
                binding.countryCodePicker.registerCarrierNumberEditText(binding.editTextPhone)
                val phoneNumber = binding.countryCodePicker.fullNumberWithPlus
                if (binding.editTextPhone.text.toString().trim().isNotEmpty() && binding.countryCodePicker.isValidFullNumber) {
                    sendVerificationCode(phoneNumber)
                } else {
                    Toast.makeText(this, "Insira um número válido", Toast.LENGTH_SHORT).show()
                }
            } else {
                val code = getVerificationCode()
                if (code.length == 6) {
                    verifyCode(code)
                } else {
                    Toast.makeText(this, "Insira o código de 6 dígitos completo", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.buttonResendCode.setOnClickListener {
            val phoneNumber = binding.countryCodePicker.fullNumberWithPlus
            sendVerificationCode(phoneNumber)
        }

        setupCodeInputs()
    }

    private fun setupCodeInputs() {
        val editTexts = listOf(
            binding.editTextCode1,
            binding.editTextCode2,
            binding.editTextCode3,
            binding.editTextCode4,
            binding.editTextCode5,
            binding.editTextCode6
        )

        for (i in editTexts.indices) {
            editTexts[i].addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1 && i < editTexts.size - 1) {
                        editTexts[i + 1].requestFocus()
                    } else if (s?.length == 0 && i > 0) {
                        editTexts[i - 1].requestFocus()
                    }
                }
                override fun afterTextChanged(s: android.text.Editable?) {}
            })
        }
    }

    private fun getVerificationCode(): String {
        return binding.editTextCode1.text.toString().trim() +
                binding.editTextCode2.text.toString().trim() +
                binding.editTextCode3.text.toString().trim() +
                binding.editTextCode4.text.toString().trim() +
                binding.editTextCode5.text.toString().trim() +
                binding.editTextCode6.text.toString().trim()
    }

    private fun sendVerificationCode(phoneNumber: String) {
        showLoading(true)
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    showLoading(false)
                    Toast.makeText(this@LoginActivity, "Falha na verificação: ${e.message}", Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    showLoading(false)
                    this@LoginActivity.verificationId = verificationId
                    
                    // Show verification UI
                    binding.phoneInputContainer.visibility = View.GONE
                    binding.verifyInputContainer.visibility = View.VISIBLE
                    binding.resendContainer.visibility = View.VISIBLE
                    
                    // Update Text
                    binding.textViewTitle.text = "Verificação"
                    binding.textViewSubtitle.text = "Insira o código de 6 dígitos enviado para seu celular"
                    binding.buttonAction.text = "Verificar e Entrar"
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verifyCode(code: String) {
        showLoading(true)
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                showLoading(false)
                if (task.isSuccessful) {
                    navigateToMain()
                } else {
                    Toast.makeText(this, "Falha na Autenticação", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.buttonAction.isEnabled = !isLoading
    }
}
