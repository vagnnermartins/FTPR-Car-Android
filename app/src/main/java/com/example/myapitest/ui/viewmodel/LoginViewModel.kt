package com.example.myapitest.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapitest.data.repository.AuthRepository
import com.example.myapitest.data.repository.AuthRepositoryImpl
import com.example.myapitest.data.repository.PhoneAuthResult
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val phoneNumber: String = "",
    val verificationCode: String = "",
    val isLoading: Boolean = false,
    val showVerificationCodeInput: Boolean = false,
    val error: String? = null,
    val loginSuccess: Boolean = false
)

class LoginViewModel : ViewModel() {
    private val authRepository: AuthRepository = AuthRepositoryImpl()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    private var currentVerificationId: String? = null

    fun onPhoneNumberChange(newNumber: String) {
        _uiState.update { it.copy(phoneNumber = newNumber) }
    }

    fun onVerificationCodeChange(newCode: String) {
        _uiState.update { it.copy(verificationCode = newCode) }
    }

    fun onSendVerificationCode(activity: Activity) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        val fullPhoneNumber = "+55${_uiState.value.phoneNumber}"

        viewModelScope.launch {
            authRepository.sendVerificationCode(activity, fullPhoneNumber).collect { result ->
                when (result) {
                    is PhoneAuthResult.CodeSent -> {
                        currentVerificationId = result.verificationId
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                showVerificationCodeInput = true
                            )
                        }
                    }

                    is PhoneAuthResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }

                    is PhoneAuthResult.VerificationCompleted -> {
                        signInWithCredential(result.credential)
                    }
                }
            }
        }
    }

    fun onVerifyCode() {
        if (currentVerificationId == null) {
            _uiState.update { it.copy(error = "ID de verificação não encontrado.") }
            return
        }
        _uiState.update { it.copy(isLoading = true, error = null) }

        val credential = PhoneAuthProvider.getCredential(
            currentVerificationId!!,
            _uiState.value.verificationCode
        )
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            val success = authRepository.signInWithCredential(credential)
            if (success) {
                _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Falha ao fazer login. Código inválido?"
                    )
                }
            }
        }
    }

    fun signInWithGoogle(idToken: String?) {
        if (idToken == null) {
            _uiState.update { it.copy(error = "Falha ao obter o token do Google.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val success = authRepository.signInWithGoogle(idToken)
            if (success) {
                _uiState.update { it.copy(isLoading = false, loginSuccess = true) }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Falha na autenticação com o Firebase."
                    )
                }
            }
        }
    }
}