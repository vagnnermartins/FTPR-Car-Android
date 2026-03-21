package com.example.myapitest.service

import retrofit2.HttpException
import java.io.IOException

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val code: Int, val message: String, val exception: Exception? = null) : Result<Nothing>()
}

suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
    return try {
        val response = apiCall()
        Result.Success(response)
    } catch (e: Exception) {
        when (e) {
            is HttpException -> {
                val code = e.code()
                val message = e.message()
                Result.Error(code, message, e)
            }
            is IOException -> {
                Result.Error(-2, "Network error: check your connection", e)
            }
            else -> {
                Result.Error(-1, e.message ?: "Unknown error: ${e.javaClass.simpleName}", e)
            }
        }
    }
}
