package com.ltcn272.finny.core.navigation

object Graph {
    const val ROOT = "root"
    const val ONBOARD = "onboarding"
    const val AUTH = "auth"
    const val MAIN = "main"
}

object NavArgs {
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


    const val CREATE_EDIT_TRANSACTION = "createEditTransaction"

    const val LIST_BUDGET = "listBudget"
    const val SETTINGS = "settings"
    const val LIST_CATEGORY = "listCategory"
    const val LIST_RECURRING_TRANSACTION = "listRecurringTransaction"
}

val BottomRoutes = setOf(
    MainRoute.HOME, MainRoute.TRANSACTION, MainRoute.DASHBOARD, MainRoute.SETTINGS
)
