package com.example.myapitest.model

data class ItemGetId(
    val id: String,
    val value: CarDetails
)

data class CarDetails(
    val imageUrl: String,
    val year: String,
    val name: String,
    val licence: String,
    val place: PlaceData?
)

data class PlaceData(
    val lat: String?,
    val long: String?
)
