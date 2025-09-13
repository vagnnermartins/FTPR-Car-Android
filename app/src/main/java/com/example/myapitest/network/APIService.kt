package com.example.myapitest.network

import com.example.myapitest.data.models.request.CreateCarRequest
import com.example.myapitest.data.models.response.CreateCarResponseResult
import com.example.myapitest.data.models.response.GetCarResponseResult
import com.example.myapitest.data.models.response.GetCarsResponseResult
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

private const val BASE_URL = "http://10.0.2.2:3000/"

private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

interface APIService {
    @GET("car")
    suspend fun getCars(): Response<List<GetCarsResponseResult>>
    @POST("car")
    suspend fun addCar(@Body createCarRequest: CreateCarRequest): Response<CreateCarResponseResult>
    @GET("car/{id}")
    suspend fun getCarById(@Path("id") carId: String): Response<GetCarResponseResult>
    @PATCH("car/{id}")
    suspend fun updateCar(@Path("id") carId: String, @Body createCarRequest: CreateCarRequest): Response<CreateCarResponseResult>
    @DELETE("car/{id}")
    suspend fun deleteCar(@Path("id") carId: String): Response<Unit>
}

object ServiceApi {
    val retrofitService : APIService by lazy {
        retrofit.create(APIService::class.java)
    }
}