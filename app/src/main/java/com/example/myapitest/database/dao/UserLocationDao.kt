package com.example.myapitest.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapitest.database.model.UserLocation

@Dao
interface UserLocationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userLocation: UserLocation)

    @Query("SELECT * FROM user_location_table")
    suspend fun getAllUserLocations(): List<UserLocation>

    @Query("SELECT * FROM user_location_table ORDER BY id DESC LIMIT 1")
    suspend fun getLastLocation(): UserLocation?

}