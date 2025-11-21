package com.ltcn272.finny.core.navigation

import java.time.LocalDateTime

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
    const val CHALLENGE = "challenge"
    const val TRANSACTION_DETAIL = "transactionDetail/{id}"
    const val BUDGET_DETAIL = "budgetDetail"

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
        date?.let { args.add("${NavArgs.DATE}=$it") } // LocalDateTime.toString() is ISO-8601
        return if (args.isEmpty()) route else "$route?${args.joinToString("&")}"
    }

    const val CREATE_BUDGET_BASE = "createBudget"

    // 2. Định nghĩa route đầy đủ với argument tùy chọn
    const val CREATE_BUDGET =
        "$CREATE_BUDGET_BASE?${NavArgs.BUDGET_ID}={${NavArgs.BUDGET_ID}}"

    // 3. Tạo hàm helper để xây dựng URL
    fun createBudgetUrl(budgetId: String? = null): String {
        return if (budgetId == null) {
            CREATE_BUDGET_BASE
        } else {
            "$CREATE_BUDGET_BASE?${NavArgs.BUDGET_ID}=$budgetId"
        }
    }

    const val LIST_BUDGET = "listBudget"
    const val SETTINGS = "settings"
}

val BottomRoutes = setOf(
    MainRoute.HOME, MainRoute.TRANSACTION, MainRoute.CHAT, MainRoute.CHALLENGE
)
