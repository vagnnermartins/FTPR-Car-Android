package com.example.myapitest

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageMetadata
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import java.util.*

class CarDetailActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_CAR_ID = "extra_car_id"
        private const val EXTRA_CAR_NAME = "extra_car_name"
        private const val EXTRA_CAR_YEAR = "extra_car_year"
        private const val EXTRA_CAR_LICENCE = "extra_car_licence"
        private const val EXTRA_CAR_IMAGE_URL = "extra_car_image_url"
        private const val EXTRA_CAR_LAT = "extra_car_lat"
        private const val EXTRA_CAR_LONG = "extra_car_long"

        fun createEditIntent(context: Context, car: Car): Intent {
            return Intent(context, CarDetailActivity::class.java).apply {
                putExtra(EXTRA_CAR_ID, car.id)
                putExtra(EXTRA_CAR_NAME, car.name)
                putExtra(EXTRA_CAR_YEAR, car.year)
                putExtra(EXTRA_CAR_LICENCE, car.licence)
                putExtra(EXTRA_CAR_IMAGE_URL, car.imageUrl)
                putExtra(EXTRA_CAR_LAT, car.place.lat)
                putExtra(EXTRA_CAR_LONG, car.place.long)
            }
        }
    }

    private lateinit var binding: ActivityCarDetailBinding
    private var imageUri: Uri? = null
    private var carId: String? = null
    private var lastLocation: Place? = null
    private var selectedImageSizeMb: Double? = null
    private var originalImageUrl: String? = null
    private var uploadedImageUrl: String? = null
    private var isUploading = false
    private var isEditMode = false
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
        loadEditModeIfNeeded()
        setupListeners()
        setupYearMask()
        if (!isEditMode) {
            checkLocationPermissions()
        }
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
            Toast.makeText(this, getString(R.string.storage_not_initialized), Toast.LENGTH_LONG).show()
            binding.btnSelectImage.isEnabled = false
            binding.btnSave.isEnabled = false
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnSelectImage.setOnClickListener { pickImage.launch("image/*") }
        binding.ivCar.setOnClickListener { pickImage.launch("image/*") }
        binding.btnSave.setOnClickListener { saveCar() }
        binding.btnDelete.setOnClickListener { confirmDeleteCar() }
    }

    private fun loadEditModeIfNeeded() {
        carId = intent.getStringExtra(EXTRA_CAR_ID)
        isEditMode = !carId.isNullOrBlank()
        if (!isEditMode) return

        binding.tvToolbarTitle.setText(R.string.edit_car_title)
        binding.btnSave.setText(R.string.update_car)
        binding.btnDelete.visibility = View.VISIBLE

        binding.etName.setText(intent.getStringExtra(EXTRA_CAR_NAME).orEmpty())
        binding.etYear.setText(intent.getStringExtra(EXTRA_CAR_YEAR).orEmpty())
        binding.etLicence.setText(intent.getStringExtra(EXTRA_CAR_LICENCE).orEmpty())

        originalImageUrl = intent.getStringExtra(EXTRA_CAR_IMAGE_URL)
        uploadedImageUrl = originalImageUrl

        val lat = intent.getDoubleExtra(EXTRA_CAR_LAT, Double.NaN)
        val long = intent.getDoubleExtra(EXTRA_CAR_LONG, Double.NaN)
        if (!lat.isNaN() && !long.isNaN()) {
            lastLocation = Place(lat, long)
        }

        if (!originalImageUrl.isNullOrBlank()) {
            binding.btnSelectImage.visibility = View.GONE
            Picasso.get()
                .load(originalImageUrl)
                .fit()
                .centerCrop()
                .into(binding.ivCar)
        }

        refreshLocationText()
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
            selectedImageSizeMb = size.toDouble() / (1024 * 1024)
            refreshLocationText()
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
        refreshLocationText()
    }

    private fun refreshLocationText() {
        lastLocation?.let {
            binding.tvLocation.text = if (selectedImageSizeMb != null) {
                getString(R.string.location_with_coordinates_and_image, it.lat, it.long, selectedImageSizeMb)
            } else {
                getString(R.string.location_with_coordinates, it.lat, it.long)
            }
            return
        }

        selectedImageSizeMb?.let {
            binding.tvLocation.text = getString(R.string.location_waiting_with_image, it)
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
            Toast.makeText(this, getString(R.string.storage_unavailable), Toast.LENGTH_SHORT).show()
            return
        }

        if (FirebaseAuth.getInstance().currentUser == null) {
            Toast.makeText(this, getString(R.string.upload_requires_login), Toast.LENGTH_LONG).show()
            return
        }

        isUploading = true
        binding.btnSave.isEnabled = false
        binding.btnSave.setText(R.string.uploading_image_button)
        uploadedImageUrl = null
        Toast.makeText(this, getString(R.string.uploading_image_message), Toast.LENGTH_SHORT).show()

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
                binding.btnSave.setText(R.string.save_car)
                if (task.isSuccessful) {
                    uploadedImageUrl = task.result.toString()
                    Log.d("FirebaseStorage", "Upload Sucesso: $uploadedImageUrl")
                    Toast.makeText(this, getString(R.string.upload_complete), Toast.LENGTH_SHORT).show()
                } else {
                    binding.btnSelectImage.visibility = View.VISIBLE
                    val storageException = task.exception as? StorageException
                    val errorMsg = task.exception?.message ?: "Erro desconhecido"
                    Log.e("FirebaseStorage", "Erro completo: $errorMsg")
                    val userMessage = when (storageException?.errorCode) {
                        StorageException.ERROR_NOT_AUTHENTICATED -> getString(R.string.session_expired)
                        StorageException.ERROR_NOT_AUTHORIZED -> getString(R.string.storage_not_authorized)
                        StorageException.ERROR_QUOTA_EXCEEDED -> getString(R.string.storage_quota_exceeded)
                        else -> getString(R.string.storage_upload_failed, errorMsg)
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
            Toast.makeText(this, getString(R.string.wait_upload_finish), Toast.LENGTH_SHORT).show()
            return
        }

        if (name.isBlank() || year.isBlank() || licence.isBlank() || uploadedImageUrl.isNullOrBlank() || lastLocation == null) {
            Toast.makeText(this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show()
            return
        }

        val car = Car(
            id = carId ?: UUID.randomUUID().toString(),
            imageUrl = uploadedImageUrl!!,
            year = year,
            name = name,
            licence = licence,
            place = lastLocation!!
        )
        if (isEditMode) {
            updateCar(car)
        } else {
            createCar(car)
        }
    }

    private fun createCar(car: Car) {
        binding.btnSave.isEnabled = false
        binding.btnSave.setText(R.string.sending_api_button)
        binding.btnDelete.isEnabled = false
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.saveCar(car)
                if (response.isSuccessful) {
                    Toast.makeText(this@CarDetailActivity, getString(R.string.car_saved_success), Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    binding.btnSave.isEnabled = true
                    binding.btnSave.setText(R.string.save_car)
                    binding.btnDelete.isEnabled = true
                    val apiError = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                    Toast.makeText(
                        this@CarDetailActivity,
                        getString(R.string.save_api_error, apiError ?: "HTTP ${response.code()}"),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                binding.btnSave.isEnabled = true
                binding.btnSave.setText(R.string.save_car)
                binding.btnDelete.isEnabled = true
                Toast.makeText(
                    this@CarDetailActivity,
                    getString(R.string.save_api_error, e.message ?: ""),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateCar(car: Car) {
        val editId = car.id ?: return
        val previousImageUrl = originalImageUrl

        binding.btnSave.isEnabled = false
        binding.btnDelete.isEnabled = false
        binding.btnSave.setText(R.string.sending_api_button)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.updateCar(editId, car)
                if (response.isSuccessful) {
                    if (!previousImageUrl.isNullOrBlank() && previousImageUrl != car.imageUrl) {
                        deleteImageFromFirebaseIfPossible(previousImageUrl)
                    }
                    Toast.makeText(this@CarDetailActivity, getString(R.string.car_updated_success), Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    binding.btnSave.isEnabled = true
                    binding.btnDelete.isEnabled = true
                    binding.btnSave.setText(R.string.update_car)
                    val apiError = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                    Toast.makeText(
                        this@CarDetailActivity,
                        getString(R.string.save_api_error, apiError ?: "HTTP ${response.code()}"),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                binding.btnSave.isEnabled = true
                binding.btnDelete.isEnabled = true
                binding.btnSave.setText(R.string.update_car)
                Toast.makeText(
                    this@CarDetailActivity,
                    getString(R.string.save_api_error, e.message ?: ""),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun confirmDeleteCar() {
        val editId = carId ?: return
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_confirmation_title)
            .setMessage(getString(R.string.delete_confirmation_message, binding.etName.text?.toString()?.trim().orEmpty()))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                deleteCar(editId)
            }
            .show()
    }

    private fun deleteCar(editId: String) {
        binding.btnSave.isEnabled = false
        binding.btnDelete.isEnabled = false
        binding.btnDelete.setText(R.string.deleting_car_button)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.deleteCar(editId)
                if (response.isSuccessful) {
                    deleteImageFromFirebaseIfPossible(originalImageUrl)
                    if (uploadedImageUrl != originalImageUrl) {
                        deleteImageFromFirebaseIfPossible(uploadedImageUrl)
                    }
                    Toast.makeText(this@CarDetailActivity, getString(R.string.car_deleted_success), Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    binding.btnSave.isEnabled = true
                    binding.btnDelete.isEnabled = true
                    binding.btnDelete.setText(R.string.delete_car)
                    val apiError = response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                    Toast.makeText(
                        this@CarDetailActivity,
                        getString(R.string.delete_api_error, apiError ?: "HTTP ${response.code()}"),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                binding.btnSave.isEnabled = true
                binding.btnDelete.isEnabled = true
                binding.btnDelete.setText(R.string.delete_car)
                Toast.makeText(
                    this@CarDetailActivity,
                    getString(R.string.delete_api_error, e.message ?: ""),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun deleteImageFromFirebaseIfPossible(imageUrl: String?) {
        if (imageUrl.isNullOrBlank() || !::storage.isInitialized) return
        if (!imageUrl.startsWith("gs://") && !imageUrl.contains("firebasestorage")) return

        try {
            storage.getReferenceFromUrl(imageUrl)
                .delete()
                .addOnFailureListener { error ->
                    Log.w("FirebaseStorage", "Falha ao remover imagem antiga", error)
                }
        } catch (e: Exception) {
            Log.w("FirebaseStorage", "Nao foi possivel interpretar a URL da imagem para exclusao", e)
        }
    }
}
