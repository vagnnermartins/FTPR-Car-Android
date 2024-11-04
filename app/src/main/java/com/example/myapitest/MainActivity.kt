package com.example.myapitest

import ItemAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapitest.databinding.ActivityMainBinding
import com.example.myapitest.models.Car
import com.example.myapitest.service.Result
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.safeApiCall
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var googleSignInClient: GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestLocationPermission()
        setupGoogleAuthUser()
        setupView()



        // 1- Criar tela de Login com algum provedor do Firebase (Telefone, Google)
        //      Cadastrar o Seguinte celular para login de test: +5511912345678
        //      Código de verificação: 101010

        // 2- Criar Opção de Logout no aplicativo

        // 3- Integrar API REST /car no aplicativo
        //      API será disponibilida no Github
        //      JSON Necessário para salvar e exibir no aplicativo
        //      O Image Url deve ser uma foto armazenada no Firebase Storage
        //      { "id": "001", "imageUrl":"https://image", "year":"2020/2020", "name":"Gaspar", "licence":"ABC-1234", "place": {"lat": 0, "long": 0} }

        // Opcionalmente trabalhar com o Google Maps ara enviar o place
    }


    override fun onResume() {
        super.onResume()
        fetchItems()
    }


    private fun setupView() {
        logoutGoogle()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = true
            fetchItems()
        }
        binding.addCta.setOnClickListener {
        //TODO: Adicionar novo item
        }
    }

    private  fun setupGoogleAuthUser() {
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN)
    }




    private fun handleOnSuccess(data: List<Car>) {
        val adapter = ItemAdapter(data) {
            // listener do item clicado
        }
        binding.recyclerView.adapter = adapter
    }


    private fun logoutGoogle() {
        binding.logoutButton.setOnClickListener {
            googleSignInClient.signOut()
                .addOnCompleteListener {
                    startActivity(LoginActivity.newIntent(this))
                    finish()
                }
            Toast.makeText(this, "Logout", Toast.LENGTH_SHORT).show()
        }

    }

    private fun requestLocationPermission() {
        // TODO
    }

    private fun fetchItems() {
        // Alterando execução para IO thread
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.apiService.getCars() }

            // Alterando execução para Main thread
            withContext(Dispatchers.Main) {
                binding.swipeRefreshLayout.isRefreshing = false
                when (result) {
                    is Result.Error -> {
                        Log.e("MainActivity", "Error: ${result.message}")
                        Toast.makeText(this@MainActivity, "Erro", Toast.LENGTH_SHORT).show()
                    }
                    is Result.Success -> handleOnSuccess(result.data)
                }
            }
        }
    }


    companion object {

        fun newIntent(context: Context) = Intent(context, MainActivity::class.java)
    }
}
