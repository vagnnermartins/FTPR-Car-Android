package com.example.myapitest.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapitest.data.repository.CarRepository
import com.example.myapitest.domain.model.Car
import kotlinx.coroutines.launch

class CarViewModel : ViewModel() {
    private val repository = CarRepository()

    private val _cars = MutableLiveData<List<Car>>(emptyList())
    val cars: LiveData<List<Car>> = _cars

    private val _car = MutableLiveData<Car?>()
    val car: LiveData<Car?> = _car

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _deleteSuccess = MutableLiveData<Boolean>(false)
    val deleteSuccess: LiveData<Boolean> = _deleteSuccess

    fun loadCars() {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            repository.getCars()
                .onSuccess { carList ->
                    _cars.value = carList
                    _error.value = null
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Erro ao carregar carros"
                }
            _loading.value = false
        }
    }

    fun addCar(car: Car) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            repository.addCar(car)
                .onSuccess { newCar ->
                    _car.value = newCar
                    _error.value = null
                    val currentCars = _cars.value?.toMutableList() ?: mutableListOf()
                    currentCars.add(newCar)
                    _cars.value = currentCars
                    loadCars()
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Erro ao adicionar carro"
                }
            _loading.value = false
        }
    }

    fun deleteCar(id: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            _deleteSuccess.value = false

            repository.deleteCar(id)
                .onSuccess {
                    _error.value = null
                    _deleteSuccess.value = true

                    val currentCars = _cars.value?.toMutableList() ?: mutableListOf()
                    val updatedCars = currentCars.filter { it.id != id }
                    _cars.value = updatedCars

                    loadCars()
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Erro ao deletar carro"
                    _deleteSuccess.value = false
                }
            _loading.value = false
        }
    }

    fun updateCar(id: String, car: Car) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null

            repository.updateCar(id, car)
                .onSuccess { updatedCar ->
                    _car.value = updatedCar
                    _error.value = null

                    val currentCars = _cars.value?.toMutableList() ?: mutableListOf()
                    val index = currentCars.indexOfFirst { it.id == id }
                    if (index != -1) {
                        currentCars[index] = updatedCar
                        _cars.value = currentCars
                    }

                    loadCars()
                }
                .onFailure { exception ->
                    _error.value = exception.message ?: "Erro ao atualizar carro"
                }
            _loading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearDeleteSuccess() {
        _deleteSuccess.value = false
    }

    fun clearCar() {
        _car.value = null
    }
}