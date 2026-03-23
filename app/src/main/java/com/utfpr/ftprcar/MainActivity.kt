package com.utfpr.ftprcar

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.utfpr.ftprcar.databinding.ActivityMainBinding
import com.utfpr.ftprcar.model.Car
import com.utfpr.ftprcar.repository.CarRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: CarAdapter
    private val repository = CarRepository()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        setupRecyclerView()
        setupSwipeRefresh()
        setupFab()
    }

    override fun onResume() {
        super.onResume()
        fetchCars()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_logout) {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupRecyclerView() {
        adapter = CarAdapter(this, emptyList()) { car ->
            val intent = Intent(this, CarDetailActivity::class.java).apply {
                putExtra(CarDetailActivity.EXTRA_CAR_ID,       car.id)
                putExtra(CarDetailActivity.EXTRA_CAR_NAME,     car.name)
                putExtra(CarDetailActivity.EXTRA_CAR_YEAR,     car.year)
                putExtra(CarDetailActivity.EXTRA_CAR_LICENCE,  car.licence)
                putExtra(CarDetailActivity.EXTRA_CAR_IMAGE,    car.imageUrl)
                putExtra(CarDetailActivity.EXTRA_CAR_LAT,      car.place.lat)
                putExtra(CarDetailActivity.EXTRA_CAR_LONG,     car.place.lng)
            }
            startActivity(intent)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener { fetchCars() }
    }

    private fun setupFab() {
        binding.addCta.setOnClickListener {
            startActivity(Intent(this, AddCarActivity::class.java))
        }
    }

    private fun fetchCars() {
        lifecycleScope.launch {
            binding.swipeRefreshLayout.isRefreshing = true
            try {
                val cars = repository.getCars()
                adapter.updateCars(cars)
            } catch (e: Exception) {
                Toast.makeText(
                    this@MainActivity,
                    "Failed to load cars: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
    }
}
