package com.ltcn272.finny.data.remote.dto

import com.google.gson.annotations.SerializedName

data class StatementResponseDto(
    @SerializedName("success") val success: Boolean,
    @SerializedName("pdf") val pdfBase64: String
)
