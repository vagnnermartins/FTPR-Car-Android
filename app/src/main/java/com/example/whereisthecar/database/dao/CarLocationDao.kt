package com.example.whereisthecar.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.whereisthecar.database.model.CarLocation

@Dao
interface CarLocationDao {

    @Insert
    suspend fun insert(carLocation: CarLocation)

    @Query("SELECT * FROM car_location_table")
    suspend fun getAllCarLocation(): List<CarLocation>?

    @Query("SELECT * FROM car_location_table ORDER BY id DESC LIMIT 1")
    suspend fun getLastLocation(): CarLocation?
}