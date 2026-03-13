package com.example.myapitest

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapitest.adapter.CarAdapter
import com.example.myapitest.databinding.ActivityMainBinding
import com.example.myapitest.viewmodel.CarUiState
import com.example.myapitest.viewmodel.CarViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: CarViewModel by viewModels()
    private val adapter = CarAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (FirebaseAuth.getInstance().currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        requestLocationPermission()
        setupView()
        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is CarUiState.Loading -> {
                        binding.swipeRefreshLayout.isRefreshing = true
                    }
                    is CarUiState.Success -> {
                        binding.swipeRefreshLayout.isRefreshing = false
                        adapter.submitList(state.cars)
                        
                        if (state.cars.isEmpty()) {
                            binding.textViewEmpty.visibility = android.view.View.VISIBLE
                            binding.recyclerView.visibility = android.view.View.GONE
                        } else {
                            binding.textViewEmpty.visibility = android.view.View.GONE
                            binding.recyclerView.visibility = android.view.View.VISIBLE
                        }
                    }
                    is CarUiState.Error -> {
                        binding.swipeRefreshLayout.isRefreshing = false
                        binding.textViewEmpty.visibility = android.view.View.VISIBLE
                        binding.textViewEmpty.text = "Erro ao carregar os dados:\n${state.message}"
                        binding.recyclerView.visibility = android.view.View.GONE
                        Log.e("MainActivity", "Error: ${state.message}")
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchItems()
    }

    private fun setupView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        
        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchItems()
        }

        binding.addCta.setOnClickListener {
            val intent = Intent(this, CarFormActivity::class.java)
            startActivity(intent)
        }
    }

    private fun requestLocationPermission() {
        // TODO: Location Logic
    }

    private fun fetchItems() {
        viewModel.fetchCars()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
