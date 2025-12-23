package com.ltcn272.finny.domain.model

import java.time.LocalDate

data class DaySection(
    val date: LocalDate,
    val transactions: List<Transaction>,
    val totalAmount: Double
)