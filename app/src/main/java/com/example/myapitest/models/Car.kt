package com.example.myapitest.models

import java.time.Year

data class Car(
    val id: String,
    val imageUrl: String,
    val name: String,
    val year: Year,
    val licence: String,
) {

}