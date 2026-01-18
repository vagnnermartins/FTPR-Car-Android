package com.utfpr.psil.cartrack.ui

import android.app.Application
import com.google.android.libraries.places.api.Places
import com.utfpr.psil.cartrack.R
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CarTrackApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
    }
}