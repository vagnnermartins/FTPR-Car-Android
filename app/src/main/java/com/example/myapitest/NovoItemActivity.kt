package com.example.myapitest

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings.Secure
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapitest.databinding.ActivityNovoItemBinding
import com.example.myapitest.model.Item
import com.example.myapitest.model.ItemValue
import com.example.myapitest.services.Result
import com.example.myapitest.services.RetrofitClient
import com.example.myapitest.services.safeApiCall
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

class NovoItemActivity : AppCompatActivity() {

    private lateinit var binding : ActivityNovoItemBinding
    private lateinit var imageUri: Uri
    private var imagemArquivo: File? = null

    //Inicia o laucher da camera e após a confirmação, colocar o texto no etText
    private var iniciaCamera:ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){
        result -> if (result.resultCode == Activity.RESULT_OK){
          binding.etImageUrl.setText("Imagem capturada")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNovoItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupView()
    }

    private fun setupView() {
        //É preciso alterar no Manifest o tema da Activity para não ter toolbar
        setSupportActionBar(binding.toolbar)
       supportActionBar?.setDisplayHomeAsUpEnabled(true)
       supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.btFoto.setOnClickListener {
            tirarFoto()
        }

        binding.btSalvar.setOnClickListener {
            salvar()
        }

    }

    private fun tirarFoto() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED){
            abrirCamera()
        } else {
            solicitaPermissao()
        }
    }

    private fun solicitaPermissao() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.CAMERA), CAMERA_REQUEST_CODE
        )
    }

    private fun abrirCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        imageUri = criarUriImage()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        iniciaCamera.launch(intent)
    }

    private fun criarUriImage(): Uri {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        imagemArquivo = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            storageDir      /* directory */
        )

        return FileProvider.getUriForFile(
            this,  // Contexto
            "${packageName}.fileprovider", // Autoridade
            imagemArquivo!! // O arquivo
        )

    }

    private fun salvar() {
        if (!valicacao())
            return
        val imageBitmap = BitmapFactory.decodeFile(imagemArquivo!!.path)
        uploadImage(imageBitmap)
    }

    private fun uploadImage(bitmap: Bitmap) {
        val armazenamento = FirebaseStorage.getInstance()
        val refArmazenamento = armazenamento.reference
        val refImagem = refArmazenamento.child("images/${UUID.randomUUID()}.jpg")
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()
        val uploadData = refImagem.putBytes(data)
        binding.carregandoImagem.visibility = View.VISIBLE
        binding.btFoto.isEnabled = false

        uploadData
            .addOnFailureListener {
                Toast.makeText(this,"Falha no upload, tente novamente", Toast.LENGTH_SHORT).show()
            }
            .addOnSuccessListener {
                binding.carregandoImagem.visibility = View.GONE
                binding.btFoto.isEnabled = true
                refImagem.downloadUrl.addOnSuccessListener { uri ->
                salvarDados(uri.toString())}
            }
    }

    private fun salvarDados(imageUrl: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val vazio = ""
            val place = ItemValue(
                vazio,
                vazio
            )
            val id = SecureRandom().nextInt().toString()
            val dados = Item(
                id,
                imageUrl,
                binding.etAno.text.toString(),
                binding.etNome.text.toString(),
                binding.etPlaca.text.toString(),
                place
            )
            System.out.println(dados)
            val resultado = safeApiCall { RetrofitClient.apiService.addItem(dados) }
            withContext(Dispatchers.Main){
                when (resultado){
                    is Result.Error -> {
                        Toast.makeText(this@NovoItemActivity, "Erro ao adicionar",
                            Toast.LENGTH_SHORT).show()
                    }
                    is Result.Success -> {
                        Toast.makeText(this@NovoItemActivity, "Item Adicionado com Sucesso",
                            Toast.LENGTH_SHORT).show()
                    }
                }
                finish()
            }
        }
    }

    private fun valicacao(): Boolean {
        if (binding.etNome.text.toString().isBlank()) {
            Toast.makeText(this, getString(R.string.preencher_campo,"Nome"),Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.etAno.text.toString().isBlank()){
            Toast.makeText(this, getString(R.string.preencher_campo,"Ano"),Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.etPlaca.text.toString().isBlank()){
            Toast.makeText(this, getString(R.string.preencher_campo,"Placa"),Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.etImageUrl.text.toString().isBlank()){
            Toast.makeText(this, getString(R.string.preencher_campo,"ImageUrl"),Toast.LENGTH_SHORT).show()
            return false
        }
        return true

    }


    companion object{
        private const val CAMERA_REQUEST_CODE = 101

        fun newIntent(context: Context)
        = Intent(context, NovoItemActivity::class.java)
    }

}