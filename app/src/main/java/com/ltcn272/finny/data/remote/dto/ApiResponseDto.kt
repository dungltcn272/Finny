package com.ltcn272.finny.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Ánh xạ từ APIResponse<T> trong APIResponse.swift.
 * Đây là wrapper chung cho hầu hết các phản hồi từ backend.
 * T đại diện cho kiểu dữ liệu cụ thể (ví dụ: AuthDataDto, BudgetDataDto, List<PriceDto>).
 */
data class ApiResponseDto<T>(
    @SerializedName("status") val status: Int,
    @SerializedName("data") val data: T? = null,
    @SerializedName("message") val message: String
)

/**
 * DTO cho phản hồi upload ảnh (Ánh xạ từ APIClient logic)
 */
data class UploadImageResponseDto(
    @SerializedName("image_url") val imageUrl: String
)