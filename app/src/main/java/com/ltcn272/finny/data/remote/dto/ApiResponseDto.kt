package com.ltcn272.finny.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ApiResponseDto<T>(
    @SerializedName("status") val status: Int,
    @SerializedName("data") val data: T? = null,
    @SerializedName("message") val message: String
)

data class UploadImageResponseDto(
    @SerializedName("image_url") val imageUrl: String
)