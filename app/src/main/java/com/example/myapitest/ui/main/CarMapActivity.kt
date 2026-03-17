package com.example.myapitest.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapitest.R
import com.example.myapitest.data.FirebaseStorageManager
import com.example.myapitest.data.api.RetrofitClient
import com.example.myapitest.data.model.Car
import com.example.myapitest.data.model.Place
import com.example.myapitest.repository.CarRepository
import com.example.myapitest.ui.login.LoginActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch

class CarMapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var carRepository: CarRepository
    private var carId: String? = null
    private var imageUrl: String? = null
    private var selectedImageUri: Uri? = null
    private var carLat: Double = 0.0
    private var carLong: Double = 0.0
    private var mMap: GoogleMap? = null

    private lateinit var ivCarPhoto: ImageView
    private lateinit var etCarName: EditText
    private lateinit var etCarYear: EditText
    private lateinit var etCarLicence: EditText
    private lateinit var tvErrorLog: TextView
    private lateinit var btnChangePhoto: FloatingActionButton
    private lateinit var btnBack: ImageButton
    private lateinit var btnDelete: ImageButton
    private lateinit var btnUpdate: ImageButton
    private lateinit var btnNewLocation: MaterialButton
    private lateinit var loadingOverlay: View
    private lateinit var tvLoadingMessage: TextView

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            ivCarPhoto.setImageURI(it)
            logStatus("Nova foto selecionada! Salve para confirmar.")
        }
    }

    private val selectLocationLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            carLat = result.data?.getDoubleExtra("LAT", 0.0) ?: 0.0
            carLong = result.data?.getDoubleExtra("LONG", 0.0) ?: 0.0
            updateMapPosition()
            logStatus("Localização atualizada! Clique no ícone de salvar para gravar.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_map)

        carRepository = CarRepository(RetrofitClient.getInstance(this))
        setupViews()
        loadCarData()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupViews() {
        ivCarPhoto = findViewById(R.id.ivCarPhoto)
        etCarName = findViewById(R.id.etCarName)
        etCarYear = findViewById(R.id.etCarYear)
        etCarLicence = findViewById(R.id.etCarLicence)
        tvErrorLog = findViewById(R.id.tvErrorLog)
        btnChangePhoto = findViewById(R.id.btnChangePhoto)
        btnBack = findViewById(R.id.btnBack)
        btnDelete = findViewById(R.id.btnDelete)
        btnUpdate = findViewById(R.id.btnUpdate)
        btnNewLocation = findViewById(R.id.btnNewLocation)
        loadingOverlay = findViewById(R.id.loadingOverlay)
        tvLoadingMessage = findViewById(R.id.tvLoadingMessage)

        findViewById<TextView>(R.id.tvUserInfo).text = "${FirebaseAuth.getInstance().currentUser?.phoneNumber} Logado"

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity()
        }

        btnBack.setOnClickListener { finish() }
        btnChangePhoto.setOnClickListener { pickImageLauncher.launch("image/*") }
        
        btnNewLocation.setOnClickListener {
            // RZ - Passa a localização ATUAL do carro para que o mapa inicie nela
            val intent = Intent(this, SelectLocationActivity::class.java).apply {
                putExtra("LAT", carLat)
                putExtra("LONG", carLong)
            }
            selectLocationLauncher.launch(intent)
        }

        btnUpdate.setOnClickListener { updateCar() }
        btnDelete.setOnClickListener { showDeleteConfirmation() }
    }

    private fun loadCarData() {
        carId = intent.getStringExtra("ID")
        imageUrl = intent.getStringExtra("IMAGE_URL")
        etCarName.setText(intent.getStringExtra("NAME"))
        etCarYear.setText(intent.getStringExtra("YEAR"))
        etCarLicence.setText(intent.getStringExtra("LICENCE"))
        carLat = intent.getDoubleExtra("LAT", 0.0)
        carLong = intent.getDoubleExtra("LONG", 0.0)

        if (!imageUrl.isNullOrEmpty()) {
            Picasso.get().load(imageUrl).placeholder(R.drawable.fotopadrao).error(R.drawable.fotopadrao).into(ivCarPhoto)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        updateMapPosition()
    }

    private fun updateMapPosition() {
        mMap?.let { map ->
            val carLocation = LatLng(carLat, carLong)
            map.clear()
            map.addMarker(MarkerOptions().position(carLocation).title(etCarName.text.toString()))
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(carLocation, 15f))
        }
    }

    private fun updateCar() {
        val name = etCarName.text.toString()
        val year = etCarYear.text.toString()
        val licence = etCarLicence.text.toString()

        if (name.isEmpty() || year.isEmpty() || licence.isEmpty()) {
            logStatus("Erro: Preencha todos os campos.")
            return
        }

        lifecycleScope.launch {
            showLoading("Atualizando dados...")
            
            var finalImageUrl = imageUrl
            if (selectedImageUri != null) {
                val uploadedUrl = FirebaseStorageManager.uploadImage(selectedImageUri!!)
                if (uploadedUrl != null) finalImageUrl = uploadedUrl
            }

            val carToUpdate = Car(
                id = carId, name = name, year = year, licence = licence,
                imageUrl = finalImageUrl, place = Place(carLat, carLong)
            )

            if (carRepository.updateCar(carToUpdate)) {
                Toast.makeText(this@CarMapActivity, "Carro atualizado!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                logStatus("Erro ao atualizar API.")
                hideLoading()
            }
        }
    }

    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Apagar Carro")
            .setMessage("Tem certeza que deseja remover este carro?")
            .setPositiveButton("Sim") { _, _ -> deleteCar() }
            .setNegativeButton("Não", null)
            .show()
    }

    private fun deleteCar() {
        carId?.let { id ->
            lifecycleScope.launch {
                showLoading("Removendo carro...")
                if (carRepository.deleteCar(id)) {
                    Toast.makeText(this@CarMapActivity, "Carro removido!", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    logStatus("Erro ao apagar carro.")
                    hideLoading()
                }
            }
        }
    }

    private fun showLoading(message: String) {
        tvLoadingMessage.text = message
        loadingOverlay.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        loadingOverlay.visibility = View.GONE
    }

    private fun logStatus(message: String) {
        tvErrorLog.text = "RZ Console: $message"
    }
}
