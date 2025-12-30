package com.ltcn272.finny.presentation.features.bank_notification

import com.ltcn272.finny.domain.model.MessageCard
import java.time.LocalDateTime
import java.util.UUID

data class PendingBankNotification(
    val id: String = UUID.randomUUID().toString(),
    val appName: String,
    val title: String,
    val text: String,
    val originalString: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val isLoading: Boolean = false,
    val cardResult: MessageCard? = null
)
