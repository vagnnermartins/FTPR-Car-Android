package com.example.myapitest.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.myapitest.data.repository.AuthRepository
import com.example.myapitest.data.repository.AuthRepositoryImpl
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class ProfileUiState(
    val isLoading: Boolean = true,
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val logoutSuccess: Boolean = false
)

class ProfileViewModel : ViewModel() {
    private val authRepository: AuthRepository = AuthRepositoryImpl()
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    displayName = currentUser.displayName,
                    email = currentUser.email,
                    photoUrl = currentUser.photoUrl?.toString()
                )
            }
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.update { it.copy(logoutSuccess = true) }
    }
}