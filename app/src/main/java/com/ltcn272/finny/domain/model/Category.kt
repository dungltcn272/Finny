package com.ltcn272.finny.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Category(
    val serverId: String? = null,
    val name: String?,
    val description: String? = null,
    val color: String? = null,
    val isDefault: Boolean
) : Parcelable

