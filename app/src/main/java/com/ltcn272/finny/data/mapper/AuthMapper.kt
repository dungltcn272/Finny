package com.ltcn272.finny.data.mapper

import com.ltcn272.finny.data.remote.dto.AuthDataDto
import com.ltcn272.finny.domain.model.AuthToken

fun AuthDataDto.toAuthToken(): AuthToken {
    return AuthToken(
        accessToken = this.accessToken,
        refreshToken = this.refreshToken
    )
}
