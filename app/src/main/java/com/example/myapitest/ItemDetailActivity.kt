package com.example.myapitest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapitest.databinding.ActivityItemDetailBinding
import com.example.myapitest.model.Item
import com.example.myapitest.service.Result
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.safeApiCall
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ItemDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityItemDetailBinding

    private lateinit var item: Item

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        fetchItem()
    }

    private fun fetchItem() {
        val itemId = intent.getStringExtra(ARG_ID) ?: ""

        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall {
                RetrofitClient.apiService.getItem(itemId)
            }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Error -> {
                        Toast.makeText(this@ItemDetailActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
                    }
                    is Result.Success -> {
                        item = result.data.value
                        handleFetchItemSuccess()
                    }
                }
            }
        }
    }

    private fun handleFetchItemSuccess() {
        binding.nameTextView.text = item.name
        binding.yearTextView.text = item.year
        binding.licenseTextView.text = item.licence

        Picasso.get()
            .load(item.imageUrl)
            .placeholder(R.drawable.ic_download)
            .error(R.drawable.ic_error)
            .into(binding.imageViewCar)
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.deleteButton.setOnClickListener {
            deleteItem()
        }
    }

    private fun deleteItem() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall {
                RetrofitClient.apiService.deleteItem(item.id)
            }

            when (result) {
                is Result.Error -> {
                    Toast.makeText(this@ItemDetailActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
                is Result.Success -> {
                    finish()
                }
            }
        }
    }

    companion object {
        private const val ARG_ID = "ARG_ID"

        fun newIntent(
            context: Context,
            itemId: String
        ) = Intent(context, ItemDetailActivity::class.java).apply {
            putExtra(ARG_ID, itemId)
        }
    }
}