package com.example.myapitest

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.example.myapitest.databinding.ActivityNewCarBinding
import com.example.myapitest.model.Car
import com.example.myapitest.model.Place
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID
import com.example.myapitest.service.Result

class NewCarActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityNewCarBinding
    private lateinit var mMap: GoogleMap
    private var selectedMarker: Marker? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var imageUri: Uri
    private var imageFile: File? = null

    private val cameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            uploadImageToFirebase()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewCarBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        setupView()
        setupGoogleMap()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        binding.mapContent.visibility = View.VISIBLE
        mMap.setOnMapClickListener { latLng ->
            selectedMarker?.remove() //Limpa o atual caso exista

            selectedMarker = mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .draggable(true)
                    .title("Lat: ${latLng.latitude}, Lng: ${latLng.longitude}")
            )
        }
        getDeviceLocation()
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
                    Toast.makeText(this, R.string.error_camera_permission, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.takePictureCta.setOnClickListener {
            onTakePicture()
        }
        binding.saveCta.setOnClickListener {
            saveCar()
        }
    }

    private fun saveCar(){
        if (!validateForm()) {
            return
        }

        saveData()
    }

    private fun saveData() {
        val location = selectedMarker?.position?.let { position ->
            Place(
                position.latitude,
                position.longitude
            )
        } ?: throw IllegalArgumentException(getString(R.string.precisa_validar))

        val id = SecureRandom().nextInt().toString()

        val car = Car(
            id = id,
            name = binding.name.text.toString(),
            year = binding.year.text.toString(),
            licence = binding.licence.text.toString(),
            imageUrl = binding.imageUrl.text.toString(),
            place = location
        )

        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall {
                RetrofitClient.apiService.postCar(car)
            }
            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Success -> {
                        Toast.makeText(
                            this@NewCarActivity,
                            getString(R.string.success_create, car.name),
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }

                    is Result.Error -> {
                        Toast.makeText(
                            this@NewCarActivity,
                            getString(R.string.erro_create),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        if (binding.name.text.toString().isBlank()) {
            Toast.makeText(this, getString(R.string.error_validate_form, "Name"), Toast.LENGTH_SHORT).show()
            return false
        }
        val year = binding.year.text.toString()
        if (year.isBlank()) {
            Toast.makeText(this, getString(R.string.error_validate_form, "Year"), Toast.LENGTH_SHORT).show()
            return false
        }
        // Valida se está no formato "2020/2020"
        if (year.length != 9 || year[4] != '/') {
            Toast.makeText(this, getString(R.string.error_validate_year_format), Toast.LENGTH_SHORT).show()
            return false
        }
        // Pega as duas partes separadas pelo "/"
        val parts = year.split("/")
        if (parts.size != 2 || parts.any { it.length != 4 || !it.all { ch -> ch.isDigit() } }) {
            Toast.makeText(this, getString(R.string.error_validate_year_format), Toast.LENGTH_SHORT).show()
            return false
        }
        val license = binding.licence.text.toString()
        if (license.isBlank()) {
            Toast.makeText(this, getString(R.string.error_validate_form, "License"), Toast.LENGTH_SHORT).show()
            return false
        }
        //Valida o formato "ABC-1234"
        if (license.length != 8 || license[3] != '-') {
            Toast.makeText(this, getString(R.string.error_validate_license_format), Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.imageUrl.text.toString().isBlank()) {
            Toast.makeText(this, getString(R.string.error_validate_form, "Imagem"), Toast.LENGTH_SHORT).show()
            return false
        }
        if (selectedMarker == null) {
            Toast.makeText(this, getString(R.string.error_validate_form_location), Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    private fun uploadImageToFirebase() {
        //Obtem a referencia do Firebase storage
        val storageRef = FirebaseStorage.getInstance().reference

        //Cria a referencia para nossa imagem
        val imagesRef = storageRef.child("images/${UUID.randomUUID()}.jpg")

        val baos = ByteArrayOutputStream()
        val imageBitmap = BitmapFactory.decodeFile(imageFile!!.path)
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        onLoadingImage(true) //Coloca o load na tela

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

    private fun onTakePicture() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PERMISSION_GRANTED) {
            openCamera()
        } else {
            requestCameraPermission()
        }
    }

    private fun openCamera(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        imageUri = createImageUri()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraLauncher.launch(intent)
    }

    private fun createImageUri(): Uri {
        val timestamp: String = SimpleDateFormat("yyyyMMdd-HHmmss").format(Date())
        val imageFileName = "JPEG_${timestamp}_"

        //Obtem o diretório de armazenamento do aplicativo
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        //Cria o arquivo temporário
        imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)
        return FileProvider.getUriForFile(this, "com.example.myapitest.fileprovider", imageFile!!)
    }

    private fun requestCameraPermission(){
        ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.CAMERA),
            REQUEST_CODE_CAMERA
        )
    }

    private fun setupGoogleMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun getDeviceLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PERMISSION_GRANTED
        ) {
            //Já tenho permissão de localização do usuário
            loadCurrentLocation()
        } else {
            //
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

    companion object{

        const val REQUEST_CODE_CAMERA = 101

        fun newIntent(context: Context) = Intent(context, NewCarActivity::class.java)
    }
}