package com.example.whereisthecar

import android.app.Application
import com.example.whereisthecar.database.DatabaseBuilder

class Application : Application() {

    override fun onCreate() {
        super.onCreate()
        init()
    }

    /**
     * fun para inicializar nossas dependências através do nosso Context
     */
    private fun init() {
        DatabaseBuilder.getInstance(this)
    }
}