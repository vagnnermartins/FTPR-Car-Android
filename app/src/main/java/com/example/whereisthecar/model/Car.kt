package com.example.whereisthecar.model

data class CarGetResponse(
    val id: String,
    val value: Car
)

data class Car(
    val id: String,
    val name: String,
    val licence: String,
    val imageUrl: String,
    val year: String,
    val place: CarLocation
)

data class CarLocation(
    val lat: Double,
    val long: Double,
    val name: String? = null
)

