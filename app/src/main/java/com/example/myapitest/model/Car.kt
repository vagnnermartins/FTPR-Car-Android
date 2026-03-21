package com.example.myapitest.model

import com.google.gson.annotations.SerializedName

data class Car(
    @SerializedName(value = "id", alternate = ["_id"])
    val id: String? = null,
    
    @SerializedName(value = "imageUrl", alternate = ["image"])
    val imageUrl: String,
    
    @SerializedName("year")
    val year: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName(value = "licence", alternate = ["license"])
    val licence: String,
    
    @SerializedName("place")
    val place: Place
)

data class Place(
    @SerializedName("lat")
    val lat: Double,
    
    @SerializedName(value = "long", alternate = ["lng"])
    val long: Double
)
