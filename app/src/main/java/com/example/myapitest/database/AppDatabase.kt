package com.example.myapitest.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapitest.database.converter.DateConverters
import com.example.myapitest.database.dao.UserLocationDao
import com.example.myapitest.database.model.UserLocation

@Database(entities = [UserLocation::class], version = 1)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userLocationDao(): UserLocationDao
}