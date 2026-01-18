package com.utfpr.psil.cartrack.data.datasources

import com.utfpr.psil.cartrack.data.model.CarDetailResponse
import com.utfpr.psil.cartrack.data.model.CarRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface CarApiService {
    @GET("car")
    suspend fun getAllCars(): List<CarRequest>

    @GET("car/{id}")
    suspend fun getCarById(@Path("id") id: String): CarDetailResponse

    @POST("car")
    suspend fun addCar(@Body car: CarRequest): Response<Unit>

    @DELETE("car/{id}")
    suspend fun deleteCar(@Path("id") id: String): Response<Unit>

    @PATCH("car/{id}")
    suspend fun updateCar(@Path("id") id: String, @Body car: CarRequest): Response<Unit>
}