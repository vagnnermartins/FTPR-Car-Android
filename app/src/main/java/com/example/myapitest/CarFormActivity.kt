package com.example.myapitest

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapitest.databinding.ActivityCarFormBinding

class CarFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCarFormBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCarFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupListeners()
        // Map will be setup in Phase 4
    }

    private fun setupToolbar() {
        // Assume no action bar is set from the Theme, but we might want a manual Back arrow.
        // For now, relying on native back button is sufficient until fully styled.
    }

    private fun setupListeners() {
        binding.buttonSave.setOnClickListener {
            saveCar()
        }

        binding.imageContainer.setOnClickListener {
            // Implement Image Picker logic in Phase 3
            Toast.makeText(this, "Selecionar Imagem (Fase 3)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveCar() {
        val name = binding.editTextName.text.toString()
        val year = binding.editTextYear.text.toString()
        val licence = binding.editTextLicence.text.toString()

        if (name.isBlank() || year.isBlank() || licence.isBlank()) {
            Toast.makeText(this, "Preencha os campos obrigatórios", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.buttonSave.isEnabled = false

        // Fake loading for now. Will map to ViewModel in the next step.
        binding.progressBar.postDelayed({
            binding.progressBar.visibility = View.GONE
            binding.buttonSave.isEnabled = true
            Toast.makeText(this, "Salvo com sucesso!", Toast.LENGTH_SHORT).show()
            finish()
        }, 1500)
    }
}
