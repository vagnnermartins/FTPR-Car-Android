package com.example.myapitest

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.myapitest.databinding.ActivityCarFormBinding
import com.example.myapitest.model.Car
import com.example.myapitest.model.Place
import com.example.myapitest.viewmodel.CarViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.UUID

class CarFormActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityCarFormBinding
    private val viewModel: CarViewModel by viewModels()
    private var selectedImageUri: Uri? = null
    private var googleMap: GoogleMap? = null
    private var selectedLat: Double = 0.0
    private var selectedLng: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupListeners()
        
        // Map will be setup in Phase 4
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        
        // Default location (e.g., São Paulo)
        val defaultLoc = LatLng(-23.5505, -46.6333)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLoc, 10f))

        googleMap?.setOnMapClickListener { latLng ->
            googleMap?.clear()
            googleMap?.addMarker(MarkerOptions().position(latLng).title("Local do Veículo"))
            
            selectedLat = latLng.latitude
            selectedLng = latLng.longitude
            binding.textSelectedLocation.text = "Lat: ${String.format("%.4f", latLng.latitude)}, Lng: ${String.format("%.4f", latLng.longitude)}"
        }
    }

    private fun setupToolbar() {
        // Assume no action bar is set from the Theme, but we might want a manual Back arrow.
        // For now, relying on native back button is sufficient until fully styled.
    }

    private fun setupListeners() {
        binding.buttonSave.setOnClickListener {
            saveCar()
        }

        binding.imageContainer.setOnClickListener {
            // Launch the photo picker
            imagePickerLauncher.launch("image/*")
        }
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.imageViewCar.setImageURI(it)
            binding.iconAddPhoto.visibility = View.GONE
        }
    }

    private fun saveCar() {
        val name = binding.editTextName.text.toString()
        val year = binding.editTextYear.text.toString()
        val licence = binding.editTextLicence.text.toString()

        if (name.isBlank() || year.isBlank() || licence.isBlank()) {
            Toast.makeText(this, "Preencha os campos obrigatórios", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.buttonSave.isEnabled = false

        // Map selected coordinates to Place
        val car = Car(
            id = UUID.randomUUID().toString(),
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/ftpr-car.appspot.com/o/placeholder.png?alt=media",
            year = year,
            name = name,
            licence = licence,
            place = Place(lat = selectedLat, long = selectedLng)
        )

        // Pass the selectedImageUri to the view model
        viewModel.createCar(car, selectedImageUri) { success, errorMessage ->
            runOnUiThread {
                binding.progressBar.visibility = View.GONE
                binding.buttonSave.isEnabled = true

                if (success) {
                    Toast.makeText(this, "Salvo com sucesso!", Toast.LENGTH_SHORT).show()
                    finish() // Return to MainActivity
                } else {
                    Toast.makeText(this, "Erro ao salvar: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
