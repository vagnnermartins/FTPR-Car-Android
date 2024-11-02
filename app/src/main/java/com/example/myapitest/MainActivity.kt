package com.example.myapitest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapitest.adapter.ItemAdapter
import com.example.myapitest.databinding.ActivityMainBinding
import com.example.myapitest.model.Item
import com.example.myapitest.services.RetrofitClient.apiService
import com.example.myapitest.services.safeApiCall
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.myapitest.services.Result

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestLocationPermission()
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

        // Opcionalmente trabalhar com o Google Maps para enviar o place
    }

    override fun onResume() {
        super.onResume()
       fetchItems()
    }

        //Cria o menu usando o .xml criado
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }
        //Cria a ação a seu clicado no menu se clicado no retorno do id
    override fun onOptionsItemSelected(itemMenu: MenuItem): Boolean {
        return when(itemMenu.itemId) {
            R.id.menu_logout -> {
                logOut()
                true
            } else -> super.onOptionsItemSelected(itemMenu)
        }
    }
        //Aqui realiza o logOut no Firebase
    private fun logOut() {
        FirebaseAuth.getInstance().signOut() //faz logout no firebase
        val intent = LoginActivity.newIntent(this) //Aqui começa o processo de limpar a task e matar a MainActivity
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun setupView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = true
            fetchItems()
        }

      binding.addCta.setOnClickListener {
        startActivity(NovoItemActivity.newIntent(this))
       }

    }

    private fun requestLocationPermission() {

    }

    private fun fetchItems() {
        CoroutineScope(Dispatchers.IO).launch { //Aqui faz a consulta dos dados dentro da Api
            val resultado = safeApiCall { apiService.getItems()
            }

            withContext(Dispatchers.Main) {
                binding.swipeRefreshLayout.isRefreshing = false
                when (resultado) {
                    is Result.Error -> {
                        Toast.makeText(this@MainActivity, "ERRO AO CARREGAR", Toast.LENGTH_LONG).show()}
                    is Result.Success -> {
                       binding.recyclerView.adapter = ItemAdapter(resultado.data)
                    }
                }
            }

       }}

   // private fun handleOnSucess(data: List<Item>){
     //   val adapter = ItemAdapter(data){

       // }
      //  binding.recyclerView.adapter = adapter



    companion object{
        fun newIntent(context: Context) = Intent(context, MainActivity::class.java)
    }
}


