package com.example.arquitectura

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.arquitectura.databinding.ActivitySubpaginaBinding

class SubpaginaActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySubpaginaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySubpaginaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener datos del Intent
        val titulo = intent.getStringExtra("TITULO")
        val descripcion = intent.getStringExtra("DESCRIPCION")

        // Configurar la vista con los datos
        binding.tituloTextView.text = titulo
        binding.descripcionTextView.text = descripcion
    }
}
