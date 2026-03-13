package com.example.myapitest

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.lifecycleScope
import com.example.myapitest.adapter.CarAdapter
import com.example.myapitest.api.RetrofitClient
import com.example.myapitest.databinding.ActivityMainBinding
import com.example.myapitest.model.Car
import com.example.myapitest.model.Place
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        requestLocationPermission()
        setupView()
    }

    override fun onResume() {
        super.onResume()
        fetchItems()
    }

    private fun setupView() {
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchItems()
        }

        binding.addCta.setOnClickListener {
            val newCar = Car(
                id = System.currentTimeMillis().toString(),
                name = "Novo Carro",
                year = "2023/2023",
                licence = "XYZ-1234",
                imageUrl = "https://cdn.pixabay.com/photo/2017/01/06/19/15/car-1952096_1280.jpg",
                place = Place(-23.5505, -46.6333)
            )

            lifecycleScope.launch {
                try {
                    RetrofitClient.api.addCar(newCar)
                    fetchItems()
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Erro ao adicionar carro", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun requestLocationPermission() {
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
        }
    }

    private fun fetchItems() {
        lifecycleScope.launch {
            try {
                val cars = RetrofitClient.api.getCars()
                binding.recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
                binding.recyclerView.adapter = CarAdapter(cars)
                binding.swipeRefreshLayout.isRefreshing = false
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@MainActivity,
                    "Erro ao carregar carros: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }
}