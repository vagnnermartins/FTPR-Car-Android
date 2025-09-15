package com.example.myapitest.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapitest.model.Car
import com.example.myapitest.data.repository.CarRepository
import com.example.myapitest.data.repository.CarRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = true,
    val cars: List<Car> = emptyList(),
    val error: String? = null
)

class HomeViewModel : ViewModel() {
    private val carRepository: CarRepository = CarRepositoryImpl()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    fun fetchCars() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = carRepository.getCars()
            result.onSuccess { cars ->
                _uiState.update { it.copy(isLoading = false, cars = cars) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }
}