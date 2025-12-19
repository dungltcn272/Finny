package com.ltcn272.finny.data.mapper

import com.ltcn272.finny.data.remote.dto.AuthUserDto
import com.ltcn272.finny.data.remote.dto.ProfileUserDto
import com.ltcn272.finny.domain.model.User
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun AuthUserDto.toUserDomain(): User {
    return User(
        id = this.id,
        displayName = this.displayName,
        email = this.email,
        avatar = this.avatar,
        plan = this.plan,
        isActive = this.isActive,
        currency = null,
        isAdmin = false,
        lastLogin = null
    )
}

fun ProfileUserDto.toUserDomain(): User {
    return User(
        id = this.id,
        displayName = this.displayName,
        email = this.email,
        avatar = this.avatar,
        plan = this.plan,
        isActive = this.isActive,
        currency = this.currency,
        isAdmin = this.isAdmin,
        lastLogin = this.lastLogin?.let {
            ZonedDateTime.parse(
                it,
                DateTimeFormatter.ISO_ZONED_DATE_TIME
            )
        }
    )
}

fun User.toUpdateMap(): Map<String, Any?> {
    return mapOf(
        "display_name" to this.displayName,
        "avatar" to this.avatar,
        "currency" to this.currency
    )
}
