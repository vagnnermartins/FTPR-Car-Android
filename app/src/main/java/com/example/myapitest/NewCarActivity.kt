package com.example.myapitest

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.myapitest.databinding.ActivityNewCarBinding
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
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class NewCarActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityNewCarBinding
    private lateinit var mMap: GoogleMap
    private var selectedMarker: Marker? = null
    private var imageUri: Uri? = null
    private var imageFile: File? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            binding.imageUrl.setText("Imagem capturada com sucesso!")
        } else {
            imageFile = null
            imageUri = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewCarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupView()
        setupGoogleMap()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        binding.mapContent.visibility = View.VISIBLE

        getDeviceLocation()

        mMap.setOnMapClickListener { latLng ->
            selectedMarker?.remove()
            selectedMarker = mMap.addMarker(
                MarkerOptions().position(latLng).title("Localização do Carro")
            )
        }
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.saveCta.setOnClickListener { saveItem() }
        binding.takePictureCta.setOnClickListener { takePicture() }
    }

    private fun setupGoogleMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun takePicture() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        imageUri = createImageUri()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraLauncher.launch(intent)
    }

    private fun createImageUri(): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val file = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        imageFile = file
        // MUITO IMPORTANTE: O nome no Manifest deve ser igual a este 'fileprovider'
        return FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
    }

    private fun getDeviceLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            loadCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    @SuppressLint("MissingPermission")
    private fun loadCurrentLocation() {
        if (!::mMap.isInitialized) return
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val latLong = LatLng(it.latitude, it.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 15f))
            }
        }
    }

    private fun saveItem() {
        if (!validateForm()) return

        // Se tirou foto, faz upload primeiro. Se não, tenta salvar direto (usando URL manual se houver)
        if (imageFile != null && imageUri != null) {
            uploadImageToFirebase()
        } else {
            saveData()
        }
    }

    private fun uploadImageToFirebase() {
        val uri = imageUri ?: return
        onLoadImage(true)

        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("cars/${UUID.randomUUID()}.jpg")

        imageRef.putFile(uri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    binding.imageUrl.setText(downloadUri.toString())
                    onLoadImage(false)
                    saveData()
                }
            }
            .addOnFailureListener {
                onLoadImage(false)
                Toast.makeText(this, "Falha no upload da imagem", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveData() {
        val name = binding.name.text.toString()
        val year = binding.year.text.toString()
        val license = binding.licence.text.toString()
        val imageUrl = binding.imageUrl.text.toString()

        val place = selectedMarker?.position?.let {
            Place(lat = it.latitude, long = it.longitude)
        } ?: Place(0.0, 0.0)

        lifecycleScope.launch {
            binding.saveCta.isEnabled = false

            val newCar = Car(
                id = UUID.randomUUID().toString(), // API gera ou usamos localmente
                name = name,
                year = year,
                licence = license,
                imageUrl = imageUrl,
                place = place
            )

            val result = withContext(Dispatchers.IO) {
                safeApiCall<Unit> { RetrofitClient.carApiService.addCar(newCar) }            }

            when (result) {
                is Result.Success -> handleOnSuccess()
                is Result.Error -> {
                    Log.e("API_DEBUG", "Erro ao salvar: ${result.message}")
                    handleOnError()
                }
            }
            binding.saveCta.isEnabled = true
        }
    }

    private fun onLoadImage(isLoading: Boolean) {
        binding.loadImageProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.saveCta.isEnabled = !isLoading
    }

    private fun handleOnError() {
        Toast.makeText(this, "Erro ao salvar o veículo na API", Toast.LENGTH_SHORT).show()
    }

    private fun handleOnSuccess() {
        Toast.makeText(this, "Veículo cadastrado com sucesso!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun validateForm(): Boolean {
        var isValid = true
        // Validando apenas os campos essenciais que compõem o objeto Car
        val fields = listOf(binding.name, binding.imageUrl)

        fields.forEach {
            if (it.text.isNullOrBlank()) {
                it.error = "Este campo é obrigatório"
                isValid = false
            }
        }

        if (selectedMarker == null) {
            Toast.makeText(this, "Por favor, selecione a localização no mapa", Toast.LENGTH_LONG).show()
            isValid = false
        }

        return isValid
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1002

        fun newIntent(context: Context) = Intent(context, NewCarActivity::class.java)
    }
}