package com.ltcn272.finny.core.navigation

// ...

sealed class AppRoute(val route: String) {

    // Flow khởi đầu (Slides)
    data object Intro : AppRoute("intro_flow")

    // Màn hình Login/Register (Tách biệt khỏi Slides)
    data object Auth : AppRoute("auth_screen")

    // Flow chính (Dashboard và Bottom Nav)
    data object MainWrapper : AppRoute("main_wrapper")

    // Các route chi tiết khác...
    data object TransactionDetail : AppRoute("txn_detail/{id}") {
        fun createRoute(id: String) = "txn_detail/$id"
    }
    // ...
}