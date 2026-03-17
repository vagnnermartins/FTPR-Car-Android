package com.example.myapitest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapitest.model.Car
import com.example.myapitest.repository.CarRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

sealed class CarUiState {
    object Loading : CarUiState()
    data class Success(val cars: List<Car>) : CarUiState()
    data class Error(val message: String) : CarUiState()
}

class CarViewModel : ViewModel() {

    private val repository = CarRepository()

    private val _uiState = MutableStateFlow<CarUiState>(CarUiState.Loading)
    val uiState: StateFlow<CarUiState> = _uiState

    fun fetchCars() {
        viewModelScope.launch {
            _uiState.value = CarUiState.Loading
            repository.getCars()
                .catch { e ->
                    _uiState.value = CarUiState.Error("Error fetching cars: ${e.message}")
                }
                .collect { cars ->
                    _uiState.value = CarUiState.Success(cars)
                }
        }
    }

    fun createCar(car: Car, imageUri: android.net.Uri?, userId: String = "default_user", onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                // 1. Upload Image if exists
                var finalImageUrl = car.imageUrl
                if (imageUri != null) {
                    val imageRepo = com.example.myapitest.repository.ImageRepository()
                    finalImageUrl = imageRepo.uploadImage(imageUri, userId)
                }

                // 2. Map new Car with possibly new Image URL
                val carToSave = car.copy(imageUrl = finalImageUrl)

                // 3. Save to API
                repository.createCar(carToSave)
                    .catch { e ->
                        onResult(false, e.message)
                    }
                    .collect {
                        fetchCars() // Refresh the list
                        onResult(true, null)
                    }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    fun updateCar(id: String, car: Car, imageUri: android.net.Uri?, userId: String = "default_user", onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                var finalImageUrl = car.imageUrl
                if (imageUri != null) {
                    val imageRepo = com.example.myapitest.repository.ImageRepository()
                    finalImageUrl = imageRepo.uploadImage(imageUri, userId)
                }

                val carToSave = car.copy(imageUrl = finalImageUrl)

                repository.updateCar(id, carToSave)
                    .catch { e ->
                        onResult(false, e.message)
                    }
                    .collect {
                        fetchCars()
                        onResult(true, null)
                    }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }
}
