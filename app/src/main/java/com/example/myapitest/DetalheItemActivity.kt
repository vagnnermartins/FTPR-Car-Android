package com.example.myapitest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DetalheItemActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalhe_item)
    }

    companion object{
        private const val ARG_ID = "ARG_ID"
        fun newIntent(context: Context, idItem: String) =
            Intent(context, DetalheItemActivity::class.java).apply {
                putExtra(ARG_ID, idItem)
            }
    }
}