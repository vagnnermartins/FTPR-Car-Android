package com.example.myapitest.ui.authenticaded.cars.carsLIst

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapitest.network.ServiceApi
import com.example.myapitest.network.StatusService
import kotlinx.coroutines.launch
import java.io.IOException

data class Car(
    val id: String? = null,
    val imageUrl: String,
    val year: String,
    val name: String,
    val licence: String,
    val lat: Double,
    val long: Double
)

class CarsViewModel : ViewModel() {
    var carsList by mutableStateOf<List<Car>>(emptyList())
        private set

    var carsState: StatusService<*> by mutableStateOf(StatusService.Idle)
        private set

    fun reset() {
        carsState = StatusService.Idle
        carsList = emptyList()
    }

    fun getCars() {
        carsState = StatusService.Loading
        viewModelScope.launch {
            try {
                val service = ServiceApi.retrofitService
                val carsResponse = service.getCars()
                if (carsResponse.isSuccessful) {
                    val carsData = carsResponse.body()

                    // Convert API response to Car objects
                    carsList = carsData?.map { carResponse ->
                        Car(
                            id = carResponse.id,
                            imageUrl = carResponse.imageUrl ?: "",
                            year = carResponse.year ?: "",
                            name = carResponse.name ?: "",
                            licence = carResponse.licence ?: "",
                            lat = carResponse.place?.lat ?: 0.0,
                            long = carResponse.place?.long ?: 0.0
                        )
                    } ?: emptyList()

                    carsState = StatusService.Success(carsData)
                } else {
                    carsState = StatusService.UnknownError
                }
            } catch (error: IOException) {
                carsState = StatusService.UnknownError
            }
        }
    }

    // Helper functions to get specific car data if needed
    fun getCarAt(index: Int): Car? {
        return if (index in carsList.indices) carsList[index] else null
    }

    fun getCarByLicence(licence: String): Car? {
        return carsList.find { it.licence == licence }
    }
}