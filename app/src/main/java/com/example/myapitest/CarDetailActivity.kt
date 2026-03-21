package com.example.myapitest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myapitest.databinding.ActivityCarDetailBinding
import com.example.myapitest.model.Car
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.Result
import com.example.myapitest.service.safeApiCall
import com.example.myapitest.ui.loadUrl
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

class CarDetailActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityCarDetailBinding
    private lateinit var car: Car
    private var mMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Iniciando CarDetailActivity")
        binding = ActivityCarDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
        loadItem()
        setupGoogleMap()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "onMapReady: Google Map está pronto")
        mMap = googleMap

        // Verificamos se 'car' já foi carregado pela API enquanto o mapa abria
        if (::car.isInitialized) {
            loadItemInGoogleMap()
        }
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { 
            Log.d(TAG, "Navegação: Botão voltar pressionado")
            finish() 
        }

        binding.deleteCTA.setOnClickListener { deleteItem() }
        binding.editCTA.setOnClickListener { editCar() }
    }

    private fun loadItem() {
        val itemId = intent.getStringExtra(ARG_ID)
        Log.d(TAG, "loadItem: Tentando carregar item com ID: $itemId")

        if (itemId == null) {
            Log.e(TAG, "loadItem: ID recebido é nulo")
            finish()
            return
        }

        lifecycleScope.launch {
            Log.d(TAG, "loadItem: Chamando API getCar($itemId)")
            val result = safeApiCall { RetrofitClient.carApiService.getCar(itemId) }

            when (result) {
                is Result.Success -> {
                    val carResponse = result.data
                    car = carResponse.value

                    Log.d(TAG, "loadItem: Sucesso! Carro extraído: ${car.name}")
                    handleSuccess()
                }
                is Result.Error -> {
                    Log.e(TAG, "loadItem: Erro ${result.code}")
                    Toast.makeText(this@CarDetailActivity, "Erro ao carregar detalhes", Toast.LENGTH_SHORT).show()                }
            }
        }
    }

    private fun setupGoogleMap() {
        Log.d(TAG, "setupGoogleMap: Inicializando SupportMapFragment")
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun handleSuccess() {
        Log.d(TAG, "handleSuccess: Atualizando UI com ${car.name}")
        
        // Preenchendo os campos com segurança para evitar "null" na tela
        binding.name.text = car.name ?: "Sem nome"
        
        // Mapeando os campos do modelo para os IDs do layout
        binding.age.text = car.year ?: "Ano não informado"
        binding.profession.setText(car.licence ?: "Placa não informada")

        car.imageUrl?.let {
            binding.image.loadUrl(it)
        }
        
        loadItemInGoogleMap()
    }

    private fun loadItemInGoogleMap() {
        val googleMap = mMap
        if (googleMap == null) {
            Log.w(TAG, "loadItemInGoogleMap: Mapa ainda não inicializado")
            return
        }

        if (!::car.isInitialized) return

        val lat = car.place?.lat
        val lng = car.place?.long

        if (lat != null && lng != null) {
            val carLocation = LatLng(lat, lng)
            googleMap.clear() // Limpa marcadores anteriores
            googleMap.addMarker(MarkerOptions().position(carLocation).title(car.name ?: "Veículo"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(carLocation, 15f))
        } else {
            Log.w(TAG, "loadItemInGoogleMap: Localização não disponível (place ou coordenadas nulas)")
        }
    }

    private fun deleteItem() {
        lifecycleScope.launch {
            Log.d(TAG, "deleteItem: Solicitando exclusão do ID: ${car.id}")
            binding.deleteCTA.isEnabled = false
            val result = safeApiCall { RetrofitClient.carApiService.deleteCar(car.id) }

            if (result is Result.Success) {
                Log.d(TAG, "deleteItem: Sucesso na exclusão")
                Toast.makeText(this@CarDetailActivity, "Item removido com sucesso", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Log.e(TAG, "deleteItem: Erro ao excluir")
                binding.deleteCTA.isEnabled = true
                Toast.makeText(this@CarDetailActivity, "Erro ao deletar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun editCar() {
        lifecycleScope.launch {
            Log.d(TAG, "editCar: Solicitando atualização")
            binding.editCTA.isEnabled = false

            val updatedCar = car.copy(
                name = binding.name.text.toString(),
                year = binding.age.text.toString(),
                licence = binding.profession.text.toString()
            )

            val result = safeApiCall {
                RetrofitClient.carApiService.updateCar(car.id, updatedCar)
            }

            if (result is Result.Success) {
                Log.d(TAG, "editCar: Sucesso na atualização")
                Toast.makeText(this@CarDetailActivity, "Dados atualizados!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Log.e(TAG, "editCar: Erro na atualização")
                binding.editCTA.isEnabled = true
                Toast.makeText(this@CarDetailActivity, "Erro ao atualizar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val TAG = "CarDetailActivity"
        private const val ARG_ID = "arg_id"

        fun newIntent(context: Context, itemId: String): Intent {
            return Intent(context, CarDetailActivity::class.java).apply {
                putExtra(ARG_ID, itemId)
            }
        }
    }
}
