package com.example.myapitest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapitest.adapter.CarAdapter
import com.example.myapitest.databinding.ActivityMainBinding
import com.example.myapitest.viewmodel.CarViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private val viewModel: CarViewModel by viewModels()
    private lateinit var adapter: CarAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchCars()
    }

    private fun setupRecyclerView() {
        adapter = CarAdapter(
            emptyList(),
            onCarClick = { car ->
                val intent = Intent(this, MapsActivity::class.java)
                intent.putExtra("LAT", car.place.lat)
                intent.putExtra("LONG", car.place.long)
                intent.putExtra("CAR_NAME", car.name)
                startActivity(intent)
            },
            onEditClick = { car ->
                startActivity(CarDetailActivity.createEditIntent(this, car))
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.cars.observe(this) { cars ->
            adapter.updateCars(cars)
            binding.swipeRefreshLayout.isRefreshing = false
            
            // Gerencia estado vazio
            if (cars.isEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.emptyState.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        }

        viewModel.error.observe(this) { message ->
            Toast.makeText(this, getString(R.string.load_error, message), Toast.LENGTH_SHORT).show()
            binding.swipeRefreshLayout.isRefreshing = false
            
            // Em caso de erro e lista vazia, mostra estado vazio ou mantém anterior
            if (adapter.itemCount == 0) {
                binding.emptyState.visibility = View.VISIBLE
            }
        }
    }

    private fun setupListeners() {
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.fetchCars()
        }
        
        binding.addCta.setOnClickListener {
            startActivity(Intent(this, CarDetailActivity::class.java))
        }
    }
}
