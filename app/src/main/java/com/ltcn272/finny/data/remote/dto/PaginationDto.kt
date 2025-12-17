package com.ltcn272.finny.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PaginationDto(

    @SerializedName("page")
    val page: Int,

    @SerializedName("limit")
    val limit: Int,

    @SerializedName("total")
    val total: Int,

    @SerializedName("total_page")
    val totalPage: Int
)