package com.example.myapitest.ui.authenticaded.cars.carDetails

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapitest.data.models.request.CreateCarRequest
import com.example.myapitest.data.models.request.Place
import com.example.myapitest.network.ServiceApi
import com.example.myapitest.network.StatusService
import com.example.myapitest.ui.authenticaded.cars.carsLIst.Car
import kotlinx.coroutines.launch

class CarDetailsViewModel : ViewModel() {

    var car by mutableStateOf<Car?>(null)

    var loadCarsState: StatusService<*> by mutableStateOf(StatusService.Idle)
        private set

    var carsUpdateState: StatusService<*> by mutableStateOf(StatusService.Idle)
        private set

    var deleteCarState: StatusService<*> by mutableStateOf(StatusService.Idle)
        private set

    fun updateCarName(newName: String) {
        car = car?.copy(name = newName)
    }

    fun updateCarYear(newYear: String) {
        car = car?.copy(year = newYear)
    }

    fun updateCarLicence(newLicence: String) {
        car = car?.copy(licence = newLicence)
    }

    fun updateCarLocation(newLat: Double, newLong: Double) {
        car = car?.copy(lat = newLat, long = newLong)
    }

    fun updateLoadState(newState: StatusService<*>) {
        loadCarsState = newState
    }

    fun updateCarsUpdateState(newState: StatusService<*>) {
        carsUpdateState = newState
    }

    fun updateDeleteCarState(newState: StatusService<*>) {
        deleteCarState = newState
    }

    fun loadCarDetails(carId: String) {
        loadCarsState = StatusService.Loading
        viewModelScope.launch {
            try {
                val service = ServiceApi.retrofitService
                val carResponse = service.getCarById(carId)
                if (carResponse.isSuccessful) {
                    val carData = carResponse.body()

                    car = Car(
                        id = carData?.id,
                        imageUrl = carData?.value?.imageUrl ?: "",
                        year = carData?.value?.year ?: "",
                        name = carData?.value?.name ?: "",
                        licence = carData?.value?.licence ?: "",
                        lat = carData?.value?.place?.lat ?: 0.0,
                        long = carData?.value?.place?.long ?: 0.0
                    )

                    loadCarsState = StatusService.Success(car)
                } else {
                    loadCarsState = StatusService.UnknownError
                }

            } catch (e: Exception) {
                loadCarsState = StatusService.UnknownError
            }
        }
    }

    fun updateCar() {
        if (isFormInvalid()) {
            return
        }

        carsUpdateState = StatusService.Loading

        viewModelScope.launch {
            try {
                val body = formatApiBody()
                val service = ServiceApi.retrofitService
                val updatedCarResponse = service.updateCar(car!!.id!!, body)

                if (updatedCarResponse.isSuccessful) {
                    carsUpdateState = StatusService.Success(updatedCarResponse)
                } else {
                    carsUpdateState = StatusService.UnknownError
                }
            } catch (e: Exception) {
                carsUpdateState = StatusService.UnknownError
            }
        }
    }

    fun deleteCar() {
        deleteCarState = StatusService.Loading
        viewModelScope.launch {
            try {
                val service = ServiceApi.retrofitService
                val deleteCarResponse = service.deleteCar(car!!.id!!)
                if (deleteCarResponse.isSuccessful) {
                    deleteCarState = StatusService.Success(deleteCarResponse)
                } else {
                    deleteCarState = StatusService.UnknownError
                }
                } catch (e: Exception) {
                deleteCarState = StatusService.UnknownError
            }
        }
    }

    fun isFormInvalid(): Boolean {
        if (
            car == null ||
            car!!.name.isEmpty() ||
            car!!.id == null ||
            car!!.id!!.isEmpty() ||
            car!!.year.isEmpty() ||
            car!!.licence.isEmpty() ||
            car!!.imageUrl.isEmpty()
        ) {
            carsUpdateState = StatusService.Error(1, "Please fill in all fields")
            return true
        }
        return false
    }

    fun formatApiBody(): CreateCarRequest {
        return CreateCarRequest(
            id = car!!.id!!,
            name = car!!.name,
            year = car!!.year,
            licence = car!!.licence,
            imageUrl = car!!.imageUrl,
            place = Place(
                lat = car!!.lat,
                long = car!!.long
            ),
        )
    }
}