package com.example.ftprcar.ui.car.list

import com.example.ftprcar.model.Car

data class CarListState(
    val loading: Boolean = false,
    val processed: Boolean = false,
    val error: String = "",
    val cars: List<Car> = emptyList(),
)