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

    fun createCar(car: Car, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            repository.createCar(car)
                .catch { e ->
                    onResult(false, e.message)
                }
                .collect {
                    fetchCars() // Refresh the list
                    onResult(true, null)
                }
        }
    }

    fun updateCar(id: String, car: Car, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            repository.updateCar(id, car)
                .catch { e ->
                    onResult(false, e.message)
                }
                .collect {
                    fetchCars() // Refresh the list
                    onResult(true, null)
                }
        }
    }
}
