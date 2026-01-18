package com.utfpr.psil.cartrack.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utfpr.psil.cartrack.data.repositories.CarRepository
import com.utfpr.psil.cartrack.ui.model.CarListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CarListViewModel @Inject constructor(private val carRepository: CarRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<CarListUiState>(CarListUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        fetchCars()
    }

    fun fetchCars() = viewModelScope.launch {
        _uiState.update { CarListUiState.Loading }
        try {
            val cars = carRepository.fetchAllCars()
            _uiState.update { CarListUiState.Success(cars) }
        } catch (e: Exception) {
            _uiState.update {
                CarListUiState.Error(
                    e.localizedMessage ?: "Erro ao buscar carros"
                )
            }
        }
    }

    fun onDeleteCar(id: String) = viewModelScope.launch {
        try {
            carRepository.removeCar(id)
            fetchCars()
        } catch (e: Exception) {
            _uiState.update {
                CarListUiState.Error(
                    e.localizedMessage ?: "Erro ao remover carro"
                )
            }
        }
    }
}
