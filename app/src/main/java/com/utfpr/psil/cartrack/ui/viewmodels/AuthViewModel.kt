package com.utfpr.psil.cartrack.ui.viewmodels

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.utfpr.psil.cartrack.R
import com.utfpr.psil.cartrack.data.model.PhoneAuthResultEvent
import com.utfpr.psil.cartrack.data.repositories.AuthRepository
import com.utfpr.psil.cartrack.ui.model.AuthUiEvents
import com.utfpr.psil.cartrack.ui.model.AuthUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiAuthState = MutableStateFlow(
        AuthUiState(
            isUserLoggedIn = authRepository.isUserLoggedIn()
        )
    )
    val uiAuthState = _uiAuthState.asStateFlow()

    private val _uiAuthEvents = MutableStateFlow<AuthUiEvents>(AuthUiEvents.Idle)
    val uiAuthEvents = _uiAuthEvents.asStateFlow()


    init {
        observeAuthEvents()
    }

    private fun observeAuthEvents() {
        authRepository.getAuthEvents().onEach { event ->
            when (event) {
                is PhoneAuthResultEvent.CodeSent -> {
                    _uiAuthState.update { it.copy(verificationId = event.verificationId) }
                }

                is PhoneAuthResultEvent.Error -> {
                    _uiAuthEvents.update {
                        AuthUiEvents.Error(event.exception.message ?: "Error to send code.")
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    fun sendVerificationCode(countryCode: String, phoneNumber: String, activity: Activity?) {
        val fullNumber = "+$countryCode$phoneNumber"
        _uiAuthEvents.update { AuthUiEvents.Loading }
        _uiAuthState.update { it.copy(phoneNumber = fullNumber) }
        authRepository.sendCode(fullNumber, activity)
    }

    fun verifyCode(verificationId: String?, code: String) = viewModelScope.launch {
        verificationId?.let {
            Log.d("AuthViewModel", "Verifying code: $code")
            _uiAuthEvents.update { AuthUiEvents.Loading }

            val result = authRepository.signIn(verificationId, code)
            result.fold(
                onSuccess = { user ->
                    _uiAuthEvents.update { AuthUiEvents.Success }
                    _uiAuthState.update { it.copy(user = user.toString(), isUserLoggedIn = true) }
                }, onFailure = { exception ->
                    _uiAuthEvents.update {
                        AuthUiEvents.Error(
                            exception.message ?: "Error to verify code."
                        )
                    }
                }
            )
        }
    }

    fun loginWithGoogle(context: Context) = viewModelScope.launch {
        _uiAuthEvents.update { AuthUiEvents.Loading }
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        try {
            val result = credentialManager.getCredential(context = context, request = request)

            val credential = result.credential

            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val signInResult = authRepository.signInWithGoogle(idToken)

                signInResult.fold(
                    onSuccess = { user ->
                        _uiAuthEvents.update { AuthUiEvents.Success }
                        _uiAuthState.update {
                            it.copy(
                                user = user.toString(),
                                isUserLoggedIn = true
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiAuthEvents.update {
                            AuthUiEvents.Error(
                                e.localizedMessage ?: "Erro ao autenticar com Google"
                            )
                        }
                    }
                )

            } else {
                _uiAuthEvents.update { AuthUiEvents.Error("Erro ao autenticar com Google") }
            }
        } catch (e: GetCredentialException) {
            Log.e("AuthViewModel", "Google Login Error: ${e.localizedMessage}", e)
            val errorMessage = when (e) {
                is GetCredentialCancellationException -> "Login cancelado pelo usuário"
                is NoCredentialException -> "Nenhuma conta Google encontrada no dispositivo"
                else -> e.localizedMessage ?: "Erro ao recuperar credenciais"
            }
            _uiAuthEvents.update { AuthUiEvents.Error(errorMessage) }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Unknown Google Login Error", e)
            _uiAuthEvents.update {
                AuthUiEvents.Error(
                    e.localizedMessage ?: "Erro ao autenticar com Google"
                )
            }
        }
    }

    fun signOut(context: Context) {
        authRepository.signOut()
        _uiAuthState.update { it.copy(isUserLoggedIn = false) }
        _uiAuthEvents.update { AuthUiEvents.Idle }
        viewModelScope.launch {
            val credentialManager = CredentialManager.create(context)
            try {
                val clearRequest = ClearCredentialStateRequest()
                credentialManager.clearCredentialState(clearRequest)
            } catch (e: ClearCredentialException) {
                Log.e("AuthViewModel", "Couldn't clear user credentials: ${e.localizedMessage}")
            }
        }
    }

    fun resetEvents() {
        _uiAuthEvents.update { AuthUiEvents.Idle }
    }
}
