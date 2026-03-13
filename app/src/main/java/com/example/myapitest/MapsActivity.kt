package com.example.myapitest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapitest.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    // AO CLICAR EM CADA ITEM DE CARROS ABRE O MAPA MOSTRANDO O LOCAL DO CARRO

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val lat = intent.getDoubleExtra("lat", 0.0)
        val long = intent.getDoubleExtra("long", 0.0)

        val location = LatLng(lat, long)

        mMap.addMarker(
            MarkerOptions()
                .position(location)
                .title("Local do carro")
        )

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 12f))
    }
}