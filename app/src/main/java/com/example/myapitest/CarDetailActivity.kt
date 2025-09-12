package com.example.myapitest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.myapitest.databinding.ActivityCarDetailBinding
import com.example.myapitest.model.Car
import com.example.myapitest.service.Result
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.safeApiCall
import com.example.myapitest.ui.loadUrl
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CarDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityCarDetailBinding
    private lateinit var car: Car
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarDetailBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        setupView()
        loadItem()
        setupGoogleMap()

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (::car.isInitialized) {
            // Se o item já foi carregado por nossa chamada no BackEnd
            // Carregue o item no Map
            loadItemLocationInGoogleMap()
        }


    }

    private fun loadItemLocationInGoogleMap() {
        car.place.apply {
            binding.googleMapContent.visibility = View.VISIBLE
            val latLong = LatLng(lat, long)
            mMap.addMarker(
                MarkerOptions()
                    .position(latLong)
            )
            mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    latLong,
                    15f
                )
            )
        }
    }



    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.deleteCTA.setOnClickListener {
            deleteItem()
        }
        binding.editCTA.setOnClickListener {
            editItem()
        }
    }

    private fun setupGoogleMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    private fun loadItem() {
        val itemId = intent.getStringExtra(ARG_ID) ?: ""

        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.apiService.getCarById(itemId) }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Success -> {
                        car = result.data.value
                        handleSuccess()

                    }

                    is Result.Error -> {
                        hadleError()
                    }
                }
            }
        }
    }

    private fun deleteItem() {
        CoroutineScope(Dispatchers.IO).launch {
           val result = safeApiCall { RetrofitClient.apiService.deleteById(car.id) }

            withContext(Dispatchers.Main){
                when (result){
                    is Result.Error -> {
                        Toast.makeText(this@CarDetailActivity, R.string.erro_delete, Toast.LENGTH_SHORT).show()
                    }
                    is Result.Success -> {
                        Toast.makeText(this@CarDetailActivity, R.string.success_delete, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }

        }
    }

    private fun editItem() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall {
                RetrofitClient.apiService.updateItem(
                    car.id,
                    car.copy(
                        name = binding.etName.text.toString(),
                        year = binding.etYear.text.toString(),
                        licence = binding.etLicense.text.toString()
                    )
                )
            }

            Log.d("TAG", "editItem: $result")

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Error -> {
                        Toast.makeText(
                            this@CarDetailActivity,
                            R.string.error_update,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is Result.Success -> {
                        Toast.makeText(
                            this@CarDetailActivity,
                            R.string.success_update,
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun handleSuccess() {
        binding.etYear.setText(car.year)
        binding.etName.setText(car.name)
        binding.etLicense.setText(car.licence)
        binding.imageUrl.loadUrl(car.imageUrl)
        loadItemLocationInGoogleMap()
    }

    private fun hadleError() {

    }


    companion object {
        private const val ARG_ID = "ARG_ID"
        fun newIntent(context: Context, id: String): Intent {
            return Intent(context, CarDetailActivity::class.java).apply {
                putExtra(ARG_ID, id)
            }
        }
    }
}