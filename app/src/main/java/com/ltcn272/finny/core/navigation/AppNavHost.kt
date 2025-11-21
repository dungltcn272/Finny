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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.facebook.CallbackManager
import com.ltcn272.finny.core.Gate
import com.ltcn272.finny.presentation.features.MainBottomBar
import com.ltcn272.finny.presentation.features.auth.AuthScreen
import com.ltcn272.finny.presentation.features.budget.budget_detail.BudgetDetailScreen
import com.ltcn272.finny.presentation.features.budget.budget_list.ListBudgetScreen
import com.ltcn272.finny.presentation.features.budget.create_budget.CreateBudgetScreen
import com.ltcn272.finny.presentation.features.home.HomeScreen
import com.ltcn272.finny.presentation.features.intro.IntroScreen
import com.ltcn272.finny.presentation.features.setting.SettingScreen
import com.ltcn272.finny.presentation.features.transation.create_transaction.CreateTransactionScreen
import com.ltcn272.finny.presentation.features.transation.transaction_list.TransactionListScreen


@Composable
fun AppNav(startRoute: String, callbackManager: CallbackManager) {
    val nav = rememberNavController()
    val context = LocalContext.current

    // Route hiện tại
    val entry by nav.currentBackStackEntryAsState()
    val routeSet = entry?.destination?.hierarchy?.mapNotNull { it.route }?.toSet().orEmpty()
    val currentTab = routeSet.firstOrNull { it in BottomRoutes }
    val showBottomBar = currentTab != null

    Box(Modifier.fillMaxSize()) {
        // CONTENT
        NavHost(
            navController = nav,
            startDestination = startRoute,
            route = Graph.ROOT,
            modifier = Modifier
                .fillMaxSize()
        ) {
            // ===== Onboarding =====
            composable(Graph.ONBOARD) {
                IntroScreen(
                    onBoardSeen = { Gate.markOnboardingSeen(context) },
                    onGetStartedClick = {
                        nav.navigate(Graph.AUTH) {
                            popUpTo(Graph.ONBOARD) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            // ===== Auth =====
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

            // ===== Main (đã đăng nhập) =====
            navigation(startDestination = MainRoute.HOME, route = Graph.MAIN) {
                composable(MainRoute.HOME) {
                    HomeScreen()
                }
                composable(MainRoute.TRANSACTION) {
                    TransactionListScreen(
                        onCreateTransactionClick = {
                            nav.navigate(MainRoute.createTransactionUrl())
                        },
                        onTransactionClick = { transactionId ->
                            nav.navigate(MainRoute.createTransactionUrl(transactionId = transactionId))
                        },
                        onOpenListBudget = { nav.navigate(MainRoute.LIST_BUDGET) }
                    )
                }
                composable(MainRoute.CHAT) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text("Chat")
                    }
                }
                composable(MainRoute.CHALLENGE) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Text("Challenge")
                    }
                }
                composable(MainRoute.LIST_BUDGET) {
                    ListBudgetScreen(
                        onBack = { nav.popBackStack() },
                        onCreateNew = { nav.navigate(MainRoute.createBudgetUrl()) },
                        onBudgetClick = { budgetId ->
                            nav.navigate(MainRoute.BUDGET_DETAIL + "/$budgetId")
                        }
                    )
                }

                composable(
                    route = MainRoute.CREATE_BUDGET,
                    arguments = listOf(
                        navArgument(NavArgs.BUDGET_ID) {
                            type = NavType.StringType
                            nullable = true
                        }
                    )
                ) {
                    CreateBudgetScreen(
                        onBack = { nav.popBackStack() }
                    )
                }

                // Không hiện bottom bar:
                composable(
                    route = MainRoute.TRANSACTION_DETAIL,
                    arguments = listOf(navArgument("id") { type = NavType.StringType })
                ) {
//                    TransactionDetailScreen(
//                        sampleTransactions().first(),
//                        onBack = { nav.popBackStack() }
//                    )
                }
                composable(
                    route = MainRoute.BUDGET_DETAIL + "/{budgetId}",
                    arguments = listOf(navArgument("budgetId") { type = NavType.StringType })
                ) { backStackEntry ->
                    BudgetDetailScreen(
                        onBack = { nav.popBackStack() },
                        onEditClick = {
                            val budgetId = backStackEntry.arguments?.getString("budgetId")
                            nav.navigate(MainRoute.createBudgetUrl(budgetId = budgetId))
                        },
                        onTransactionClick = { transactionId ->
                            nav.navigate(MainRoute.createTransactionUrl(transactionId = transactionId))
                        },
                        onAddTransactionClick = {
                            val budgetId = backStackEntry.arguments?.getString("budgetId")
                            nav.navigate(MainRoute.createTransactionUrl(budgetId = budgetId))
                        }
                    )
                }
                composable(
                    route = MainRoute.CREATE_TRANSACTION,
                    arguments = listOf(
                        navArgument(NavArgs.TRANSACTION_ID) {
                            type = NavType.StringType
                            nullable = true
                        },
                        navArgument(NavArgs.BUDGET_ID) {
                            type = NavType.StringType
                            nullable = true
                        },
                        navArgument(NavArgs.DATE) {
                            type = NavType.StringType
                            nullable = true
                        }
                    )
                ) {
                    CreateTransactionScreen(
                        onBack = { nav.popBackStack() },
                        onTransactionCreated = {
                            nav.previousBackStackEntry?.savedStateHandle?.set(
                                "transaction_created",
                                true
                            )
                            nav.popBackStack()
                        }
                    )
                }
                composable(MainRoute.SETTINGS) {
                    SettingScreen()
                }
            }
        }

        // BOTTOM NAV OVERLAY
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
