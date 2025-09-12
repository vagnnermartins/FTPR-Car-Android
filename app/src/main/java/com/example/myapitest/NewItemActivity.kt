package com.example.myapitest

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.checkSelfPermission
import androidx.core.view.isVisible
import com.example.myapitest.databinding.ActivityNewItemBinding
import com.example.myapitest.model.Car
import com.example.myapitest.model.Place
import com.example.myapitest.service.Result
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.safeApiCall
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.Math.abs
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

class NewItemActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityNewItemBinding
    private lateinit var mMap: GoogleMap
    private var selectedMarker: Marker? = null
    private lateinit var imageUri: Uri
    private var imageFile: File? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val cameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            uploadImageToFirebase()

            Log.d("NewItemActivity", "Imagem capturada")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewItemBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Buscar localização do usuário - GPS, Wifi, 3G/4G
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        setupView()
        setupGoogleMap()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, R.string.error_request_camera, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        binding.mapContainer.visibility = View.VISIBLE
        mMap.setOnMapClickListener { latLng ->

            // Limpar marcador anterior, se existir
            selectedMarker?.remove()

            // Adicionar novo marcador
            selectedMarker = mMap.addMarker(
                MarkerOptions().position(latLng).draggable(true)
                    .title("${latLng.latitude}, ${latLng.longitude}")
            )
        }
        getDeviceLocation()
    }

    private fun uploadImageToFirebase() {
        // Obtém referência do Firebase Storage
        val storageRef = FirebaseStorage.getInstance().reference

        // Cria uma referência para a nossa imagem
        val imagesRef = storageRef.child("images/${UUID.randomUUID()}.jpg")

        val baos = ByteArrayOutputStream()
        val imageBitmap = BitmapFactory.decodeFile(imageFile!!.path)
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        onLoadingImage(true) // Coloca um load na tela
        imagesRef.putBytes(data)
            .addOnFailureListener {
                Toast.makeText(this, R.string.error_upload_image, Toast.LENGTH_SHORT).show()
            }
            .addOnSuccessListener {
                imagesRef.downloadUrl
                    .addOnCompleteListener {
                        onLoadingImage(false)
                    }
                    .addOnSuccessListener { uri ->
                        binding.imageUrl.setText(uri.toString())
                    }
            }
    }

    private fun onLoadingImage(isLoading: Boolean) {
        binding.loadImageProgress.isVisible = isLoading
        binding.takePictureCta.isEnabled = !isLoading
        binding.saveCta.isEnabled = !isLoading
    }


    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.saveCta.setOnClickListener {
            onSave()
        }
        binding.takePictureCta.setOnClickListener {
            onTakePicture()

        }
    }

    private fun onTakePicture() {
        if (checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == PermissionChecker.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            requestCameraPermission()
        }
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.CAMERA),
            REQUEST_CODE_CAMERA
        )
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        imageUri = createImageUri()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraLauncher.launch(intent)
    }

    @SuppressLint("SimpleDateFormat")
    private fun createImageUri(): Uri {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd-HHmmss").format(Date())
        val imageFileName = "JPEG_${timeStamp}_"

        // Obtém o diretório de armazenamento externo para imagens
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        // cria um arquivo de imagem
        imageFile = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        )

        return FileProvider.getUriForFile(
            this,
            "com.example.myapitest.fileprovider",// applicationId + .provider
            imageFile!!
        )
    }

    private fun onSave() {
        if (!validateForm()) return
        saveData()
    }

    private fun saveData() {
        val name = binding.etName.text.toString()
        val year = binding.etYear.text.toString()
        val license = binding.etLicense.text.toString()
        val imageUrl = binding.imageUrl.text.toString()
        val location = selectedMarker?.position?.let { position ->
            Place(
                position.latitude,
                position.longitude,
            )
        } ?: throw IllegalArgumentException("Usuário deveria ter a localização nesse ponto.")

        CoroutineScope(Dispatchers.IO).launch {
            val itemValue = Car(
                kotlin.math.abs(SecureRandom().nextInt()).toString(),
                imageUrl,
                year,
                name,
                license,
                location
            )

            val result = safeApiCall { RetrofitClient.apiService.addItem(itemValue) }
            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Error -> {
                        Toast.makeText(
                            this@NewItemActivity,
                            R.string.error_create,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is Result.Success -> {
                        Toast.makeText(
                            this@NewItemActivity,
                            getString(R.string.success_create, name),
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        if (binding.etName.text.toString().isBlank()) {
            Toast.makeText(
                this,
                getString(R.string.error_validate_form, "Nome"),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (binding.etYear.text.toString().isBlank()) {
            Toast.makeText(
                this,
                getString(R.string.error_validate_form, "Ano"),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (binding.etLicense.text.toString().isBlank()) {
            Toast.makeText(
                this,
                getString(R.string.error_validate_form, "Placa"),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }

        if (binding.imageUrl.text.toString().isBlank()) {
            Toast.makeText(
                this,
                getString(R.string.error_validate_form, "Imagem"),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (selectedMarker == null) {
            Toast.makeText(
                this,
                getString(R.string.error_validate_form_location),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }


    private fun setupGoogleMap() {
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as com.google.android.gms.maps.SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun getDeviceLocation() {
        if (
            checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PermissionChecker.PERMISSION_GRANTED
        ) {
            // Já tenho permissão de localização do usuário
            loadCurrentLocation()
        } else {
            // NÃO tenho permissão de localização do usuário
        }
    }

    @SuppressLint("MissingPermission")
    private fun loadCurrentLocation() {
        mMap.isMyLocationEnabled = true
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            val currentLocationLatLng = LatLng(location.latitude, location.longitude)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocationLatLng, 15f))
        }
    }


    companion object {
        private const val REQUEST_CODE_CAMERA = 101
        fun newIntent(context: Context) = Intent(context, NewItemActivity::class.java)
    }
}
