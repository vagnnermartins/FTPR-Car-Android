package com.example.myapitest.api

import com.example.myapitest.model.Car
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface CarApiService {
    @GET("car")
    suspend fun getCars(): List<Car>

    @Headers("Content-Type: application/json")
    @POST("car")
    suspend fun saveCar(@Body car: Car): Response<ResponseBody>
}
