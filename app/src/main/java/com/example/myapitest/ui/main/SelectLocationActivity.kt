package com.example.myapitest.ui.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.myapitest.R
import com.example.myapitest.databinding.ActivitySelectLocationBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng

// RZ - Activity para selecionar localização manualmente no mapa
class SelectLocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivitySelectLocationBinding
    private lateinit var mMap: GoogleMap
    private var initialLat: Double = 0.0
    private var initialLong: Double = 0.0

    // RZ - Coordenadas padrão: Cristo Redentor, Rio de Janeiro
    private val DEFAULT_LAT = -22.951916
    private val DEFAULT_LNG = -43.2104872

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // RZ - Tenta pegar a localização inicial passada pela tela anterior
        initialLat = intent.getDoubleExtra("LAT", 0.0)
        initialLong = intent.getDoubleExtra("LONG", 0.0)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // RZ - Botão de retorno caso o usuário desista
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnConfirmLocation.setOnClickListener {
            val center = mMap.cameraPosition.target
            val resultIntent = Intent().apply {
                putExtra("LAT", center.latitude)
                putExtra("LONG", center.longitude)
            }
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isZoomGesturesEnabled = true

        // RZ - Se uma localização inicial foi fornecida (diferente de 0), inicia o mapa nela.
        // Se não foi fornecida, usa o padrão (Cristo Redentor).
        val startLatLng = if (initialLat != 0.0 || initialLong != 0.0) {
            LatLng(initialLat, initialLong)
        } else {
            LatLng(DEFAULT_LAT, DEFAULT_LNG)
        }
        
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startLatLng, 15f))

        // RZ - Tenta mostrar a bolinha azul se tiver permissão, mas mantém o foco no ponto selecionado acima
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.isMyLocationEnabled = true
        }
    }
}
