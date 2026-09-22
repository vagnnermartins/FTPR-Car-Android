package com.example.myapitest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapitest.databinding.ActivityDetailBinding
import com.example.myapitest.model.CarLocation
import com.example.myapitest.model.CarValue
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

class DetailActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var car: CarValue

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onInit()
    }

    private fun onInit() {
        binding.backButton.setOnClickListener { finish() }
        binding.saveButton.setOnClickListener { saveCar() }
        binding.deleteButton.setOnClickListener { deleteCar() }
        loadItem()
        setupGoogleMap()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (::car.isInitialized) {
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

    private fun setupGoogleMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    private fun saveCar() {
        if (!validateForm()) {
            Toast
                .makeText(
                    this@DetailActivity,
                    R.string.item_detail_update_car_form_invalid,
                    Toast.LENGTH_SHORT
                )
                .show()
            return
        }

        val model = binding.model.text.toString()
        val year = binding.year.text.toString()
        val licence = binding.license.text.toString()
        val carData = CarValue(
            id = car.id,
            imageUrl = car.imageUrl,
            year = year,
            name = model,
            licence = licence,
            // TODO - Update later
            place = CarLocation(
                lat = 1.0,
                long = 1.2,
            )
        )

        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.apiService.updateCar(car.id, carData) }
            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Success -> {
                        car = result.data.value
                        Toast
                            .makeText(
                                this@DetailActivity,
                                R.string.item_detail_update_car_success,
                                Toast.LENGTH_SHORT
                            ).show()
                    }

                    is Result.Error -> {
                        Toast
                            .makeText(
                                this@DetailActivity,
                                R.string.item_detail_update_car_error,
                                Toast.LENGTH_SHORT
                            ).show()
                    }
                }
            }
        }

    }

    private fun deleteCar() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.apiService.deleteCar(car.id) }
            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Success -> {
                        Toast
                            .makeText(
                                this@DetailActivity,
                                R.string.item_detail_delete_car_success,
                                Toast.LENGTH_SHORT
                            ).show()
                        finish()
                    }

                    is Result.Error -> {
                        Toast
                            .makeText(
                                this@DetailActivity,
                                R.string.item_detail_delete_car_error,
                                Toast.LENGTH_SHORT
                            ).show()
                    }
                }
            }
        }
    }

    private fun loadItem() {
        val itemId = intent.getStringExtra(ITEM_ID) ?: ""
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.apiService.getCar(itemId) }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Success -> {
                        val carData = result.data
                        car = carData.value
                        handleSuccess()
                    }

                    is Result.Error -> handleError()
                }
            }
        }
    }

    private fun handleSuccess() {
        binding.model.text = Editable.Factory.getInstance().newEditable(car.name)
        binding.year.text = Editable.Factory.getInstance().newEditable(car.year)
        binding.license.text = car.licence
        binding.image.loadUrl(car.imageUrl)
        loadItemLocationInGoogleMap()
    }

    private fun handleError() {
        Toast
            .makeText(
                this@DetailActivity,
                R.string.item_detail_load_car_error,
                Toast.LENGTH_SHORT
            )
            .show()
    }

    private fun validateForm(): Boolean {
        val model = binding.model.text.toString()
        val year = binding.year.text.toString()
        val licence = binding.license.text.toString()

        val areAllFieldsValid = model.isNotEmpty() && year.isNotEmpty() && licence.isNotEmpty()

        if (areAllFieldsValid) {
            binding.model.error = null
            binding.year.error = null
            binding.license.error = null
            return true
        }

        if (model.isEmpty()) {
            binding.model.error = getString(R.string.required_field)
        }
        if (year.isEmpty()) {
            binding.year.error = getString(R.string.required_field)
        }
        if (licence.isEmpty()) {
            binding.license.error = getString(R.string.required_field)
        }

        return false
    }


    companion object {
        const val ITEM_ID = "item_id"
        fun newIntent(
            context: Context, itemId: String,
        ) = Intent(context, DetailActivity::class.java).apply {
            putExtra(ITEM_ID, itemId)
        }
    }
}