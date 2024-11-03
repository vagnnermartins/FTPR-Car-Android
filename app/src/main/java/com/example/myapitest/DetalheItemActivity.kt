package com.example.myapitest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapitest.databinding.ActivityDetalheItemBinding
import com.example.myapitest.model.Item
import com.example.myapitest.services.Result
import com.example.myapitest.services.RetrofitClient
import com.example.myapitest.services.safeApiCall
import com.example.myapitest.ui.loadUrl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetalheItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalheItemBinding

    private lateinit var itemDetalhe: Item

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalheItemBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setupView()

        carregaItem()

    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setOnClickListener{
            finish()
        }
    }

    private fun carregaItem(){
        val idItem = intent.getStringExtra(ARG_ID) ?: ""  //não pode retornar nulo

        CoroutineScope(Dispatchers.IO).launch {
            val detalhes = safeApiCall { RetrofitClient.apiService.getItem(idItem)
            }

            withContext(Dispatchers.Main){
                when (detalhes){
                    is Result.Error ->
                    {Toast.makeText(
                            this@DetalheItemActivity, "Erro ao carregar detalhe do item", Toast.LENGTH_LONG).
                                show()}
                    is Result.Success -> {
                        itemDetalhe = detalhes.data
                        handleSucess()
                    }
                }
            }
        }

    }

    private fun handleSucess() {
        binding.tvNome.text = itemDetalhe.name
        binding.tvAno.text = itemDetalhe.year
        binding.tvPlaca.text = itemDetalhe.licence
        binding.imageView.loadUrl(itemDetalhe.imageUrl)
    }

    companion object{
        private const val ARG_ID = "ARG_ID"
        fun newIntent(context: Context, idItem: String) =
            Intent(context, DetalheItemActivity::class.java).apply {
                putExtra(ARG_ID, idItem)
            }
    }
}