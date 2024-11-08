package com.example.myapitest.model

data class Item(
    val id: String,
    val imageUrl: String,
    val year: String,
    val name: String,
    val licence: String,
    val place: Place
)

data class SingleItem(
    val id: String,
    val value: Item,
)

data class Place(
    val lat: Float,
    val long: Float
)