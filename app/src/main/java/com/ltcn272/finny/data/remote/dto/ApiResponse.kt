package com.ltcn272.finny.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(

    @SerializedName("status")
    val status: Int,

    @SerializedName("data")
    val data: T,

    @SerializedName("message")
    val message: String
)

class BusinessException(
    override val message: String,
    val statusCode: Int
) : Exception(message)

fun <T> ApiResponse<T>.ensureSuccess(): T {
    if (this.status == 200) {
        return this.data
    } else {
        throw BusinessException(
            message = this.message,
            statusCode = this.status
        )
    }
}

data class UploadImageResponseDto(
    @SerializedName("image_url") val imageUrl: String
)