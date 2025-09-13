package com.example.myapitest.ui.authenticaded.cars.addCar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapitest.data.models.request.CreateCarRequest
import com.example.myapitest.data.models.request.Place
import com.example.myapitest.network.FirebaseService
import com.example.myapitest.network.ServiceApi
import com.example.myapitest.network.StatusService
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

class AddCarViewModel : ViewModel() {
    var name by mutableStateOf("")
        private set

    var year by mutableStateOf("")
        private set

    var license by mutableStateOf("")
        private set

    var imageUrl by mutableStateOf("")
        private set

    var latitude by mutableDoubleStateOf(0.0)
        private set
    var longitude by mutableDoubleStateOf(0.0)
        private set

    fun updateName(name: String) {
        this.name = name
    }

    fun updateYear(year: String) {
        this.year = year
    }

    fun updateLicense(license: String) {
        this.license = license
    }

    fun updateImageUrl(imageUrl: String) {
        this.imageUrl = imageUrl
    }

    fun updateLocation(lat: Double, lng: Double) {
        latitude = lat
        longitude = lng
    }

    var additionState: StatusService<*> by mutableStateOf(StatusService.Idle)
        private set

    fun updateState(newState: StatusService<*>) {
        additionState = newState
    }

    fun addCar() {
        if (isFormInvalid()) {
            return
        }

        additionState = StatusService.Loading

        viewModelScope.launch {
            try {
                val body = formatApiBody()
                val service = ServiceApi.retrofitService
                val carCreationResponse = service.addCar(body)
                if (carCreationResponse.isSuccessful) {
                    additionState = StatusService.Success(carCreationResponse)
                } else {
                    additionState = StatusService.UnknownError
                }
            } catch (error: IOException) {
                additionState = StatusService.UnknownError
            }
        }
    }

    fun isFormInvalid (): Boolean {
        if (
            name.isEmpty() ||
            year.isEmpty() ||
            license.isEmpty() ||
            imageUrl.isEmpty()
        ) {
            additionState = StatusService.Error(1, "Please fill in all fields")
            return true
        }
        return false
    }

    fun formatApiBody(): CreateCarRequest {
        return CreateCarRequest(
            id = UUID.randomUUID().toString(),
            name = name,
            year = year,
            licence = license,
            imageUrl = imageUrl,
            place = Place(
                lat = latitude,
                long = longitude
            ),
        )
    }
}