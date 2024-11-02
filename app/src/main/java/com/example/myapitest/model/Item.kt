package com.example.myapitest.model

data class Item(
    val id: String,
    val imageUrl: String,
    val year: String,
    val name: String,
    val licence: String,
    val place: ItemValue?
)

data class ItemValue(
    val lat: String?,
    val long: String?
)
