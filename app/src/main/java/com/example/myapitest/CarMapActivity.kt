package com.example.myapitest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapitest.databinding.ActivityCarMapBinding
import com.example.myapitest.model.Place
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

// ... imports ...

class CarMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityCarMapBinding
    private var place: Place? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Recuperar o place com compatibilidade para versões novas do Android
        place = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("place", Place::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra("place")
        }

        // Inicializar o MapView
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this) // Usa o onMapReady abaixo
    }

    override fun onMapReady(googleMap: GoogleMap) {
        place?.let {
            val location = LatLng(it.lat, it.long)

            // Adiciona o marcador
            googleMap.addMarker(MarkerOptions()
                .position(location)
                .title("Local do Carro"))

            // Move a câmera
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))

            // Força a atualização da UI do mapa
            googleMap.uiSettings.isZoomControlsEnabled = true
        } ?: run {
            // Log de erro caso o place venha nulo
            android.util.Log.e("MAP_ERROR", "Objeto Place está nulo!")
        }
    }


    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}