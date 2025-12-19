package com.ltcn272.finny.domain.util

import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

sealed class AppResult<out T> {
    data class Success<out T>(val data: T) : AppResult<T>()
    data class Error(val errorType: ErrorType) : AppResult<Nothing>()
    data object Loading : AppResult<Nothing>()
}

enum class ErrorType {
    NETWORK,
    TIMEOUT,
    UNAUTHORIZED,
    SERVER_ERROR,
    UNKNOWN
}

fun Exception.toErrorType(): ErrorType {
    return when (this) {
        is SocketTimeoutException -> ErrorType.TIMEOUT
        is IOException -> ErrorType.NETWORK
        is HttpException -> when (this.code()) {
            401, 403 -> ErrorType.UNAUTHORIZED
            in 500..599 -> ErrorType.SERVER_ERROR
            else -> ErrorType.UNKNOWN
        }
        else -> ErrorType.UNKNOWN
    }
}
