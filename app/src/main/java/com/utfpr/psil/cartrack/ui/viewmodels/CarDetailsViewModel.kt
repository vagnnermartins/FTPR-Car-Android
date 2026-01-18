package com.utfpr.psil.cartrack.ui.viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utfpr.psil.cartrack.data.repositories.CarRepository
import com.utfpr.psil.cartrack.data.repositories.PlacesRepository
import com.utfpr.psil.cartrack.ui.model.CarDetailsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CarDetailsViewModel @Inject constructor(
    private val carRepository: CarRepository,
    private val placesRepository: PlacesRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val carId: String? = savedStateHandle["carId"]

    private val _uiState = MutableStateFlow(CarDetailsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        Log.d("CarDetailsViewModel", "CarId: $carId")
        carId?.let { loadCar(it) }
    }

    fun loadCar(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val car = carRepository.getCarById(id)
                val address = placesRepository.getAddressFromCoordinates(
                    car.value.place.lat,
                    car.value.place.long
                )
                Log.d("CarDetailsViewModel", "Car: $car")
                _uiState.update { it.copy(car = car.value, address = address, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.localizedMessage ?: "Erro desconhecido",
                        isLoading = false
                    )
                }
            }
        }
    }

    fun refresh() {
        carId?.let { loadCar(it) }
    }
}