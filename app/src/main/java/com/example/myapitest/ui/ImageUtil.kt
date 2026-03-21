package com.example.myapitest.ui

import android.widget.ImageView
import com.example.myapitest.R
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

/**
 * Carrega uma URL de imagem em um ImageView utilizando Picasso.
 * Trata erros de rede (como 404) exibindo uma imagem de erro padrão.
 */
fun ImageView.loadUrl(url: String?) {
    // Se a URL for nula ou vazia, define a imagem de erro imediatamente
    if (url.isNullOrBlank()) {
        android.util.Log.w("Picasso", "URL nula ou vazia. Definindo ic_error.")
        setImageResource(R.drawable.ic_error)
        return
    }

    android.util.Log.d("Picasso", "Iniciando carregamento: $url")

    Picasso.get()
        .load(url)
        .placeholder(R.drawable.ic_download) // Exibe enquanto carrega
        .error(R.drawable.ic_error)       // Exibe se der 404, 500 ou erro de rede
        .into(this, object : Callback {
            override fun onSuccess() {
                android.util.Log.d("Picasso", "Sucesso ao carregar: $url")
            }

            override fun onError(e: Exception?) {
                // Aqui capturamos o erro 404 que você reportou
                android.util.Log.e("Picasso", "Erro ao carregar imagem (404 ou rede): $url. Erro: ${e?.message}")
            }
        })
}
