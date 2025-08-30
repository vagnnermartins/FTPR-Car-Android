package com.example.myapitest.model

data class Item(
    val id: String,
    val imageUrl: String,
    val year: String,
    val name: String,
    val licence: String,
    val place: Place?
)

data class Place(
    val lat: String?,
    val long: String?
)
