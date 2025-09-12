package com.example.myapitest.database

import android.content.Context
import androidx.room.Room

object DatabaseBuilder {
    private var instance: AppDatabase? = null

    fun getInstance(context: Context? = null): AppDatabase {
        return instance ?: synchronized(this) {

            if (context == null){
                throw IllegalStateException("Precisa passar o Context para criar um database")
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