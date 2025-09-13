package com.example.myapitest.ui.authenticaded.profile

import androidx.lifecycle.ViewModel
import com.example.myapitest.network.FirebaseService

class ProfileViewModel : ViewModel() {
    fun logout() {
        try {
            FirebaseService.signOut()
        } catch (e: Exception) {

        }
    }
}