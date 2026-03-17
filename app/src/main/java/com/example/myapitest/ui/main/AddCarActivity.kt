package com.example.myapitest.ui.main

import android.Manifest
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapitest.R
import com.example.myapitest.data.FirebaseStorageManager
import com.example.myapitest.data.api.RetrofitClient
import com.example.myapitest.data.model.Car
import com.example.myapitest.data.model.Place
import com.example.myapitest.databinding.ActivityAddCarBinding
import com.example.myapitest.repository.CarRepository
import com.example.myapitest.ui.login.LoginActivity
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class AddCarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddCarBinding
    private lateinit var carRepository: CarRepository
    private var selectedImageUri: Uri? = null

    private val selectLocationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val lat = result.data?.getDoubleExtra("LAT", 0.0) ?: 0.0
            val lng = result.data?.getDoubleExtra("LONG", 0.0) ?: 0.0
            binding.etLat.setText(lat.toString())
            binding.etLong.setText(lng.toString())
            logConsole("Localização selecionada!")
        }
    }

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.ivCarPreview.setImageURI(it)
            logConsole("Foto selecionada da galeria.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = FirebaseAuth.getInstance().currentUser
        binding.tvUserInfo.text = "${user?.phoneNumber} Logado"
        
        carRepository = CarRepository(RetrofitClient.getInstance(this))
        setupView()
    }

    private fun setupView() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }

        binding.ivCarPreview.setOnClickListener { pickImageLauncher.launch("image/*") }

        binding.btnDefaultPhoto.setOnClickListener {
            val defaultImageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/" + R.drawable.fotopadrao)
            selectedImageUri = defaultImageUri
            binding.ivCarPreview.setImageResource(R.drawable.fotopadrao)
            logConsole("Usando a foto padrão.")
        }

        binding.btnGetLocation.setOnClickListener { getCurrentLocation() }

        binding.btnSelectLocation.setOnClickListener {
            val latStr = binding.etLat.text.toString()
            val lngStr = binding.etLong.text.toString()
            val intent = Intent(this, SelectLocationActivity::class.java)
            
            // RZ - Passa os valores se já estiverem preenchidos, caso contrário o SelectLocationActivity usará o padrão (Cristo Redentor)
            if (latStr.isNotEmpty() && lngStr.isNotEmpty()) {
                intent.putExtra("LAT", latStr.toDoubleOrNull() ?: 0.0)
                intent.putExtra("LONG", lngStr.toDoubleOrNull() ?: 0.0)
            }
            
            selectLocationLauncher.launch(intent)
        }

        binding.btnSave.setOnClickListener { saveCar() }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        LocationServices.getFusedLocationProviderClient(this).lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                binding.etLat.setText(location.latitude.toString())
                binding.etLong.setText(location.longitude.toString())
                logConsole("Localização capturada!")
            } else logConsole("Erro ao obter GPS.")
        }
    }

    private fun saveCar() {
        val name = binding.etName.text.toString()
        val year = binding.etYear.text.toString()
        val licence = binding.etLicence.text.toString()
        val latStr = binding.etLat.text.toString()
        val lngStr = binding.etLong.text.toString()

        if (name.isEmpty() || year.isEmpty() || licence.isEmpty() || selectedImageUri == null || latStr.isEmpty() || lngStr.isEmpty()) {
            logConsole("Preencha todos os campos.")
            return
        }

        lifecycleScope.launch {
            binding.loadingOverlay.visibility = View.VISIBLE
            logConsole("Salvando na API...")

            val imageUrl = FirebaseStorageManager.uploadImage(selectedImageUri!!)

            if (imageUrl != null) {
                val car = Car(
                    name = name, year = year, licence = licence,
                    imageUrl = imageUrl, place = Place(latStr.toDouble(), lngStr.toDouble())
                )

                val result = carRepository.saveCarDetailed(car)

                if (result.success) {
                    Toast.makeText(this@AddCarActivity, "Carro salvo!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    logConsole("Erro: ${result.message}")
                }
            } else {
                logConsole("Erro no upload da imagem.")
            }

            binding.loadingOverlay.visibility = View.GONE
        }
    }

    private fun logConsole(message: String) {
        binding.tvErrorLog.text = "RZ Console: $message"
    }
}
