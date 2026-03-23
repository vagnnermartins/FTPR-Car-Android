package com.utfpr.ftprcar

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.utfpr.ftprcar.databinding.ActivityAddCarBinding
import com.utfpr.ftprcar.model.Car
import com.utfpr.ftprcar.model.Place
import com.utfpr.ftprcar.repository.CarRepository
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class AddCarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCarBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var storage: FirebaseStorage

    private val repository = CarRepository()
    private var selectedImageUri: Uri? = null
    private var currentLat: Double = 0.0
    private var currentLong: Double = 0.0

    // Image picker launcher
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                selectedImageUri = it
                binding.ivCarImage.setImageURI(it)
            }
        }

    // Location permission launcher
    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
            if (perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            ) {
                fetchLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Car"

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        storage = FirebaseStorage.getInstance()

        binding.btnPickImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnGetLocation.setOnClickListener {
            requestLocation()
        }

        binding.btnSave.setOnClickListener {
            saveCar()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun requestLocation() {
        val fineGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            fetchLocation()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        binding.tvLocation.text = "Getting location..."
        binding.btnGetLocation.isEnabled = false

        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        fusedLocationClient.getCurrentLocation(request, null)
            .addOnSuccessListener { location ->
                binding.btnGetLocation.isEnabled = true
                if (location != null) {
                    currentLat  = location.latitude
                    currentLong = location.longitude
                    binding.tvLocation.text = "Lat: $currentLat  |  Long: $currentLong"
                } else {
                    // Emulator with no mock location set — use São Paulo as default
                    currentLat  = -23.5505
                    currentLong = -46.6333
                    binding.tvLocation.text = "Default: Lat: $currentLat  |  Long: $currentLong"
                    Toast.makeText(
                        this,
                        "Could not get GPS fix. Using default location (São Paulo).\nOn emulator: set a location in Extended Controls (\u22EF → Location).",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addOnFailureListener {
                binding.btnGetLocation.isEnabled = true
                binding.tvLocation.text = "Location failed"
                Toast.makeText(this, "Location error: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveCar() {
        val name    = binding.etName.text.toString().trim()
        val year    = binding.etYear.text.toString().trim()
        val licence = binding.etLicence.text.toString().trim()

        if (name.isEmpty()) { binding.tilName.error = "Required"; return }
        if (year.isEmpty()) { binding.tilYear.error = "Required"; return }
        if (licence.isEmpty()) { binding.tilLicence.error = "Required"; return }
        
        if (selectedImageUri == null) {
            Toast.makeText(this, "Please pick an image for the car.", Toast.LENGTH_SHORT).show()
            return
        }
        
        binding.tilName.error    = null
        binding.tilYear.error    = null
        binding.tilLicence.error = null

        showProgress(true)

        lifecycleScope.launch {
            try {
                val imageUrl = uploadImageIfSelected()
                val carId = UUID.randomUUID().toString()

                val car = Car(
                    id       = carId,
                    imageUrl = imageUrl,
                    year     = year,
                    name     = name,
                    licence  = licence,
                    place    = Place(lat = currentLat, lng = currentLong)
                )

                repository.addCar(car)
                Toast.makeText(this@AddCarActivity, "Car saved!", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                print(e.toString())
                Toast.makeText(
                    this@AddCarActivity,
                    "Error saving car: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                showProgress(false)
            }
        }
    }

    private suspend fun uploadImageIfSelected(): String {
        val uri = selectedImageUri ?: return ""
        val ref = storage.reference.child("cars/${UUID.randomUUID()}")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    private fun showProgress(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled = !show
        binding.btnPickImage.isEnabled = !show
        binding.btnGetLocation.isEnabled = !show
    }
}
