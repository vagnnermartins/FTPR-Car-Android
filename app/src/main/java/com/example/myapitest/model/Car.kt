package com.example.myapitest.model

import com.google.gson.annotations.SerializedName

data class Car(
    val id: String,
    val imageUrl: String? = null,
    val year: String? = null,
    val name: String? = null,
    val licence: String? = null,
    val place: Place? = null
)

data class Place(
    val lat: Double? = null,
    val long: Double? = null
)

data class CarDetailResponse(
    val id: String,
    val value: Car
)
