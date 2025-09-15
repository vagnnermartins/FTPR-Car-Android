package com.example.myapitest.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapitest.data.repository.CarRepository
import com.example.myapitest.data.repository.CarRepositoryImpl
import com.example.myapitest.domain.model.CarDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CarDetailUiState(
    val isLoading: Boolean = true,
    val car: CarDetail? = null,
    val error: String? = null,
    val deleteSuccess: Boolean = false
)

class CarDetailViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val carRepository: CarRepository = CarRepositoryImpl()

    private val _uiState = MutableStateFlow(CarDetailUiState())
    val uiState = _uiState.asStateFlow()

    private val carId: String = checkNotNull(savedStateHandle["carId"])

    init {
        fetchCarDetails()
    }

    fun fetchCarDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = carRepository.getCarById(carId)
            result.onSuccess { car ->
                _uiState.update { it.copy(isLoading = false, car = car) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }

    fun deleteCar() {
        if (carId != null) {
            viewModelScope.launch {
                val result = carRepository.deleteCar(carId)
                result.onSuccess {
                    _uiState.update { it.copy(deleteSuccess = true) }
                }.onFailure { error ->
                    _uiState.update { it.copy(error = "Erro ao deletar: ${error.message}") }
                }
            }
        }
    }
}