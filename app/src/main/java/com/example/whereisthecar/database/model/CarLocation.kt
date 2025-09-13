package com.example.whereisthecar.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "car_location_table")
data class CarLocation(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var latitude: Double = 0.toDouble(),
    var longitude: Double = 0.toDouble(),
    var createdAt: Date = Date()
)