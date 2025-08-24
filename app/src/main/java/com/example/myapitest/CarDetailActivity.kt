package com.example.myapitest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapitest.ui.loadUrl
import com.example.myapitest.databinding.ActivityCarDetailBinding
import com.example.myapitest.models.Car
import com.example.myapitest.service.Result
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.safeApiCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CarDetailActivity : AppCompatActivity() {


    private lateinit var binding: ActivityCarDetailBinding

    private lateinit var car: Car

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarDetailBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupView()
        loadItem()
    }

    private fun loadItem() {
        val itemId = intent.getStringExtra(ARG_ID) ?: ""

        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.apiService.getCar(itemId) }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Error -> {
                        Toast.makeText(
                            this@CarDetailActivity,
                            "Erro ao carregar item",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }

                    is Result.Success -> {
                        car = result.data.value
                        handleSuccess()
                    }
                }
            }
        }
    }

    private fun handleSuccess() {
        binding.name.setText(car.name)
        binding.year.setText(car.year)
        binding.license.setText(car.licence)
        binding.image.loadUrl(car.imageUrl)
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.deleteCTA.setOnClickListener {
            deleteItem()
        }
        binding.editCTA.setOnClickListener {
            editItem()
        }
    }

    private fun editItem() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall {
                RetrofitClient.apiService.updateCar(
                    car.id,
                    car.copy(
                        name = binding.name.text.toString(),
                        year = binding.year.text.toString(),
                        licence = binding.license.text.toString()

                    )
                )
            }
            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Error -> {
                        Toast.makeText(
                            this@CarDetailActivity,
                            "Erro ao atualizar Carro",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is Result.Success -> {
                        Toast.makeText(
                            this@CarDetailActivity,
                            "Carro atualizado com sucesso",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun deleteItem() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.apiService.deleteCar(car.id) }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Error -> {
                        Toast.makeText(
                            this@CarDetailActivity,
                            "Erro ao deletar Carro",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    is Result.Success -> {
                        Toast.makeText(
                            this@CarDetailActivity,
                            "Carro deletado com sucesso",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            currentFocus?.let { view ->
                if (view !is EditText) {
                    hideKeyboard()
                    view.clearFocus()
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    fun hideKeyboard() {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let {
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    companion object {

        private const val ARG_ID = "ARG_ID"

        fun newIntent(
            context: Context,
            itemId: String
        ) =
            Intent(context, CarDetailActivity::class.java).apply {
                putExtra(ARG_ID, itemId)
            }
    }
}