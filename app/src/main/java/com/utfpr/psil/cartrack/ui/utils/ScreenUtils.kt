package com.utfpr.psil.cartrack.ui.utils

import android.view.Window
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

fun setFullscreen(window: Window) {

    val windowInsetsController = WindowCompat.getInsetsController(
        window, window.decorView
    )
    windowInsetsController.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    windowInsetsController.hide(
        WindowInsetsCompat.Type.navigationBars()
    )
}