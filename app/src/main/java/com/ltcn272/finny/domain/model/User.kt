package com.ltcn272.finny.domain.model

import java.time.ZonedDateTime

data class User(
    val id: String,
    val displayName: String?,
    val email: String,
    val avatar: String?,
    val plan: String,
    val isActive: Boolean,
    val currency: String?,
    val isAdmin: Boolean,
    val lastLogin: ZonedDateTime?
)
