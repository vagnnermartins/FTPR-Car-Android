package com.utfpr.psil.cartrack.di

import android.content.Context
import com.utfpr.psil.cartrack.data.datasources.AuthDataSource
import com.utfpr.psil.cartrack.data.datasources.AuthDataSourceImpl
import com.utfpr.psil.cartrack.data.datasources.CarApiService
import com.utfpr.psil.cartrack.data.datasources.CarImagesDataSource
import com.utfpr.psil.cartrack.data.datasources.CarImagesFirebaseStorageDataSourceImpl
import com.utfpr.psil.cartrack.data.datasources.GooglePlaceDataSourceImpl
import com.utfpr.psil.cartrack.data.datasources.PlaceDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun providesGooglePlaceDataSource(@ApplicationContext context: Context): PlaceDataSource =
        GooglePlaceDataSourceImpl(context)

    @Provides
    @Singleton
    fun providesAuthDataSource(): AuthDataSource = AuthDataSourceImpl()

    @Provides
    @Singleton
    fun providesCarImagesDataSource(): CarImagesDataSource =
        CarImagesFirebaseStorageDataSourceImpl()

    @Provides
    @Singleton
    fun providesCarApiService(): CarApiService = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:3000/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CarApiService::class.java)
}