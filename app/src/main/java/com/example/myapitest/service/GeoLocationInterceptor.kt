package com.example.myapitest.service

import com.example.myapitest.database.dao.UserLocationDao
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class GeoLocationInterceptor(
    private val userLocationDao: UserLocationDao
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val userLocationLast = runBlocking {
            userLocationDao.getLastLocation()
        }

        val originalRequest: Request = chain.request()
        val newRequest = originalRequest.newBuilder()
            .addHeader("x-data-latitude", userLocationLast?.latitude.toString())
            .addHeader("x-data-longitude", userLocationLast?.longitude.toString())
            .build()
        return chain.proceed(newRequest)
    }

}