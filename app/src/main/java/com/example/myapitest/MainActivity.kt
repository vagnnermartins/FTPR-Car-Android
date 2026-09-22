package com.example.myapitest

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapitest.adapter.ItemCarAdapter
import com.example.myapitest.database.DatabaseBuilder
import com.example.myapitest.database.model.CarLocation
import com.example.myapitest.databinding.ActivityMainBinding
import com.example.myapitest.service.Result
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.safeApiCall
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestLocationPermission()
        setupView()
    }

    override fun onResume() {
        super.onResume()
        fetchItems()
    }

    private fun setupView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = true
            fetchItems()
        }
        binding.logoutCta.setOnClickListener { logout() }
        binding.addCta.setOnClickListener { navigateToCreate() }
    }

    private fun navigateToCreate() {
        val intent = CreateActivity.newIntent(this)
        startActivity(intent)
    }

    private fun logout() {
        val auth = FirebaseAuth.getInstance()
        auth.signOut()
        val intent = LoginActivity.newIntent(this)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToDetail(id: String) {
        val intent = DetailActivity.newIntent(this@MainActivity, id)
        startActivity(intent)
    }

    private fun requestLocationPermission() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    getLastLocation()
                } else {
                    Toast.makeText(
                        this,
                        R.string.error_request_location_permission,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        checkLocationPermissionAndRequest()
    }

    private fun checkLocationPermissionAndRequest() {
        when {
            checkSelfPermission(ACCESS_FINE_LOCATION) == PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        this,
                        ACCESS_COARSE_LOCATION
                    ) == PERMISSION_GRANTED -> {
                getLastLocation()
            }

            shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION) -> {
                // Pedindo a permissão do usuário para ACCESS_FINE_LOCATION
                locationPermissionLauncher.launch(ACCESS_FINE_LOCATION)
            }

            shouldShowRequestPermissionRationale(ACCESS_COARSE_LOCATION) -> {
                // Pedindo a permissão do usuário para ACCESS_COARSE_LOCATION
                locationPermissionLauncher.launch(ACCESS_COARSE_LOCATION)
            }

            else -> {
                // Fallback em caso de algum erro na verificação da permissão
                locationPermissionLauncher.launch(ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun getLastLocation() {
        if (
            ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PERMISSION_GRANTED
        ) {
            requestLocationPermission()
            return
        }
        fusedLocationProviderClient.lastLocation.addOnCompleteListener { task: Task<Location?> ->
            if (task.isSuccessful) {
                val location = task.result
                if (location != null) {
                    Log.d("Hello World", "Lat: ${location.latitude} Long: ${location.longitude}")
                    val carLocation =
                        CarLocation(latitude = location.latitude, longitude = location.longitude)

                    CoroutineScope(Dispatchers.IO).launch {
                        DatabaseBuilder.getInstance()
                            .carLocationDao()
                            .insert(carLocation)
                    }
                } else {
                    Log.w("MainActivity", "Last known location is null")
                }
            } else {
                Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchItems() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.apiService.getCars() }

            withContext(Dispatchers.Main) {
                binding.swipeRefreshLayout.isRefreshing = false
                when (result) {
                    is Result.Success -> {
                        val adapter = ItemCarAdapter(result.data) { item ->
                            navigateToDetail(item.id)

                        }
                        binding.recyclerView.adapter = adapter
                    }

                    is Result.Error -> {
                        Toast.makeText(this@MainActivity, "Erro", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, MainActivity::class.java)
    }
}
