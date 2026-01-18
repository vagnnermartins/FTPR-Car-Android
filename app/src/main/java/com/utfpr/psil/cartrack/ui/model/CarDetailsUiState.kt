package com.utfpr.psil.cartrack.ui.model

import com.utfpr.psil.cartrack.data.model.CarRequest

data class CarDetailsUiState(
    val isLoading: Boolean = false,
    val car: CarRequest? = null,
    val error: String? = null,
    val address: String? = null
)
