package com.ltcn272.finny.presentation.common.util

import kotlinx.coroutines.flow.Flow

enum class NetworkStatus {
    Available, Unavailable, Losing, Lost
}

interface ConnectivityObserver {
    fun observe(): Flow<NetworkStatus>
}
