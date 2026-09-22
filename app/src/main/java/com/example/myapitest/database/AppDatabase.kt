package com.example.myapitest.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapitest.database.converter.DateConverters
import com.example.myapitest.database.dao.CarLocationDao
import com.example.myapitest.database.model.CarLocation

@Database(entities = [CarLocation::class], version = 1)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun carLocationDao(): CarLocationDao
}