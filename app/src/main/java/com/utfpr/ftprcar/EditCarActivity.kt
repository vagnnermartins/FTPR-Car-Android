package com.utfpr.ftprcar

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.utfpr.ftprcar.databinding.ActivityEditCarBinding
import com.utfpr.ftprcar.model.Car
import com.utfpr.ftprcar.model.Place
import com.utfpr.ftprcar.repository.CarRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class EditCarActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CAR_ID      = "car_id"
        const val EXTRA_CAR_NAME    = "car_name"
        const val EXTRA_CAR_YEAR    = "car_year"
        const val EXTRA_CAR_LICENCE = "car_licence"
        const val EXTRA_CAR_IMAGE   = "car_image"
        const val EXTRA_CAR_LAT     = "car_lat"
        const val EXTRA_CAR_LONG    = "car_long"
    }

    private lateinit var binding: ActivityEditCarBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var storage: FirebaseStorage

    private val repository = CarRepository()
    private var selectedImageUri: Uri? = null

    // Original values from intent
    private var carId       = ""
    private var currentLat  = 0.0
    private var currentLong = 0.0
    private var existingImageUrl = ""

    // Image picker
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                selectedImageUri = it
                binding.ivCarImage.setImageURI(it)
            }
        }

    // Location permission
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
        binding = ActivityEditCarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Edit Car"

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        storage = FirebaseStorage.getInstance()

        // Load existing values
        carId            = intent.getStringExtra(EXTRA_CAR_ID) ?: ""
        existingImageUrl = intent.getStringExtra(EXTRA_CAR_IMAGE) ?: ""
        currentLat       = intent.getDoubleExtra(EXTRA_CAR_LAT, 0.0)
        currentLong      = intent.getDoubleExtra(EXTRA_CAR_LONG, 0.0)

        binding.etName.setText(intent.getStringExtra(EXTRA_CAR_NAME) ?: "")
        binding.etYear.setText(intent.getStringExtra(EXTRA_CAR_YEAR) ?: "")
        binding.etLicence.setText(intent.getStringExtra(EXTRA_CAR_LICENCE) ?: "")
        binding.tvLocation.text = "Lat: $currentLat  |  Long: $currentLong"

        if (existingImageUrl.isNotEmpty()) {
            Picasso.get()
                .load(existingImageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(binding.ivCarImage)
        }

        binding.btnPickImage.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding.btnGetLocation.setOnClickListener { requestLocation() }
        binding.btnSave.setOnClickListener { saveCar() }
        binding.btnDelete.setOnClickListener { confirmDelete() }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun requestLocation() {
        val fineOk   = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)   == PackageManager.PERMISSION_GRANTED
        val coarseOk = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (fineOk || coarseOk) fetchLocation()
        else locationPermissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ))
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        binding.tvLocation.text = "Getting location…"
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
                    binding.tvLocation.text = "Lat: $currentLat  |  Long: $currentLong (unchanged)"
                    Toast.makeText(this, "Could not get GPS fix. Location unchanged.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                binding.btnGetLocation.isEnabled = true
                Toast.makeText(this, "Location error: ${it.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveCar() {
        val name    = binding.etName.text.toString().trim()
        val year    = binding.etYear.text.toString().trim()
        val licence = binding.etLicence.text.toString().trim()

        if (name.isEmpty())    { binding.tilName.error    = "Required"; return }
        if (year.isEmpty())    { binding.tilYear.error    = "Required"; return }
        if (licence.isEmpty()) { binding.tilLicence.error = "Required"; return }

        // Require an image (either already existing or newly selected)
        if (existingImageUrl.isEmpty() && selectedImageUri == null) {
            Toast.makeText(this, "Please pick an image for the car.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.tilName.error    = null
        binding.tilYear.error    = null
        binding.tilLicence.error = null

        showProgress(true)

        lifecycleScope.launch {
            try {
                val imageUrl = if (selectedImageUri != null) uploadImage() else existingImageUrl

                val car = Car(
                    id       = carId,
                    imageUrl = imageUrl,
                    year     = year,
                    name     = name,
                    licence  = licence,
                    place    = Place(lat = currentLat, lng = currentLong)
                )

                repository.updateCar(carId, car)
                Toast.makeText(this@EditCarActivity, "Car updated!", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@EditCarActivity, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            } finally {
                showProgress(false)
            }
        }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Delete Car")
            .setMessage("Are you sure you want to delete \"${binding.etName.text}\"? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> deleteCar() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteCar() {
        showProgress(true)
        lifecycleScope.launch {
            try {
                repository.deleteCar(carId)
                Toast.makeText(this@EditCarActivity, "Car deleted.", Toast.LENGTH_SHORT).show()
                // Return to MainActivity and clear intermediate activities
                val intent = android.content.Intent(this@EditCarActivity, MainActivity::class.java)
                intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@EditCarActivity, "Error deleting: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                showProgress(false)
            }
        }
    }

    private suspend fun uploadImage(): String {
        val uri = selectedImageUri ?: return existingImageUrl
        val ref = storage.reference.child("cars/${UUID.randomUUID()}")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    private fun showProgress(show: Boolean) {
        binding.progressBar.visibility    = if (show) View.VISIBLE else View.GONE
        binding.btnSave.isEnabled         = !show
        binding.btnDelete.isEnabled       = !show
        binding.btnPickImage.isEnabled    = !show
        binding.btnGetLocation.isEnabled  = !show
    }
}
