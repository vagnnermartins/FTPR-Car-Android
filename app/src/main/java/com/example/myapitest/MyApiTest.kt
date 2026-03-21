package com.example.myapitest

import android.app.Application
import com.example.myapitest.database.DatabaseBuilder

class MyApiTest: Application() {
    override fun onCreate() {
        super.onCreate()

        DatabaseBuilder.getInstance(this)
    }
}