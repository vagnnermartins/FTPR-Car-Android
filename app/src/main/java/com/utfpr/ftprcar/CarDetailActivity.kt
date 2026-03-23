package com.utfpr.ftprcar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.utfpr.ftprcar.databinding.ActivityCarDetailBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.squareup.picasso.Picasso
import android.content.Intent

class CarDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        const val EXTRA_CAR_ID      = "car_id"
        const val EXTRA_CAR_NAME    = "car_name"
        const val EXTRA_CAR_YEAR    = "car_year"
        const val EXTRA_CAR_LICENCE = "car_licence"
        const val EXTRA_CAR_IMAGE   = "car_image"
        const val EXTRA_CAR_LAT     = "car_lat"
        const val EXTRA_CAR_LONG    = "car_long"
    }

    private lateinit var binding: ActivityCarDetailBinding
    private var carLat: Double = 0.0
    private var carLong: Double = 0.0
    private var carName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnBack.setOnClickListener {
            finish()
        }

        carLat   = intent.getDoubleExtra(EXTRA_CAR_LAT, 0.0)
        carLong  = intent.getDoubleExtra(EXTRA_CAR_LONG, 0.0)
        carName  = intent.getStringExtra(EXTRA_CAR_NAME) ?: ""
        val year    = intent.getStringExtra(EXTRA_CAR_YEAR) ?: ""
        val licence = intent.getStringExtra(EXTRA_CAR_LICENCE) ?: ""
        val imageUrl = intent.getStringExtra(EXTRA_CAR_IMAGE) ?: ""

        supportActionBar?.title = carName

        binding.tvDetailName.text    = carName
        binding.tvDetailYear.text    = year
        binding.tvDetailLicence.text = licence

        if (imageUrl.isNotEmpty()) {
            Picasso.get()
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_gallery)
                .into(binding.ivDetailImage)
        }

        binding.btnEdit.setOnClickListener {
            val intent = Intent(this, EditCarActivity::class.java).apply {
                val carId = this@CarDetailActivity.intent.getStringExtra(EXTRA_CAR_ID) ?: ""
                putExtra(EditCarActivity.EXTRA_CAR_ID, carId)
                putExtra(EditCarActivity.EXTRA_CAR_NAME, carName)
                putExtra(EditCarActivity.EXTRA_CAR_YEAR, year)
                putExtra(EditCarActivity.EXTRA_CAR_LICENCE, licence)
                putExtra(EditCarActivity.EXTRA_CAR_IMAGE, imageUrl)
                putExtra(EditCarActivity.EXTRA_CAR_LAT, carLat)
                putExtra(EditCarActivity.EXTRA_CAR_LONG, carLong)
            }
            startActivity(intent)
        }

        // Initialize map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val carLocation = LatLng(carLat, carLong)
        googleMap.addMarker(
            MarkerOptions()
                .position(carLocation)
                .title(carName)
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(carLocation, 15f))
    }
}
