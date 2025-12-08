package com.ltcn272.finny.data.mapper

import com.ltcn272.finny.data.remote.dto.UserDto
import com.ltcn272.finny.domain.model.User
import java.text.SimpleDateFormat
import java.util.Locale

fun UserDto.toDomain(): User {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    val outputFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
    val date = try {
        inputFormat.parse(createdAt)
    } catch (e: Exception) {
        null
    }
    val formattedDate = date?.let { outputFormat.format(it) } ?: "Unknown"

    return User(
        id = id,
        displayName = displayName,
        email = email,
        avatar = avatar,
        plan = plan.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
        memberSince = formattedDate
    )
}
