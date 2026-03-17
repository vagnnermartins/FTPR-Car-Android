package com.example.myapitest.data.api

import android.content.Context
import com.example.myapitest.R
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// RZ - O RetrofitClient agora recebe o contexto para buscar a URL configurada 
// no arquivo configuracoesapi.xml (parâmetro URLAcesso).
// Ele funciona como um singleton configurado sob demanda.

object RetrofitClient {

    private var retrofit: Retrofit? = null

    fun getInstance(context: Context): CarApi {
        if (retrofit == null) {
            // RZ - Puxa a URLAcesso do XML de configurações
            val baseUrl = context.getString(R.string.URLAcesso)

            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!.create(CarApi::class.java)
    }
}
