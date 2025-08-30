package com.example.myapitest

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapitest.databinding.ActivityNewItemBinding
import com.example.myapitest.model.Item
import com.example.myapitest.model.Place
import com.example.myapitest.service.Result
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.safeApiCall
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

class NewItemActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNewItemBinding

    private lateinit var imageUri: Uri
    private var imageFile: File? = null

    private val cameraLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            binding.imageUrl.setText("Image uploaded")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.saveCta.setOnClickListener {
            saveCar()
        }
        binding.takePictureCta.setOnClickListener {
            takePicture()
        }
    }

    private fun uploadImageToFirebase() {
        val storageRef = FirebaseStorage.getInstance().reference

        val imagesRef = storageRef.child("images/${UUID.randomUUID()}.jpg")

        val baos = ByteArrayOutputStream()
        val imageBitmap = BitmapFactory.decodeFile(imageFile!!.path)
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        binding.loadImageProgress.visibility = View.VISIBLE
        binding.takePictureCta.isEnabled = false
        binding.saveCta.isEnabled = false

        imagesRef.putBytes(data)
            .addOnFailureListener {
                binding.loadImageProgress.visibility = View.GONE
                binding.takePictureCta.isEnabled = true
                binding.saveCta.isEnabled = true
                Toast.makeText(this, "Error uploading image", Toast.LENGTH_SHORT).show()
            }
            .addOnSuccessListener {
                binding.loadImageProgress.visibility = View.GONE
                binding.takePictureCta.isEnabled = true
                binding.saveCta.isEnabled = true
                imagesRef.downloadUrl.addOnSuccessListener { uri ->
                    saveData(uri.toString())
                }
            }
    }

    private fun takePicture() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera()
        } else {
            requestCameraPermission()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        imageUri = createImageUri()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraLauncher.launch(intent)
    }

    private fun createImageUri(): Uri {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_${timeStamp}_"

        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        imageFile = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            storageDir      /* directory */
        )

        return FileProvider.getUriForFile(
            this,
            "com.example.myapitest.fileprovider",
            imageFile!!
        )
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.CAMERA),
            101
        )
    }

    private fun saveCar() {
        if (validateForm()) {
            uploadImageToFirebase()
        }
    }

    private fun saveData(imageUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val id = SecureRandom().nextInt().toString()
            val result = safeApiCall {
                RetrofitClient.apiService.createItem(
                    Item(
                        id = id,
                        name = binding.name.text.toString(),
                        year = binding.year.text.toString(),
                        place = Place(
                            long = 0F,
                            lat = 0F
                        ),
                        licence = binding.licence.text.toString(),
                        imageUrl = imageUrl
                    )
                )
            }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Error -> {
                        Toast.makeText(
                            this@NewItemActivity,
                            "Something went wrong",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is Result.Success -> {
                        finish()
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 101) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this@NewItemActivity, "Permission error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, NewItemActivity::class.java)
    }

    private fun validateForm(): Boolean {
        if (binding.name.text.toString().isBlank()) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show()
            return false
        }

        if (binding.year.text.toString().isBlank()){
            Toast.makeText(this, "Year is required", Toast.LENGTH_SHORT).show()
            return false
        }

        if (binding.licence.text.toString().isBlank()){
            Toast.makeText(this, "Licence is required", Toast.LENGTH_SHORT).show()
            return false
        }

        if (binding.imageUrl.text.toString().isBlank()){
            Toast.makeText(this, "Image is required",Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}