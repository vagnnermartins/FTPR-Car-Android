package com.example.myapitest.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapitest.data.repository.CarRepository
import com.example.myapitest.domain.model.Car
import kotlinx.coroutines.launch

class CarDetailViewModel : ViewModel() {
    private val repository = CarRepository()

    private val _car = MutableLiveData<Car?>()
    val car: LiveData<Car?> = _car

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadCarDetails(carId: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            repository.getCarById(carId)
                .onSuccess { carResponse ->
                    _car.value = carResponse.value
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }

            _loading.value = false
        }
    }

    fun updateCar(carId: String, car: Car) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            repository.updateCar(carId, car)
                .onSuccess { updatedCar ->
                    _car.value = updatedCar
                    _error.value = null
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Erro ao atualizar carro"
                }
            _loading.value = false
        }
    }
}