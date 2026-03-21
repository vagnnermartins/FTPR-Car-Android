package com.example.myapitest

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View 
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapitest.adapter.CarAdapter
import com.example.myapitest.database.DatabaseBuilder
import com.example.myapitest.database.model.UserLocation
import com.example.myapitest.databinding.ActivityMainBinding
import com.example.myapitest.model.Car
import com.example.myapitest.service.Result
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.safeApiCall
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>

    private var carAdapter: CarAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        setupLocationClient()
    }

    override fun onResume() {
        super.onResume()
        fetchItems()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_loggout -> {
                onLoggout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onLoggout() {
        FirebaseAuth.getInstance().signOut()
        val intent = LoginActivity.newIntent(this)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchItems()
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        binding.addCta.setOnClickListener {
            navigateToNewItem()
        }
    }

    private fun navigateToNewItem() {
        val intent = Intent(this, NewCarActivity::class.java)
        startActivity(intent)
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getLastLocation()
            } else {
                Toast.makeText(this, getString(R.string.location_permission_required), Toast.LENGTH_SHORT).show()
            }
        }

        checkLocationPermissionAndRequest()
    }

    private fun checkLocationPermissionAndRequest() {
        when {
            ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED -> {
                getLastLocation()
            }
            else -> {
                locationPermissionLauncher.launch(ACCESS_FINE_LOCATION)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    saveLocationInDatabase(it.latitude, it.longitude)
                }
            }
        }
    }

    private fun saveLocationInDatabase(latitude: Double, longitude: Double) {
        lifecycleScope.launch(Dispatchers.IO) {
            val userLocation = UserLocation(
                latitude = latitude,
                longitude = longitude,
                createdAt = Date()
            )
            DatabaseBuilder.getInstance(this@MainActivity).userLocationDao().insert(userLocation)
        }
    }

    private fun fetchItems() {
        binding.swipeRefreshLayout.isRefreshing = true
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                safeApiCall { RetrofitClient.carApiService.getCars() }
            }

            when (result) {
                is Result.Success -> {
                    handleSuccess(result.data)
                }
                is Result.Error -> handleError(result.message)
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun handleSuccess(data: List<Car>) {
        // Filtrar itens nulos ou inválidos para manter a lista limpa
        val validCars = data.filter { it.name != null }

        if (validCars.isEmpty()) {
            binding.recyclerView.visibility = View.GONE
            binding.tvEmptyMessage.visibility = View.VISIBLE
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.tvEmptyMessage.visibility = View.GONE

            if (carAdapter == null) {
                carAdapter = CarAdapter(validCars) { car ->
                    val intent = CarDetailActivity.newIntent(this, car.id)
                    startActivity(intent)
                }
                binding.recyclerView.adapter = carAdapter
            } else {
                carAdapter?.setupData(validCars)
            }
        }
    }

    private fun handleError(message: String) {
        Log.e("MainActivity", "Error fetching cars: $message")
        // Se houver erro, também escondemos a lista para não mostrar
        binding.recyclerView.visibility = View.GONE
        binding.tvEmptyMessage.visibility = View.VISIBLE
    }
}