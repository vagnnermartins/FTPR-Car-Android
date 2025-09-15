package com.example.myapitest

import android.annotation.SuppressLint
import android.app.Activity
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
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import com.example.myapitest.databinding.ActivityNewItemBinding
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID
import androidx.core.content.ContextCompat.checkSelfPermission
import com.example.myapitest.model.Item
import com.example.myapitest.model.ItemPlace
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.Result
import com.example.myapitest.service.safeApiCall
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.SecureRandom

class NewItem : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityNewItemBinding

    private lateinit var imageUri: Uri
    private var imageFile: File? = null

    private lateinit var mMap: GoogleMap
    private var selectedMarker: Marker? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val cameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == Activity.RESULT_OK) {
            uploadImageToFirebase()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewItemBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        setupView()
        setupGoogleMap()

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

    private fun setupGoogleMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun onTakePicture() {
        if (checkSelfPermission(this, android.Manifest.permission.CAMERA) == PERMISSION_GRANTED) {
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
                onLoadingImage(false)
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

    private fun onSave() {
        if (!validateForm()) return
        saveData()
    }

    private fun validateForm(): Boolean {
        if (binding.name.text.toString().isBlank()) {
            Toast.makeText(
                this,
                getString(R.string.error_validate_form, "Name"),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (binding.year.text.toString().isBlank()) {
            Toast.makeText(
                this,
                getString(R.string.error_validate_form, "Ano do carro"),
                Toast.LENGTH_SHORT
            ).show()
            return false
        }
        if (binding.license.text.toString().isBlank()) {
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

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        binding.mapContent.visibility = View.VISIBLE
        mMap.setOnMapClickListener { latLng ->
            selectedMarker?.remove() // Limpa o Marker atual, caso exista

            // Armazena o local do Novo Marker criado
            selectedMarker = mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .draggable(true)
                    .title("Lat: ${latLng.latitude} Long: ${latLng.longitude}")
            )
        }
        getDeviceLocation()
    }

    private fun getDeviceLocation() {
        if (
            checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PERMISSION_GRANTED
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

    private fun saveData() {
        val name = binding.name.text.toString()
        val year = binding.year.text.toString()
        val license = binding.license.text.toString()
        val imageUrl = binding.imageUrl.text.toString()
        val location = selectedMarker?.position?.let { position ->
            ItemPlace(
                position.latitude,
                position.longitude,

            )
        } ?: throw IllegalArgumentException("Usuário deveria ter a localização nesse ponto.")

        CoroutineScope(Dispatchers.IO).launch {
            val itemValue = Item(
                SecureRandom().nextInt().toString(),
                name,
                imageUrl,
                year,
                license,
                location
            )
            val result = safeApiCall { RetrofitClient.apiService.addItem(itemValue) }
            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Error -> {
                        Toast.makeText(
                            this@NewItem,
                            R.string.error_create,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is Result.Success -> {
                        Toast.makeText(
                            this@NewItem,
                            getString(R.string.success_create, name),
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }
        }
    }


    companion object {

        const val REQUEST_CODE_CAMERA = 101

        fun newIntent(context: Context) = Intent(context, NewItem::class.java)
    }
}