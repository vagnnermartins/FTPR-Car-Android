package com.example.myapitest.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapitest.api.RetrofitClient
import com.example.myapitest.model.Car
import kotlinx.coroutines.launch

class CarViewModel : ViewModel() {

    private val _cars = MutableLiveData<List<Car>>()
    val cars: LiveData<List<Car>> = _cars

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetchCars() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getCars()
                _cars.value = response
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}