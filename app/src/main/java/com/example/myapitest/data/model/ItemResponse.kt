package com.example.myapitest.data.model

import com.google.gson.annotations.SerializedName

// RZ - Modelo para representar o envelope da API (Item)
// A API retorna um objeto com "id" e "value", onde "value" contém os dados do carro.
data class ItemResponse(
    @SerializedName("id") val id: String? = null,
    @SerializedName("value") val value: Car
)
