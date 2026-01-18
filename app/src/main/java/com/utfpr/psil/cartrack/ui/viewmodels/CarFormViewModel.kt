package com.utfpr.psil.cartrack.ui.viewmodels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.utfpr.psil.cartrack.data.model.CarLocation
import com.utfpr.psil.cartrack.data.model.CarRequest
import com.utfpr.psil.cartrack.data.repositories.CarImagesRepository
import com.utfpr.psil.cartrack.data.repositories.CarRepository
import com.utfpr.psil.cartrack.data.repositories.PlacesRepository
import com.utfpr.psil.cartrack.ui.model.CarFormUiState
import com.utfpr.psil.cartrack.ui.model.PlaceSuggestion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CarFormViewModel @Inject constructor(
    private val placesRepository: PlacesRepository,
    private val carImagesRepository: CarImagesRepository,
    private val carRepository: CarRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val carId: String? = savedStateHandle["carId"]

    private val _uiState = MutableStateFlow(CarFormUiState())
    val uiState: StateFlow<CarFormUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        carId?.let { id ->
            loadCarForEdit(id)
        }
    }

    fun onAddressInputChange(query: String) {
        searchJob?.cancel()

        _uiState.update { it.copy(addressInput = query) }
        if (query.length < 2) {
            _uiState.update { it.copy(addressSuggestions = emptyList()) }
            return
        }
        searchJob = viewModelScope.launch {
            delay(300L)
            _uiState.update { it.copy(isLoadingSuggestions = true) }
            try {
                val suggestions = placesRepository.getAutocompleteSuggestions(query)
                _uiState.update {
                    it.copy(
                        addressSuggestions = suggestions,
                        isLoadingSuggestions = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.localizedMessage) }
            }
        }
    }

    fun onSuggestionSelected(suggestion: PlaceSuggestion) {
        _uiState.update {
            it.copy(
                addressInput = suggestion.description,
                addressSuggestions = emptyList()
            )
        }
        viewModelScope.launch {
            val coordinates = placesRepository.getCoordinatesForPlace(suggestion.placeId)
            _uiState.update { it.copy(selectedCoordinates = coordinates) }
        }
    }

    fun onSaveCar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                var downloadUrl: String? = null
                uiState.value.photoBitmap?.let { bitmap ->
                    val bytes = bitmapToByteArray(bitmap)
                    val fileName = "${System.currentTimeMillis()}.jpg"
                    downloadUrl = carImagesRepository.uploadPhoto(
                        bytes, fileName
                    ).getOrThrow()
                }

                val uniqueId = UUID.randomUUID().toString()

                val newCar = CarRequest(
                    id = carId ?: uniqueId,
                    name = uiState.value.carName,
                    year = uiState.value.carYear,
                    licence = uiState.value.carPlate,
                    imageUrl = downloadUrl ?: uiState.value.imageUrl ?: "",
                    place = CarLocation(
                        lat = uiState.value.selectedCoordinates?.latitude ?: 0.0,
                        long = uiState.value.selectedCoordinates?.longitude ?: 0.0
                    )
                )

                if (carId == null) carRepository.addCar(newCar) else carRepository.updateCar(
                    carId,
                    newCar
                )

                _uiState.update { it.copy(isSuccess = true) }

            } catch (e: Exception) {
                val errorMsg = if (e.message?.contains("400") == true) {
                    "Erro de validação nos dados fornecidos. Por favor verifique os campos e preencha novamente."
                } else {
                    "Falha ao salvar: ${e.message}"
                }
                _uiState.update { it.copy(errorMessage = errorMsg) }
                Log.e("Error", "Falha ao salvar: ${e.message}")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
        return baos.toByteArray()
    }

    private fun loadCarForEdit(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val car = carRepository.getCarById(id)
                _uiState.update {
                    it.copy(
                        carName = car.value.name,
                        carYear = car.value.year,
                        carPlate = car.value.licence,
                        addressInput = placesRepository.getAddressFromCoordinates(
                            car.value.place.lat,
                            car.value.place.long
                        ) ?: "",
                        selectedCoordinates = LatLng(car.value.place.lat, car.value.place.long),
                        imageUrl = car.value.imageUrl,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message, isLoading = false) }
            }
        }
    }

    fun onPhotoCaptured(context: Context, uri: Uri) = viewModelScope.launch {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val originalBitmap = BitmapFactory.decodeStream(inputStream)

                if (originalBitmap != null) {
                    val correctedBitmap = rotateImageIfRequired(context, originalBitmap, uri)
                    _uiState.update { it.copy(photoBitmap = correctedBitmap) }
                    deleteTempFile(uri, context)
                }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = e.localizedMessage) }
        }
    }

    private fun rotateImageIfRequired(context: Context, img: Bitmap, selectedImage: Uri): Bitmap {
        val orientation = try {
            context.contentResolver.openInputStream(selectedImage)?.use { input ->
                val ei = ExifInterface(input)
                ei.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (_: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }

        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(img, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(img, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(img, 270f)
            else -> img
        }
    }

    private fun rotateImage(img: Bitmap, degree: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(degree)
        val rotatedImg = Bitmap.createBitmap(img,
            0, 0, img.width, img.height, matrix, true)
        img.recycle()
        return rotatedImg
    }

    fun createPhotUri(context: Context): Uri {
        val directory = File(context.filesDir, "Images")
        if (!directory.exists()) directory.mkdirs()
        val file = File(directory, "temp_capture.jpg")
        return FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file
        ) ?: Uri.EMPTY
    }

    private fun deleteTempFile(uri: Uri, context: Context) {
        try {
            context.contentResolver.delete(uri, null, null)
        } catch (e: Exception) {
            Log.e("CarForm", "Erro ao apagar arquivo temporário", e)
        }
    }


    fun onCarNameChange(name: String) = _uiState.update { it.copy(carName = name) }
    fun onCarYearChange(year: String) = _uiState.update { it.copy(carYear = year) }
    fun onCarPlateChange(plate: String) = _uiState.update { it.copy(carPlate = plate) }
    fun onDismissSuggestions() = _uiState.update { it.copy(addressSuggestions = emptyList()) }
    fun clearErrorMessage() = _uiState.update { it.copy(errorMessage = null) }
}
