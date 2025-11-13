package com.ltcn272.finny.domain.model

import java.time.ZonedDateTime

data class Pagination(
    val currentPage: Int,
    val lastPage: Int,
    val perPage: Int,
    val total: Int
)
