package com.example.myapitest.domain.model

data class Car(
    val id: String,
    val imageUrl: String,
    val year: String,
    val name: String,
    val licence: String,
    val place: Place
)

data class Place(
    val lat: Double,
    val long: Double
)

data class CarResponse(
    val id: String,
    val value: Car
)

data class DeleteResponse(
    val message: String
)

data class ErrorResponse(
    val error: String,
    val errors: List<CarError>? = null
)

data class CarError(
    val car: Car? = null,
    val id: String? = null,
    val error: String
)