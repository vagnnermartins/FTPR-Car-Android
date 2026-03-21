package com.example.myapitest

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapitest.api.RetrofitClient
import com.example.myapitest.databinding.ActivityCarDetailBinding
import com.example.myapitest.model.Car
import com.example.myapitest.model.Place
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.launch
import java.util.*

class CarDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarDetailBinding
    private var imageUri: Uri? = null
    private var lastLocation: Place? = null
    private var uploadedImageUrl: String? = null
    private var isUploading = false
    private lateinit var storage: FirebaseStorage

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = it
            binding.ivCar.setImageURI(it)
            binding.btnSelectImage.visibility = View.GONE
            
            displayImageSize(it)
            uploadImageToFirebase(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFirebaseStorage()
        setupListeners()
        setupYearMask()
        checkLocationPermissions()
    }

    private fun setupFirebaseStorage() {
        try {
            val app = FirebaseApp.initializeApp(this) ?: FirebaseApp.getInstance()
            val bucket = app.options.storageBucket
            storage = if (bucket.isNullOrBlank()) {
                FirebaseStorage.getInstance(app)
            } else {
                FirebaseStorage.getInstance("gs://$bucket")
            }
            Log.d("FirebaseStorage", "Bucket configurado: ${storage.app.options.storageBucket}")
        } catch (e: Exception) {
            Log.e("FirebaseStorage", "Falha ao inicializar Firebase Storage", e)
            Toast.makeText(this, "Firebase Storage não foi inicializado corretamente.", Toast.LENGTH_LONG).show()
            binding.btnSelectImage.isEnabled = false
            binding.btnSave.isEnabled = false
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnSelectImage.setOnClickListener { pickImage.launch("image/*") }
        binding.ivCar.setOnClickListener { pickImage.launch("image/*") }
        binding.btnSave.setOnClickListener { saveCar() }
    }

    private fun setupYearMask() {
        binding.etYear.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (isUpdating) return
                
                val str = s.toString().replace("/", "")
                if (str.length >= 5) {
                    isUpdating = true
                    val formatted = str.substring(0, 4) + "/" + str.substring(4, Math.min(str.length, 8))
                    binding.etYear.setText(formatted)
                    binding.etYear.setSelection(formatted.length)
                    isUpdating = false
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun displayImageSize(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val size = inputStream?.available() ?: 0
            val sizeInMb = size.toDouble() / (1024 * 1024)
            binding.tvLocation.text = "Localização: Aguardando...\nImagem: %.2f MB".format(sizeInMb)
            inputStream?.close()
        } catch (e: Exception) {
            Log.e("IMAGE_SIZE", "Erro ao ler tamanho", e)
        }
    }

    private fun checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            getCurrentLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    lastLocation = Place(location.latitude, location.longitude)
                    updateLocationText()
                }
            }
    }

    private fun updateLocationText() {
        lastLocation?.let {
            val currentText = binding.tvLocation.text.toString()
            val sizeInfo = if (currentText.contains("Imagem:")) currentText.substring(currentText.indexOf("Imagem:")) else ""
            binding.tvLocation.text = "Localização: ${it.lat}, ${it.long}\n$sizeInfo"
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation()
        }
    }

    private fun uploadImageToFirebase(uri: Uri) {
        if (!::storage.isInitialized) {
            Toast.makeText(this, "Firebase Storage indisponível.", Toast.LENGTH_SHORT).show()
            return
        }

        if (FirebaseAuth.getInstance().currentUser == null) {
            Toast.makeText(this, "Faça login novamente para enviar imagens.", Toast.LENGTH_LONG).show()
            return
        }

        isUploading = true
        binding.btnSave.isEnabled = false
        binding.btnSave.text = "Enviando imagem..."
        uploadedImageUrl = null
        Toast.makeText(this, "Fazendo upload da imagem...", Toast.LENGTH_SHORT).show()

        val fileRef: StorageReference = storage.reference.child("cars/${UUID.randomUUID()}.jpg")
        val metadata = StorageMetadata.Builder()
            .setContentType(contentResolver.getType(uri) ?: "image/jpeg")
            .build()

        Log.d("FirebaseStorage", "Iniciando upload para: ${fileRef.path}")

        fileRef.putFile(uri, metadata)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    Log.e("FirebaseStorage", "Falha no putFile", task.exception)
                    task.exception?.let { throw it }
                }
                fileRef.downloadUrl
            }
            .addOnCompleteListener { task ->
                isUploading = false
                binding.btnSave.isEnabled = true
                binding.btnSave.text = "Salvar Carro"
                if (task.isSuccessful) {
                    uploadedImageUrl = task.result.toString()
                    Log.d("FirebaseStorage", "Upload Sucesso: $uploadedImageUrl")
                    Toast.makeText(this, "Upload concluído!", Toast.LENGTH_SHORT).show()
                } else {
                    binding.btnSelectImage.visibility = View.VISIBLE
                    val storageException = task.exception as? StorageException
                    val errorMsg = task.exception?.message ?: "Erro desconhecido"
                    Log.e("FirebaseStorage", "Erro completo: $errorMsg")
                    val userMessage = when (storageException?.errorCode) {
                        StorageException.ERROR_NOT_AUTHENTICATED -> "A sessão expirou. Faça login novamente."
                        StorageException.ERROR_NOT_AUTHORIZED -> "O Firebase recusou o upload. Verifique as regras do Storage."
                        StorageException.ERROR_QUOTA_EXCEEDED -> "A cota do Firebase Storage foi excedida."
                        else -> "Falha ao enviar imagem ao Firebase: $errorMsg"
                    }
                    Toast.makeText(this, userMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveCar() {
        val name = binding.etName.text?.toString()?.trim().orEmpty()
        val year = binding.etYear.text?.toString()?.trim().orEmpty()
        val licence = binding.etLicence.text?.toString()?.trim().orEmpty()

        if (isUploading) {
            Toast.makeText(this, "Aguarde o upload terminar...", Toast.LENGTH_SHORT).show()
            return
        }

        if (name.isBlank() || year.isBlank() || licence.isBlank() || uploadedImageUrl.isNullOrBlank() || lastLocation == null) {
            Toast.makeText(this, "Preencha todos os campos, imagem e localização", Toast.LENGTH_SHORT).show()
            return
        }

        val car = Car(
            id = UUID.randomUUID().toString(),
            imageUrl = uploadedImageUrl!!,
            year = year,
            name = name,
            licence = licence,
            place = lastLocation!!
        )
        sendCarToApi(car)
    }

    private fun sendCarToApi(car: Car) {
        binding.btnSave.isEnabled = false
        binding.btnSave.text = "Enviando para API..."
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.saveCar(car)
                if (response.isSuccessful) {
                    Toast.makeText(this@CarDetailActivity, "Carro salvo com sucesso!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "Salvar Carro"
                    val apiError = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                    Toast.makeText(
                        this@CarDetailActivity,
                        "Erro ao salvar na API: ${apiError ?: "HTTP ${response.code()}"}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                binding.btnSave.isEnabled = true
                binding.btnSave.text = "Salvar Carro"
                Toast.makeText(this@CarDetailActivity, "Erro ao salvar na API: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
