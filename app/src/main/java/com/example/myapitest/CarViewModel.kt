package com.example.myapitest

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapitest.models.ApiErrorResponse
import com.example.myapitest.models.Car
import com.example.myapitest.utils.CarsAPI
import com.example.myapitest.utils.NetworkUtils
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.util.UUID

class CarViewModel : ViewModel() {

    private val storage = Firebase.storage.reference
    var cars by mutableStateOf<List<Car>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val api: CarsAPI by lazy {
        val baseUrl = "http://192.168.2.191:3000/"
        NetworkUtils.getRetrofitInstance(baseUrl).create(CarsAPI::class.java)
    }

    init {
        fetchCars()
    }

    fun fetchCars() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                cars = api.getCars()
            } catch (e: Exception) {
                errorMessage = "Erro ao carregar carros: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun uploadImageToFirebase(uri: Uri, onResult: (String?) -> Unit) {
        val fileRef = storage.child("cars/${UUID.randomUUID()}.jpg")
        fileRef.putFile(uri)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    onResult(downloadUri.toString())
                }
            }
            .addOnFailureListener { onResult(null) }
    }

    fun saveCar(car: Car, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = api.addCar(listOf(car))

                if (response.isSuccessful) {
                    fetchCars()
                    onResult(null)
                } else {
                    val rawError = response.errorBody()?.string() ?: ""
                    try {
                        val apiError = Gson().fromJson(rawError, ApiErrorResponse::class.java)
                        val firstError = apiError?.errors?.firstOrNull()?.error
                        onResult(firstError ?: "Erro 400: $rawError")
                    } catch (e: Exception) {
                        onResult("Erro servidor: $rawError")
                    }
                }
            } catch (e: Exception) {
                onResult("Falha na conexão: ${e.message}")
            }
        }
    }
}
