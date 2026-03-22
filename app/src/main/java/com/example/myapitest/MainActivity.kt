package com.example.myapitest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapitest.adapter.CarAdapter
import com.example.myapitest.databinding.ActivityMainBinding
import com.example.myapitest.model.Car
import com.example.myapitest.model.Place
import com.example.myapitest.network.RetrofitInstance
import com.example.myapitest.utils.uploadImageToFirebase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var carAdapter: CarAdapter

    // Para selecionar imagem da galeria
    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { uploadCar(it) }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupRecyclerView()
        setupView()
        loadCars()

        // Swipe para atualizar
        binding.swipeRefreshLayout.setOnRefreshListener { loadCars() }

        // Botão de adicionar carro
        binding.addCta.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }
    }

    private fun setupRecyclerView() {
        carAdapter = CarAdapter(emptyList()) { car ->
            val intent = Intent(this, CarMapActivity::class.java)
            intent.putExtra("place", car.place)
            startActivity(intent)
        }
        binding.recyclerView.apply {
            adapter = carAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun setupView() {
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun loadCars() {
        binding.swipeRefreshLayout.isRefreshing = true
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val cars: List<Car> = RetrofitInstance.api.getCars()
                runOnUiThread {
                    carAdapter.setCars(cars)
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Erro ao carregar carros: ${e.message}", Toast.LENGTH_SHORT).show()
                    binding.swipeRefreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun uploadCar(uri: Uri) {
        uploadImageToFirebase(uri) { imageUrl ->
            if (imageUrl != null) {
                val newCar = Car(
                    id = System.currentTimeMillis().toString(),
                    name = "Meu Carro",
                    year = "2023/2023",
                    licence = "XYZ-1234",
                    imageUrl = imageUrl,
                    place = Place(lat = -23.5, long = -46.6)
                )

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        RetrofitInstance.api.createCar(newCar)
                        loadCars() // Atualiza lista
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Carro enviado!", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Erro ao enviar carro: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Erro ao enviar imagem", Toast.LENGTH_SHORT).show()
            }
        }
    }
}