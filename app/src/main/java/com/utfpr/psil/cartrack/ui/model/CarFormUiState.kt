package com.utfpr.psil.cartrack.ui.model

import android.graphics.Bitmap
import com.google.android.gms.maps.model.LatLng

data class CarFormUiState(
    val carName: String = "",
    val carYear: String = "",
    val carPlate: String = "",
    val addressInput: String = "",
    val addressSuggestions: List<PlaceSuggestion> = emptyList(),
    val selectedCoordinates: LatLng? = null,
    val isLoadingSuggestions: Boolean = false,
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val photoBitmap: Bitmap? = null,
    val imageUrl: String? = null
)
