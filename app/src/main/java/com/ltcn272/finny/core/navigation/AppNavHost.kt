package com.ltcn272.finny.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.facebook.CallbackManager
import com.ltcn272.finny.R
import com.ltcn272.finny.core.OnboardingManager
// Thêm import cho các model
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.presentation.features.MainBottomBar
import com.ltcn272.finny.presentation.features.auth.AuthScreen

import com.ltcn272.finny.presentation.features.home.HomeScreen
import com.ltcn272.finny.presentation.features.intro.IntroScreen

import com.ltcn272.finny.presentation.features.transation.transaction_list.TransactionScreen


@Composable
fun AppNav(
    startRoute: String,
    callbackManager: CallbackManager,
    onboardingManager: OnboardingManager
) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val routeSet = entry?.destination?.hierarchy?.mapNotNull { it.route }?.toSet().orEmpty()
    val currentTab = routeSet.firstOrNull { it in BottomRoutes }
    val showBottomBar = currentTab != null

    Box(Modifier.fillMaxSize()) {
        NavHost(
            navController = nav,
            startDestination = startRoute,
            route = Graph.ROOT,
            modifier = Modifier
                .fillMaxSize()
        ) {
            composable(Graph.ONBOARD) {
                IntroScreen(
                    onBoardSeen = { onboardingManager.markOnboardingSeen() },
                    onGetStartedClick = {
                        nav.navigate(Graph.AUTH) {
                            popUpTo(Graph.ONBOARD) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable(Graph.AUTH) {
                AuthScreen(
                    onLoggedIn = {
                        nav.navigate(Graph.MAIN) {
                            popUpTo(Graph.AUTH) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    callbackManager = callbackManager
                )
            }

            navigation(startDestination = MainRoute.HOME, route = Graph.MAIN) {
                composable(MainRoute.HOME) {
                    HomeScreen(
                        onNotificationClick = {
                            // Giữ nguyên các lambda khác
                            // nav.navigate(MainRoute.NOTIFICATION)
                        },
                        onBudgetClick = { budget ->
                            nav.currentBackStackEntry?.savedStateHandle?.set("budget", budget)
                            nav.navigate(MainRoute.BUDGET_DETAIL)
                        },
                        onTransactionClick = { transaction ->
                            nav.currentBackStackEntry?.savedStateHandle?.set("transaction", transaction)
                            nav.navigate(MainRoute.TRANSACTION_DETAIL)
                        }
                    )
                }

                composable(MainRoute.TRANSACTION) {
                    TransactionScreen()
                }

                // --- ĐỊNH NGHĨA LẠI COMPOSABLE CHO MÀN HÌNH DETAIL ---

//                composable(route = MainRoute.BUDGET_DETAIL) {
//                    val budget = nav.previousBackStackEntry?.savedStateHandle?.get<Budget>("budget")
//                    if (budget != null) {
//                        BudgetDetailScreen(
//                            budget = budget,
//                            onBack = { nav.popBackStack() }
//                        )
//                    } else {
//                        // Xử lý trường hợp không tìm thấy budget (ví dụ: quay lại)
//                        nav.popBackStack()
//                    }
//                }

//                composable(route = MainRoute.TRANSACTION_DETAIL) {
//                    val transaction = nav.previousBackStackEntry?.savedStateHandle?.get<Transaction>("transaction")
//                    if (transaction != null) {
//                        TransactionDetailScreen(
//                            transaction = transaction,
//                            onBack = { nav.popBackStack() }
//                            // Các lambda khác của màn hình detail
//                        )
//                    } else {
//                        nav.popBackStack()
//                    }
//                }

//                // Các route khác giữ nguyên...
//                composable(MainRoute.PROFILE) {
//                    ProfileScreen(onBackClick = { nav.popBackStack() })
//                }
            }
        }

        if (showBottomBar) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                MainBottomBar(
                    selectedRoute = currentTab ?: MainRoute.HOME,
                    onSelect = { route ->
                        nav.navigate(route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(Graph.MAIN) { saveState = true }
                        }
                    }
                )
            }
        }
    }
}
