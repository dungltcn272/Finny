package com.ltcn272.finny.core.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.facebook.CallbackManager
import com.ltcn272.finny.core.OnboardingManager
import com.ltcn272.finny.data.mapper.toTransactionDomain
import com.ltcn272.finny.domain.model.Budget
import com.ltcn272.finny.domain.model.Transaction
import com.ltcn272.finny.presentation.common.ui.GradientFloatingActionButton
import com.ltcn272.finny.presentation.features.MainBottomBar
import com.ltcn272.finny.presentation.features.auth.AuthScreen
import com.ltcn272.finny.presentation.features.budget.budget_detail.BudgetDetailScreen
import com.ltcn272.finny.presentation.features.budget.budget_list.BudgetListScreen
import com.ltcn272.finny.presentation.features.budget.create_edit.CreateEditBudgetScreen
import com.ltcn272.finny.presentation.features.category.CategoryScreen
import com.ltcn272.finny.presentation.features.chat.ChatScreen
import com.ltcn272.finny.presentation.features.dashboard.DashboardScreen
import com.ltcn272.finny.presentation.features.home.HomeScreen
import com.ltcn272.finny.presentation.features.intro.IntroScreen
import com.ltcn272.finny.presentation.features.notification.NotificationScreen
import com.ltcn272.finny.presentation.features.profile.ProfileScreen
import com.ltcn272.finny.presentation.features.setting.SettingScreen
import com.ltcn272.finny.presentation.features.transaction.create_edit.CreateEditTransactionScreen
import com.ltcn272.finny.presentation.features.transaction.recurring_transaction.RecurringTransactionScreen
import com.ltcn272.finny.presentation.features.transaction.transaction_list.TransactionScreen
import java.time.LocalDate

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

    val showBottomBarAndFab = currentTab != null

    Box(Modifier.fillMaxSize()) {
        NavHost(
            navController = nav,
            startDestination = startRoute,
            route = Graph.ROOT,
            modifier = Modifier.fillMaxSize()
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
                        onNotificationClick = { nav.navigate(MainRoute.NOTIFICATION) },
                        onBudgetClick = { budget ->
                            nav.currentBackStackEntry?.savedStateHandle?.set("budget_arg", budget)
                            nav.navigate(MainRoute.BUDGET_DETAIL)
                        },
                        onTransactionClick = { transaction ->
                            nav.currentBackStackEntry?.savedStateHandle?.set(
                                "transaction_to_edit",
                                transaction
                            )
                            nav.navigate(MainRoute.CREATE_EDIT_TRANSACTION)
                        },
                        onSeeAllBudgets = {
                            nav.navigate(MainRoute.LIST_BUDGET)
                        },
                        onSeeAllTransactions = {
                            nav.navigate(MainRoute.TRANSACTION) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(Graph.MAIN) { saveState = true }
                            }
                        }
                    )
                }

                composable(MainRoute.TRANSACTION) {
                    TransactionScreen(
                        onTransactionClick = { transaction ->
                            nav.currentBackStackEntry?.savedStateHandle?.set(
                                "transaction_to_edit",
                                transaction
                            )
                            nav.navigate(MainRoute.CREATE_EDIT_TRANSACTION)
                        },
                        onAddTransactionWithDate = { date ->
                            nav.currentBackStackEntry?.savedStateHandle?.set(
                                NavArgs.DATE,
                                date
                            )
                            nav.navigate(MainRoute.CREATE_EDIT_TRANSACTION)
                        },
                        onNavigateToBudgetSettings = { nav.navigate(MainRoute.LIST_BUDGET) },
                        onNavigateToRecurring = { nav.navigate(MainRoute.LIST_RECURRING_TRANSACTION) }
                    )
                }

                composable(MainRoute.DASHBOARD) {
                    DashboardScreen()
                }

                composable(MainRoute.SETTINGS) {
                    SettingScreen(onLoggedOut = {
                        nav.navigate(Graph.AUTH) {
                            popUpTo(Graph.MAIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }, onNavigateToProfile = {
                        nav.navigate(MainRoute.PROFILE)
                    }, onNavigateToCategories = {
                        nav.navigate(MainRoute.LIST_CATEGORY)
                    })
                }

                composable(MainRoute.CHAT) {
                    ChatScreen(onBack = {
                        nav.popBackStack()
                    })
                }

                // Các composable khác
                composable(MainRoute.NOTIFICATION) { NotificationScreen(onBack = { nav.popBackStack() }) }
                composable(MainRoute.LIST_BUDGET) {
                    BudgetListScreen(
                        onBack = { nav.popBackStack() },
                        onCreateNew = { nav.navigate(MainRoute.CREATE_EDIT_BUDGET) },
                        onBudgetClick = { budget ->
                            nav.currentBackStackEntry?.savedStateHandle?.set(
                                "budget_arg",
                                budget
                            ); nav.navigate(MainRoute.BUDGET_DETAIL)
                        })
                }
                composable(MainRoute.LIST_RECURRING_TRANSACTION) {
                    RecurringTransactionScreen(
                        onBack = { nav.popBackStack() },
                        onTransactionClick = { recurringTransaction ->
                            val transactionToEdit = recurringTransaction.toTransactionDomain()
                            nav.currentBackStackEntry?.savedStateHandle?.set(
                                "transaction_to_edit",
                                transactionToEdit
                            )
                            nav.navigate(MainRoute.CREATE_EDIT_TRANSACTION)
                        }, onCreateNew = {
                            nav.navigate(MainRoute.CREATE_EDIT_TRANSACTION)
                        }
                    )
                }
                composable(route = MainRoute.CREATE_EDIT_BUDGET) {
                    val budgetToEdit =
                        nav.previousBackStackEntry?.savedStateHandle?.get<Budget>("budget_to_edit")
                    CreateEditBudgetScreen(
                        budgetToEdit = budgetToEdit,
                        onBack = { nav.popBackStack() })
                    LaunchedEffect(Unit) {
                        nav.previousBackStackEntry?.savedStateHandle?.remove<Budget>(
                            "budget_to_edit"
                        )
                    }
                }
                composable(route = MainRoute.CREATE_EDIT_TRANSACTION) { backStackEntry ->
                    val previousHandle = nav.previousBackStackEntry?.savedStateHandle
                    val transactionToEdit = previousHandle?.get<Transaction>("transaction_to_edit")

                    val predefinedDate = previousHandle?.get<LocalDate>(NavArgs.DATE)

                    val predefinedBudgetId: String? =
                        backStackEntry.arguments?.getString(NavArgs.BUDGET_ID)

                    CreateEditTransactionScreen(
                        transactionToEdit = transactionToEdit,
                        predefinedBudgetId = predefinedBudgetId,
                        predefinedDate = predefinedDate,
                        onBack = { nav.popBackStack() }
                    )

                    DisposableEffect(Unit) {
                        onDispose {
                            previousHandle?.remove<Transaction>("transaction_to_edit")
                            previousHandle?.remove<LocalDate>(NavArgs.DATE)
                        }
                    }
                }
                composable(route = MainRoute.BUDGET_DETAIL) {
                    val budget =
                        nav.previousBackStackEntry?.savedStateHandle?.get<Budget>("budget_arg")
                    if (budget != null)
                        BudgetDetailScreen(
                            budget = budget,
                            onBack = { nav.popBackStack() },
                            onTransactionClick = { transaction ->
                                nav.currentBackStackEntry?.savedStateHandle?.set(
                                    "transaction_arg",
                                    transaction
                                )
                            },
                            onEditBudget = { budgetToEdit ->
                                nav.currentBackStackEntry?.savedStateHandle?.set(
                                    "budget_to_edit",
                                    budgetToEdit
                                )
                                nav.navigate(MainRoute.CREATE_EDIT_BUDGET)
                            }
                        )
                }
                composable(MainRoute.PROFILE) {
                    ProfileScreen(onBackClick = { nav.popBackStack() })
                }
                composable(MainRoute.LIST_CATEGORY) {
                    CategoryScreen(onBack = { nav.popBackStack() })
                }
            }
        }

        AnimatedVisibility(
            visible = showBottomBarAndFab,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            MainBottomBar(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 12.dp),
                selectedRoute = currentTab ?: MainRoute.HOME,
                onSelect = { route ->
                    if (route == MainRoute.CHAT) {
                        nav.navigate(route)
                    } else {
                        nav.navigate(route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(Graph.MAIN) { saveState = true }
                        }
                    }
                }
            )
        }

        AnimatedVisibility(
            visible = showBottomBarAndFab,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(bottom = 90.dp, end = 16.dp),
            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
        ) {
            GradientFloatingActionButton(
                onClick = { nav.navigate(MainRoute.CREATE_EDIT_TRANSACTION) }
            )
        }
    }
}
