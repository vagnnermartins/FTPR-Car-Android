package com.example.myapitest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapitest.databinding.ActivityNewItemBinding
import com.example.myapitest.model.Item
import com.example.myapitest.model.Place
import com.example.myapitest.service.Result
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.safeApiCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.SecureRandom

class NewItemActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewItemBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.saveCta.setOnClickListener {
            saveCar()
        }
    }

    private fun saveCar() {
        if (validateForm()) {
            CoroutineScope(Dispatchers.IO).launch {
                val id = SecureRandom().nextInt().toString()
                val result = safeApiCall {
                    RetrofitClient.apiService.createItem(Item(
                        id = id,
                        name = binding.name.text.toString(),
                        year = binding.year.text.toString(),
                        place = Place(
                            long = 0F,
                            lat = 0F
                        ),
                        licence = binding.licence.text.toString(),
                        imageUrl = binding.imageUrl.text.toString()
                    ))
                }

                withContext(Dispatchers.Main) {
                    when (result) {
                        is Result.Error -> {
                            Toast.makeText(this@NewItemActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
                        }
                        is Result.Success -> {
                            finish()
                        }
                    }
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, NewItemActivity::class.java)
    }

    private fun validateForm(): Boolean {
        if (binding.name.text.toString().isBlank()) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show()
            return false
        }

        if (binding.year.text.toString().isBlank()){
            Toast.makeText(this, "Year is required", Toast.LENGTH_SHORT).show()
            return false
        }

        if (binding.licence.text.toString().isBlank()){
            Toast.makeText(this, "Licence is required", Toast.LENGTH_SHORT).show()
            return false
        }

        if (binding.imageUrl.text.toString().isBlank()){
            Toast.makeText(this, "Image is required",Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}