package com.ltcn272.finny.core.navigation

import androidx.compose.ui.geometry.isEmpty
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.text.format

object Graph {
    const val ROOT = "root"
    const val ONBOARD = "onboarding"
    const val AUTH = "auth"
    const val MAIN = "main"
}

object NavArgs {
    const val TRANSACTION_ID = "transactionId"
    const val BUDGET_ID = "budgetId"
    const val DATE = "date"
}

object MainRoute {
    const val HOME = "home"
    const val TRANSACTION = "transaction"
    const val CHAT = "chat"
    const val DASHBOARD = "dashboard"
    const val PROFILE = "profile"
    const val NOTIFICATION = "notification"

    const val BUDGET_DETAIL = "budgetDetail"
    const val CREATE_EDIT_BUDGET = "createEditBudget"
    private const val CREATE_TRANSACTION_BASE = "createTransaction"
    const val CREATE_TRANSACTION =
        "$CREATE_TRANSACTION_BASE?${NavArgs.TRANSACTION_ID}={${NavArgs.TRANSACTION_ID}}&${NavArgs.BUDGET_ID}={${NavArgs.BUDGET_ID}}&${NavArgs.DATE}={${NavArgs.DATE}}"

    fun createTransactionUrl(
        transactionId: String? = null,
        budgetId: String? = null,
        date: LocalDateTime? = null
    ): String {
        val route = CREATE_TRANSACTION_BASE
        val args = mutableListOf<String>()
        transactionId?.let { args.add("${NavArgs.TRANSACTION_ID}=$it") }
        budgetId?.let { args.add("${NavArgs.BUDGET_ID}=$it") }
        date?.let { args.add("${NavArgs.DATE}=$it") }
        return if (args.isEmpty()) route else "$route?${args.joinToString("&")}"
    }

    private const val CREATE_BUDGET_BASE = "createBudget"

    const val CREATE_EDIT_TRANSACTION_BASE = "createEditTransaction"
    const val CREATE_EDIT_TRANSACTION =
        "$CREATE_EDIT_TRANSACTION_BASE?${NavArgs.TRANSACTION_ID}={${NavArgs.TRANSACTION_ID}}&${NavArgs.BUDGET_ID}={${NavArgs.BUDGET_ID}}&${NavArgs.DATE}={${NavArgs.DATE}}"

    fun createEditTransactionUrl(
        transactionId: String? = null,
        budgetId: String? = null,
        date: LocalDate? = null
    ): String {
        val route = CREATE_EDIT_TRANSACTION_BASE
        val args = mutableListOf<String>()
        transactionId?.let { args.add("${NavArgs.TRANSACTION_ID}=$it") }
        budgetId?.let { args.add("${NavArgs.BUDGET_ID}=$it") }
        date?.let { args.add("${NavArgs.DATE}=${it.format(DateTimeFormatter.ISO_LOCAL_DATE)}") }
        return if (args.isEmpty()) route else "$route?${args.joinToString("&")}"
    }

    const val LIST_BUDGET = "listBudget"
    const val SETTINGS = "settings"
}

val BottomRoutes = setOf(
    MainRoute.HOME, MainRoute.TRANSACTION, MainRoute.DASHBOARD, MainRoute.SETTINGS
)
