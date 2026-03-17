package com.example.myapitest.data.model

import com.google.gson.annotations.SerializedName

// RZ - Modelo de dados para representar um Carro na API.
data class Car(
    @SerializedName("id") val id: String? = null,
    @SerializedName("imageUrl") val imageUrl: String? = "",
    @SerializedName("year") val year: String? = "",
    @SerializedName("name") val name: String? = "",
    @SerializedName("licence") val licence: String? = "",
    @SerializedName("place") val place: Place? = Place(0.0, 0.0),
    
    // RZ - Campo para capturar o aninhamento da API. 
    // Removido @Transient para permitir que o App leia os dados corretamente.
    @SerializedName("value") 
    val nestedValue: Car? = null
)
