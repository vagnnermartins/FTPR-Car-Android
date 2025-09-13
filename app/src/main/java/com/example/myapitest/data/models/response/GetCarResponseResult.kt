package com.example.myapitest.data.models.response

import kotlinx.serialization.Serializable

@Serializable
data class GetCarResponseResult(
    val id: String,
    val value: GetCarsResponseResult
)
