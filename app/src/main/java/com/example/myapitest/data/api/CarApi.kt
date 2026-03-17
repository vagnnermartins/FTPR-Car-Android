package com.example.myapitest.data.api

import com.example.myapitest.data.model.Car
import com.example.myapitest.data.model.ItemResponse
import retrofit2.Response
import retrofit2.http.*

// RZ - Interface da API atualizada para suportar o CRUD completo (GET, POST, PATCH, DELETE)
interface CarApi {

    @GET("items")
    suspend fun fetchCars(): Response<List<ItemResponse>>

    @POST("items")
    suspend fun saveCar(@Body item: ItemResponse): Response<ItemResponse>

    @PATCH("items/{id}")
    suspend fun updateCar(@Path("id") id: String, @Body item: ItemResponse): Response<ItemResponse>

    @DELETE("items/{id}")
    suspend fun deleteCar(@Path("id") id: String): Response<Unit>
}
