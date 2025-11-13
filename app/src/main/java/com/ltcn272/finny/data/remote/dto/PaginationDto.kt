package com.ltcn272.finny.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Ánh xạ từ Pagination trong Common/Pagination.swift
 */
data class PaginationDto(
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("last_page") val lastPage: Int,
    @SerializedName("per_page") val perPage: Int,
    @SerializedName("total") val total: Int
)