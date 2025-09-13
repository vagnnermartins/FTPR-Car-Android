package com.example.whereisthecar.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.whereisthecar.database.converter.DateConverters
import com.example.whereisthecar.database.dao.CarLocationDao
import com.example.whereisthecar.database.model.CarLocation

@Database(entities = [CarLocation::class], version = 1)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun carLocationDao(): CarLocationDao
}