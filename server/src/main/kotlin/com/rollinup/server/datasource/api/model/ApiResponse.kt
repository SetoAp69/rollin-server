package com.rollinup.server.datasource.api.model

sealed class ApiResponse<out T>() {
    data class Success<out T>(val data: T) : ApiResponse<T>()
    data class Error(val message: String) : ApiResponse<Nothing>()
}
