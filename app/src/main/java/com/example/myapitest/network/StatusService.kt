package com.example.myapitest.network

sealed interface StatusService<T> {
    object Idle : StatusService<Nothing>
    data class Success<T>(val response: T) : StatusService<T>
    data class Error(val errorCode: Int?, val message: String?) : StatusService<Nothing>
    object UnknownError : StatusService<Nothing>
    object Loading : StatusService<Nothing>
}