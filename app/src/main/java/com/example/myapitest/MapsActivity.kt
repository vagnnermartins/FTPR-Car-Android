package com.example.myapitest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var lat: Double = 0.0
    private var long: Double = 0.0
    private var carName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        lat = intent.getDoubleExtra("LAT", 0.0)
        long = intent.getDoubleExtra("LONG", 0.0)
        carName = intent.getStringExtra("CAR_NAME") ?: "Carro"

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val location = LatLng(lat, long)
        mMap.addMarker(MarkerOptions().position(location).title(carName))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }
}