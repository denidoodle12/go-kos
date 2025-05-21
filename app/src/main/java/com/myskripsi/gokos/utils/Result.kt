package com.myskripsi.gokos.utils

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()

    // Helper functions untuk mempermudah penggunaan
    val isLoading get() = this is Loading
    val isSuccess get() = this is Success
    val isError get() = this is Error

    // Function untuk mendapatkan data jika Result adalah Success
    fun getOrNull(): T? = when (this) {
        is Success -> data
        else -> null
    }

    // Function untuk mendapatkan error message jika Result adalah Error
    fun errorMessage(): String = when (this) {
        is Error -> message
        else -> ""
    }
}