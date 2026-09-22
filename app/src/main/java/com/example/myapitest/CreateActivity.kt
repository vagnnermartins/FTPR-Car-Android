package com.example.myapitest

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import android.annotation.SuppressLint
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.myapitest.databinding.ActivityCreateBinding
import com.example.myapitest.model.CarLocation
import com.example.myapitest.model.CarValue
import com.example.myapitest.service.Result
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.safeApiCall
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.security.SecureRandom
import java.util.*

class CreateActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding: ActivityCreateBinding

    // Image - Firebase
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private var selectedImageUri: Uri? = null
    private var photoFile: File? = null
    private var uploadedImageUrl: String? = null
    private var pendingAction: String? = null

    // Location - Google maps
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap
    private var selectedMarker: Marker? = null

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri
                displaySelectedImage(uri)
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            photoFile?.let { file ->
                selectedImageUri = Uri.fromFile(file)
                displaySelectedImage(selectedImageUri!!)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        onInit()
    }

    private fun onInit() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        storage = FirebaseStorage.getInstance("gs://car-android-a0af7.firebasestorage.app")
        storageReference = storage.reference

        binding.addButton.setOnClickListener { onAddPressed() }
        binding.CancelButton.setOnClickListener { onCancelPressed() }
        binding.selectImageButton.setOnClickListener { selectImageFromGallery() }
        binding.takePhotoButton.setOnClickListener { takePhotoWithCamera() }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun selectImageFromGallery() {
        if (checkImagePermissions()) {
            launchGallery()
        } else {
            pendingAction = "gallery"
            requestImagePermissions()
        }
    }

    private fun launchGallery() {
        val intent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        galleryLauncher.launch(intent)
    }

    private fun takePhotoWithCamera() {
        if (checkImagePermissions()) {
            launchCamera()
        } else {
            pendingAction = "camera"
            requestImagePermissions()
        }
    }

    private fun launchCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        photoFile = createImageFile()
        photoFile?.let { file ->
            val photoURI = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                file
            )
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            cameraLauncher.launch(intent)
        }
    }

    private fun createImageFile(): File? {
        return try {
            val storageDir = getExternalFilesDir("Pictures")
            File.createTempFile(
                "car_image_${System.currentTimeMillis()}",
                ".jpg",
                storageDir
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun displaySelectedImage(uri: Uri) {
        binding.selectedImageView.visibility = View.VISIBLE
        binding.selectedImageView.setImageURI(uri)
    }

    @SuppressLint("InlinedApi")
    private fun checkImagePermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )

        // For Android 13+ (API 33+), check READ_MEDIA_IMAGES
        // For older versions, check READ_EXTERNAL_STORAGE
        val storagePermission =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_MEDIA_IMAGES
                )
            } else {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }

        val result =
            cameraPermission == PackageManager.PERMISSION_GRANTED &&
                    storagePermission == PackageManager.PERMISSION_GRANTED
        return result
    }

    @SuppressLint("InlinedApi")
    private fun requestImagePermissions() {
        val permissions =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_IMAGES
                )
            } else {
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            }

        ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    when (pendingAction) {
                        "gallery" -> launchGallery()
                        "camera" -> launchCamera()
                    }
                    pendingAction = null
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.create_all_permissions_denied),
                        Toast.LENGTH_LONG
                    ).show()
                    // Show which permissions were denied
                    for (i in permissions.indices) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            val permissionName = when (permissions[i]) {
                                Manifest.permission.CAMERA -> getString(R.string.create_permisson_camera)
                                Manifest.permission.READ_EXTERNAL_STORAGE -> getString(R.string.create_permission_storage)
                                Manifest.permission.READ_MEDIA_IMAGES -> getString(R.string.create_permission_gallery)
                                else -> permissions[i]
                            }
                            Toast.makeText(
                                this,
                                getString(R.string.create_permission_denied, permissionName),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            REQUEST_LOCATION_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadCurrentLocation()
                } else {
                    Toast.makeText(
                        this,
                        "Location permission denied. You can still select a location manually on the map.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


    @SuppressLint("Recycle")
    private fun uploadImageToFirebase(uri: Uri, callback: (String?) -> Unit) {
        val fileName = "car_images/${UUID.randomUUID()}.jpg"
        val imageRef = storageReference.child(fileName)

        try {
            val uploadTask = imageRef.putFile(uri)
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                Log.d("CreateActivity", "Upload progress: $progress%")
            }.addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    callback(downloadUri.toString())
                }.addOnFailureListener { exception ->
                    callback(null)
                }
            }.addOnFailureListener { exception ->
                callback(null)
            }

        } catch (e: Exception) {
            callback(null)
        }
    }

    private fun onAddPressed() {
        val model = binding.model.text.toString()
        val year = binding.year.text.toString()
        val licence = binding.licence.text.toString()

        val areAllFieldsValid = validateForm()

        if (!areAllFieldsValid) {
            return
        }

        binding.addButton.isEnabled = false
        binding.addButton.text = getString(R.string.create_add_button_loading)

        if (selectedImageUri != null) {
            uploadImageToFirebase(selectedImageUri!!) { imageUrl ->
                if (imageUrl != null) {
                    uploadedImageUrl = imageUrl
                    createCarWithImageUrl(model, year, licence, imageUrl)
                } else {
                    // Reset button state
                    binding.addButton.isEnabled = true
                    binding.addButton.text = getString(R.string.create_add_button)
                    Toast.makeText(
                        this,
                        getString(R.string.create_failed_load_image),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun createCarWithImageUrl(
        model: String,
        year: String,
        licence: String,
        imageUrl: String
    ) {
        val location = selectedMarker?.position?.let { position ->
            CarLocation(
                long = position.longitude,
                lat = position.latitude
            )
        } ?: CarLocation(
            long = 0.0,
            lat = 0.0
        )

        val car = CarValue(
            SecureRandom().nextInt().toString(),
            imageUrl,
            year,
            model,
            licence,
            location
        )

        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.apiService.addCar(car) }

            withContext(Dispatchers.Main) {
                // Reset button state
                binding.addButton.isEnabled = true
                binding.addButton.text = getString(R.string.create_add_button)

                when (result) {
                    is Result.Success -> {
                        Toast
                            .makeText(
                                this@CreateActivity,
                                R.string.create_success,
                                Toast.LENGTH_SHORT
                            ).show()
                        finish()
                    }

                    is Result.Error -> {
                        Toast
                            .makeText(
                                this@CreateActivity,
                                R.string.create_error,
                                Toast.LENGTH_SHORT
                            ).show()
                    }
                }
            }
        }
    }

    private fun onCancelPressed() {
        finish()
    }

    private fun validateForm(): Boolean {
        val model = binding.model.text.toString()
        val year = binding.year.text.toString()
        val licence = binding.licence.text.toString()

        val areAllFieldsValid = model.isNotEmpty() && year.isNotEmpty() && licence.isNotEmpty()

        if (areAllFieldsValid) {
            binding.model.error = null
            binding.year.error = null
            binding.licence.error = null
            
            // Check if location is selected
            if (selectedMarker == null) {
                Toast.makeText(
                    this,
                    "Please select a location on the map",
                    Toast.LENGTH_SHORT
                ).show()
                return false
            }
            
            return true
        }

        if (model.isEmpty()) {
            binding.model.error = getString(R.string.required_field)

        }
        if (year.isEmpty()) {
            binding.year.error = getString(R.string.required_field)

        }
        if (licence.isEmpty()) {
            binding.licence.error = getString(R.string.required_field)

        }

        return false
    }


    private fun getDeviceLocation() {
        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PERMISSION_GRANTED
        ) {
            // Já tenho permissão de localização do usuário
            loadCurrentLocation()
        } else {

        }
    }

    @SuppressLint("MissingPermission")
    private fun loadCurrentLocation() {
        mMap.isMyLocationEnabled = true
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLocationLatLng = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocationLatLng, 15f))
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        binding.mapContent.visibility = View.VISIBLE
        mMap.setOnMapClickListener { latLng ->
            selectedMarker?.remove()

            selectedMarker = mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .draggable(true)
                    .title("Lat: ${latLng.latitude} Long: ${latLng.longitude}")
            )
        }
        getDeviceLocation()
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, CreateActivity::class.java)
        private const val REQUEST_PERMISSIONS = 100
        private const val REQUEST_LOCATION_PERMISSION = 101
    }
}