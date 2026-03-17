package com.example.myapitest

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.myapitest.databinding.ActivityCarFormBinding
import com.example.myapitest.model.Car
import com.example.myapitest.model.Place
import com.example.myapitest.utils.MaskWatcher
import com.example.myapitest.viewmodel.CarViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import java.io.File
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
    private var isLocationSelected: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupListeners()
        
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        
        val defaultLoc = LatLng(-23.5505, -46.6333)
        googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLoc, 10f))

        googleMap?.setOnMapClickListener { latLng ->
            googleMap?.clear()
            googleMap?.addMarker(MarkerOptions().position(latLng).title("Local do Veículo"))
            
            selectedLat = latLng.latitude
            selectedLng = latLng.longitude
            isLocationSelected = true
            binding.textSelectedLocation.text = "Lat: ${String.format("%.4f", latLng.latitude)}, Lng: ${String.format("%.4f", latLng.longitude)}"
        }
    }

    private fun setupToolbar() {
    }

    private fun setupListeners() {
        binding.editTextYear.addTextChangedListener(MaskWatcher("####/####"))
        binding.editTextLicence.addTextChangedListener(MaskWatcher("AAA-####"))

        binding.buttonSave.setOnClickListener {
            saveCar()
        }

        binding.imageContainer.setOnClickListener {
            showImagePickerOptions()
        }
    }

    private fun showImagePickerOptions() {
        val options = arrayOf("Câmera", "Galeria")
        AlertDialog.Builder(this)
            .setTitle("Escolha uma opção")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openGallery() {
        imagePickerLauncher.launch("image/*")
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(this, "Permissão de câmera negada", Toast.LENGTH_SHORT).show()
        }
    }

    private fun launchCamera() {
        val photoFile = File(cacheDir, "camera_images").apply { mkdirs() }
        val file = File(photoFile, "${UUID.randomUUID()}.jpg")
        val uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
        selectedImageUri = uri
        
        takePictureLauncher.launch(uri)
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            binding.imageViewCar.setImageURI(selectedImageUri)
            binding.iconAddPhoto.visibility = View.GONE
        } else {
            selectedImageUri = null
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

        if (!isLocationSelected) {
            Toast.makeText(this, "Selecione uma localização no Mapa", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.buttonSave.isEnabled = false

        val car = Car(
            id = UUID.randomUUID().toString(),
            imageUrl = "https://firebasestorage.googleapis.com/v0/b/ftpr-car.appspot.com/o/placeholder.png?alt=media",
            year = year,
            name = name,
            licence = licence,
            place = Place(lat = selectedLat, long = selectedLng)
        )

        viewModel.createCar(car, selectedImageUri) { success, errorMessage ->
            runOnUiThread {
                binding.progressBar.visibility = View.GONE
                binding.buttonSave.isEnabled = true

                if (success) {
                    Toast.makeText(this, "Salvo com sucesso!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Erro ao salvar: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
