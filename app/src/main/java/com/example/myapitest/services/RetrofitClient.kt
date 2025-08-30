package com.example.myapitest.services

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://192.168.100.17:3000/" // endereço utilizado para acessar o servidor no emulador

    private val loggingInterceptor = HttpLoggingInterceptor().apply { //cria os logs das requisições
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) //verifica o logs das requisições
        .build()

    private val instance: Retrofit by lazy {  //lazy só vai instanciar depois de chamar
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()) //usado para converter o json no model
            .build()
    }

    val apiService = instance.create(ApiService::class.java)

}