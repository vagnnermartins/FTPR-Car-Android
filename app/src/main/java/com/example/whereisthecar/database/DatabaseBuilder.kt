package com.example.whereisthecar.database

import android.content.Context
import androidx.room.Room

object DatabaseBuilder {

    private var instance: AppDatabase? = null

    fun getInstance(context: Context? = null): AppDatabase {
        return instance ?: synchronized(this) {
            if (context == null) {
                throw Exception("Você precisa passar o Context para criar um DataBase")
            }
            val newInstance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database"
            ).build()
            instance = newInstance
            newInstance
        }
    }
}