package com.example.myapitest.model

data class CarDto(
    val id: String,
    val imageUrl: String,
    val year: String,
    val name: String,
    val licence: String,
    val place: PlaceDto
)

data class CarResponse(
    val id: String,
    val value: CarDto
)

data class PlaceDto(
    val lat: Double,
    val long: Double
)