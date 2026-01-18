package com.utfpr.psil.cartrack.ui.model

import com.utfpr.psil.cartrack.data.model.CarRequest

sealed class CarListUiState {
    object Loading : CarListUiState()
    data class Success(val cars: List<CarRequest>) : CarListUiState()
    data class Error(val message: String) : CarListUiState()
}