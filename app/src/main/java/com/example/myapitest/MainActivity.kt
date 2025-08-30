package com.example.myapitest

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import com.example.myapitest.adapter.CarAdapter
import com.example.myapitest.databinding.ActivityMainBinding
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.safeApiCall
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.myapitest.service.Result

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

        binding.logoutCta.setOnClickListener{
            onLogout()
        }

        binding.addCta.setOnClickListener{
            navigateToNewCar()
        }
    }
    private fun onLogout() {
        FirebaseAuth.getInstance().signOut()
        val intent = LoginActivity.newIntent(this)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun requestLocationPermission() {
        //Inicializar o FusedLocation
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getLastLocation()
            } else {
                Toast.makeText(this,
                    R.string.permissao_de_localizacao_negada, Toast.LENGTH_SHORT).show()
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
                //Pedindo a permissão do usuário para ACCESS_FINE_LOCATION
                locationPermissionLauncher.launch(ACCESS_FINE_LOCATION)
            }

            shouldShowRequestPermissionRationale(ACCESS_COARSE_LOCATION) -> {
                //Pedindo a permissão do usuário para ACCESS_COARSE_LOCATION
                locationPermissionLauncher.launch(ACCESS_COARSE_LOCATION)
            }

            else -> {
                //Fallback em caso de algum erro na verificação de permissão
                locationPermissionLauncher.launch(ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermission()
            return
        }
        fusedLocationProviderClient.lastLocation.addOnCompleteListener { task: Task<Location> ->
            if (task.isSuccessful) {
                val location = task.result
                if (location != null) {
                    Log.d("Hello World", "Lat: ${location.latitude} Long: ${location.longitude}")
                }
            } else {
                Toast.makeText(this, R.string.erro_desconhecido, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchItems() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall {
                RetrofitClient.apiService.getCars()
            }
            withContext(Dispatchers.Main) {
                binding.swipeRefreshLayout.isRefreshing = false
                when(result) {
                    is Result.Success -> {
                        val adapter = CarAdapter(result.data) { item ->
                            startActivity(CarDetailActivity.newIntent(this@MainActivity, item.id))
                        }
                        binding.recyclerView.adapter = adapter
                    }
                    is Result.Error -> {
                        Toast.makeText(this@MainActivity,
                            getString(R.string.erro_ao_buscar_carros), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun navigateToNewCar(){
        startActivity(NewCarActivity.newIntent(this))
    }

    companion object {
        fun newIntent(context: Context) = Intent (context, MainActivity::class.java)
    }
}
