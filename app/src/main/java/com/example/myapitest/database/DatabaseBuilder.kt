package com.example.myapitest.database

import android.content.Context
import androidx.room.Room
import com.example.myapitest.database.AppDatabase

object DatabaseBuilder {

    private var INSTANCE: AppDatabase? = null

    fun getInstance(context: Context? = null): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            if (context == null) {
                throw IllegalStateException("Context cannot be null")
            }

            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database"
            )
                .build()
            INSTANCE = instance
            instance
        }
    }
}