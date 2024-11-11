package com.example.ftprcar.ui.car.list

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ftprcar.model.Car
import com.example.ftprcar.service.Result
import com.example.ftprcar.service.RetrofitClient
import com.example.ftprcar.service.safeApiCall
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CarListViewModel : ViewModel() {
    private val _state = MutableStateFlow(CarListState())
    val state: StateFlow<CarListState> = _state.asStateFlow()

    val storageRef = FirebaseStorage.getInstance().reference

    init {
        fetchCars()
    }

    fun getCar(carId: String): Car? {
        return state.value.cars.find { it.id == carId }
    }

    fun fetchCars() {
        viewModelScope.launch { updateCurrentCars() }
    }

    private suspend fun updateCurrentCars(processed: Boolean = false) {
        _state.update { it.copy(loading = true, error = "") }
        val response = safeApiCall { RetrofitClient.carService.getCars() }

        _state.update { currentState ->
            when (response) {
                is Result.Success -> currentState.copy(
                    cars = response.data,
                    error = "",
                    processed = processed
                )
                is Result.Error -> currentState.copy(error = response.message)
            }
        }
        _state.update { it.copy(loading = false) }
    }

    fun deleteCar(carId: String) {
        _state.update { it.copy(loading = true, error = "") }
        viewModelScope.launch {
            val response = safeApiCall { RetrofitClient.carService.deleteCar(carId) }
            when (response) {
                is Result.Success -> updateCurrentCars(true)
                is Result.Error -> _state.update { it.copy(error = response.message) }
            }
            _state.update { it.copy(loading = false) }
        }
    }

    fun addCar(car: Car, uri: Uri?) {
        if (!validateCar(car)) {
            _state.update { it.copy(error = "Please fill all fields") }
            return
        }
        _state.update { it.copy(loading = true, error = "") }

        uploadFileToFirebase(uri!!) { message, downloadUrl ->
            if (message == "Upload successful") {
                if (downloadUrl.isNotBlank()) {
                    viewModelScope.launch {
                        val response = safeApiCall {
                            RetrofitClient.carService.addCar(car.copy(imageUrl = downloadUrl))
                        }
                        when (response) {
                            is Result.Success -> updateCurrentCars(true)
                            is Result.Error -> _state.update { it.copy(error = response.message) }
                        }
                        _state.update { it.copy(loading = false) }
                    }
                }
            } else {
                _state.update { it.copy(loading = false, error = message) }
            }
        }
    }

    private fun uploadFileToFirebase(uri: Uri, onUploadComplete: (String, String) -> Unit) {
        val fileRef = storageRef.child("images/${uri.lastPathSegment ?: "file"}.jpg")
        fileRef.putFile(uri)
            .addOnSuccessListener {
                onUploadComplete("Upload successful", "")
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    onUploadComplete("Upload successful", uri.toString())
                }
            }
            .addOnFailureListener { exception ->
                onUploadComplete(
                    "Upload failed: ${exception.message}",
                    ""
                )
            }
    }

    private fun validateCar(car: Car): Boolean {
        return car.name.isNotBlank() && car.year.isNotBlank() && car.licence.isNotBlank() && car.imageUrl.isNotBlank()
    }

    fun clearError() {
        _state.update { it.copy(error = "") }
    }
}