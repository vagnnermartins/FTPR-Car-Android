package com.example.myapitest.services

import com.example.myapitest.model.Item
import com.example.myapitest.model.ItemGetId
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {


    @GET("car") //items pois é nome do método do get
    suspend fun getItems():List<Item>  //retorna uma Lista de objeto Item

    @GET("car/{id}")
    suspend fun getItem(@Path("id") id: String): ItemGetId

    @DELETE("car/{id}")
    suspend fun deleteItem(@Path("id") id: String): Item

    @POST("car")
    suspend fun addItem(@Body item: Item): Item

    @PATCH("car/{id}")
    suspend fun updateItem(@Path("id") id: String, @Body item: Item): Item


}