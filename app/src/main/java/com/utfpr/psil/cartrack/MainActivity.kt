package com.utfpr.psil.cartrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.utfpr.psil.cartrack.ui.CarTrackApp
import com.utfpr.psil.cartrack.ui.utils.setFullscreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setFullscreen(window)
        setContent {
            CarTrackApp()
        }
    }
}
