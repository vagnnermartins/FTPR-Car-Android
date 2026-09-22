package com.example.myapitest.service

import com.example.myapitest.database.dao.CarLocationDao
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class GeoLocationInterceptor(
    private val carLocationDao: CarLocationDao
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val carLocationLast = runBlocking {
            carLocationDao.getLastLocation()
        }

        val originalRequest: Request = chain.request()
        val newRequest = originalRequest.newBuilder()
            .addHeader("x-data-latitude", carLocationLast?.latitude.toString())
            .addHeader("x-data-longitude", carLocationLast?.longitude.toString())
            .build()
        return chain.proceed(newRequest)
    }

}
