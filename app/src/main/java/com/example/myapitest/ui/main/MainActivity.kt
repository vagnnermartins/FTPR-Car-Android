package com.example.myapitest.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapitest.R
import com.example.myapitest.data.FirestoreManager
import com.example.myapitest.data.api.RetrofitClient
import com.example.myapitest.databinding.ActivityMainBinding
import com.example.myapitest.repository.CarRepository
import com.example.myapitest.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var carAdapter: CarAdapter
    private lateinit var carRepository: CarRepository
    private var lastLoadedCars: List<com.example.myapitest.data.model.Car> = emptyList()
    
    private var isDetailOpening = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        val userInfo = "${user.phoneNumber} Logado"
        binding.tvUserInfo.text = userInfo
        binding.tvUserFooter.text = "Usuário: ${user.phoneNumber}"
        
        val carApi = RetrofitClient.getInstance(this)
        carRepository = CarRepository(carApi)

        setupRecyclerView()
        setupView()
    }

    override fun onResume() {
        super.onResume()
        isDetailOpening = false
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        fetchItems()
    }

    private fun setupRecyclerView() {
        carAdapter = CarAdapter(emptyList()) { car ->
            if (!isDetailOpening) {
                isDetailOpening = true
                val intent = Intent(this, CarMapActivity::class.java).apply {
                    putExtra("ID", car.id)
                    putExtra("NAME", car.name)
                    putExtra("YEAR", car.year)
                    putExtra("LICENCE", car.licence)
                    putExtra("IMAGE_URL", car.imageUrl)
                    putExtra("LAT", car.place?.lat ?: 0.0)
                    putExtra("LONG", car.place?.long ?: 0.0)
                }
                startActivity(intent)
            }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = carAdapter
        }
    }

    private fun setupView() {
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.btnCloseApp.setOnClickListener {
            finishAffinity()
        }

        binding.addCta.setOnClickListener {
            startActivity(Intent(this, AddCarActivity::class.java))
        }

        binding.btnRefresh.setOnClickListener {
            carAdapter.updateData(emptyList())
            fetchItems()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchItems()
        }

        binding.btnBackup.setOnClickListener {
            if (lastLoadedCars.isNotEmpty()) {
                lifecycleScope.launch {
                    // RZ - Mostra o overlay e reseta o status
                    binding.loadingOverlay.visibility = View.VISIBLE
                    binding.tvBackupStatus.text = "Iniciando..."
                    logError("RZ Console: Iniciando Backup no Cloud Firestore...")

                    // RZ - Chama o backup passando o callback para atualizar a segunda linha
                    val result = FirestoreManager.backupAllToFirestoreDetailed(this@MainActivity, lastLoadedCars) { status ->
                        // Atualiza o texto na Thread Principal
                        runOnUiThread {
                            binding.tvBackupStatus.text = status
                        }
                    }

                    if (result.success) {
                        val msg = "Backup Salvo! Host: firestore.google.com | Collection: ${result.collectionPath}"
                        Toast.makeText(this@MainActivity, "Backup Concluído!", Toast.LENGTH_LONG).show()
                        logError("RZ Console: $msg")
                    } else {
                        logError("RZ Console Erro: ${result.message}")
                        Toast.makeText(this@MainActivity, "Erro no Backup! Veja o rodapé.", Toast.LENGTH_SHORT).show()
                    }

                    binding.loadingOverlay.visibility = View.GONE
                }
            } else {
                Toast.makeText(this, "Nenhum dado para backup", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchItems() {
        lifecycleScope.launch {
            binding.swipeRefreshLayout.isRefreshing = true
            try {
                val cars = carRepository.getCars()
                lastLoadedCars = cars
                
                if (cars.isEmpty()) {
                    carAdapter.updateData(emptyList())
                    binding.tvEmptyMessage.text = "Não Existem Carros Cadastrados"
                    binding.tvEmptyMessage.visibility = View.VISIBLE
                    logError("RZ Console: Lista Vazia.")
                } else {
                    carAdapter.updateData(cars)
                    binding.tvEmptyMessage.visibility = View.GONE
                    logError("RZ Console: Sucesso! ${cars.size} carros carregados.")
                }
            } catch (e: Exception) {
                carAdapter.updateData(emptyList())
                binding.tvEmptyMessage.text = "Verifique sua rede - sem acesso ao servidor."
                binding.tvEmptyMessage.visibility = View.VISIBLE
                logError("RZ Console Erro: Servidor Inacessível.")
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun logError(message: String) {
        binding.tvErrorLog.text = message
    }
}
