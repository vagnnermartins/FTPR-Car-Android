package com.example.myapitest.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapitest.data.repository.CarRepositoryImpl
import com.example.myapitest.data.repository.LocationRepositoryImpl
import com.example.myapitest.data.repository.StorageRepository
import com.example.myapitest.data.repository.StorageRepositoryImpl
import com.example.myapitest.domain.model.CarDetail
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class AddEditCarUiState(
    val name: String = "",
    val year: String = "",
    val licence: String = "",
    val imageUri: Uri? = null,
    val imageUrl: String = "",
    val location: LatLng? = null,
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val saveSuccess: Boolean = false,
    val screenTitle: String = "Adicionar Carro",

    val nameError: String? = null,
    val yearError: String? = null,
    val licenceError: String? = null,
    val locationError: String? = null,
    val imageError: String? = null,

    val error: String? = null
)

class AddEditCarViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val carRepository = CarRepositoryImpl()
    private val storageRepository: StorageRepository = StorageRepositoryImpl()

    private val _uiState = MutableStateFlow(AddEditCarUiState())
    val uiState = _uiState.asStateFlow()

    private val carId: String? = savedStateHandle["carId"]

    init {
        if (carId != null) {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    isEditing = true,
                    screenTitle = "Editar Carro"
                )
            }
            viewModelScope.launch {
                carRepository.getCarById(carId).onSuccess { car ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            name = car.name,
                            year = car.year,
                            licence = car.licence,
                            imageUrl = car.imageUrl,
                            location = LatLng(car.latitude, car.longitude)
                        )
                    }
                }
            }
        } else {
            _uiState.update { it.copy(isLoading = false, isEditing = false) }
        }
    }

    fun fetchCurrentUserLocation(context: Context) {
        val locationRepository = LocationRepositoryImpl(context)
        viewModelScope.launch {
            locationRepository.getCurrentLocation().onSuccess { latLng ->
                _uiState.update { it.copy(location = latLng) }
            }
        }
    }

    fun onNameChange(newName: String) {
        _uiState.update { it.copy(name = newName, nameError = null) }
    }

    fun onYearChange(newYear: String) {
        _uiState.update { it.copy(year = newYear, yearError = null) }
    }

    fun onLicenceChange(newLicence: String) {
        _uiState.update { it.copy(licence = newLicence, licenceError = null) }
    }

    fun onImagePicked(uri: Uri) {
        _uiState.update { it.copy(imageUri = uri, imageError = null) }
    }

    fun onLocationChange(latLng: LatLng) {
        _uiState.update { it.copy(location = latLng, locationError = null) }
    }

    private fun validateInputs(): Boolean {
        _uiState.update {
            it.copy(
                nameError = null,
                yearError = null,
                licenceError = null,
                locationError = null,
                imageError = null
            )
        }

        var isValid = true
        val currentState = _uiState.value

        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(nameError = "O nome não pode estar vazio") }
            isValid = false
        }

        if (currentState.year.isBlank()) {
            _uiState.update { it.copy(yearError = "Ano não pode estar vazio") }
            isValid = false
        }

        if (currentState.licence.isBlank()) {
            _uiState.update { it.copy(licenceError = "A placa não pode estar vazia") }
            isValid = false
        }

        if (currentState.location == null) {
            _uiState.update { it.copy(locationError = "Selecione uma localização no mapa") }
            isValid = false
        }

        if (!currentState.isEditing && currentState.imageUri == null) {
            _uiState.update { it.copy(imageError = "Selecione uma imagem") }
            isValid = false
        }

        return isValid
    }

    fun saveCar() {
        if (!validateInputs()) {
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            val imageUrlResult = if (_uiState.value.imageUri != null) {
                storageRepository.uploadImage(_uiState.value.imageUri!!)
            } else {
                Result.success(_uiState.value.imageUrl)
            }

            imageUrlResult.onSuccess { finalImageUrl ->
                val carDetail = CarDetail(
                    id = carId ?: UUID.randomUUID().toString(),
                    name = _uiState.value.name,
                    year = _uiState.value.year,
                    licence = _uiState.value.licence,
                    imageUrl = finalImageUrl,
                    latitude = _uiState.value.location!!.latitude,
                    longitude = _uiState.value.location!!.longitude
                )

                val saveResult = if (_uiState.value.isEditing) {
                    carRepository.updateCar(carDetail)
                } else {
                    carRepository.addCar(carDetail)
                }

                saveResult.onSuccess {
                    _uiState.update { it.copy(isLoading = false, saveSuccess = true) }
                }.onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Falha ao salvar dados: ${error.message}"
                        )
                    }
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Falha no upload da imagem: ${error.message}"
                    )
                }
            }
        }
    }
}