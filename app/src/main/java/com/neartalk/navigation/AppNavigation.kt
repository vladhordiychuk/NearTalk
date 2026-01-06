package com.neartalk.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.neartalk.ui.screens.ChatScreen
import com.neartalk.ui.screens.DevicesScreen
import com.neartalk.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    navController: NavHostController,
    onMakeDiscoverable: () -> Unit,
    myAddress: String
) {
    val chatViewModel: ChatViewModel = hiltViewModel()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val isConnected by chatViewModel.isConnected.collectAsState(initial = false)

    LaunchedEffect(isConnected) {
        if (isConnected && drawerState.isOpen) {
            drawerState.close()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    DevicesScreen(
                        onConnect = { deviceId, deviceName ->
                            scope.launch { drawerState.close() }
                            navController.navigate("chat/$myAddress/$deviceId") {
                                popUpTo("chat/$myAddress/") { inclusive = false }
                            }
                        },
                        onCloseDrawer = {
                            scope.launch { drawerState.close() }
                        },
                        onMakeDiscoverable = onMakeDiscoverable
                    )
                }
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                NavHost(
                    navController = navController,
                    startDestination = "chat/$myAddress/"
                ) {
                    composable(
                        route = "chat/{userId}/{receiverId}",
                        arguments = listOf(
                            navArgument("userId") { type = NavType.StringType },
                            navArgument("receiverId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val receiverId = backStackEntry.arguments?.getString("receiverId") ?: ""

                        ChatScreen(
                            receiverId = receiverId,
                            onBack = {
                                chatViewModel.disconnect()
                                navController.popBackStack()
                            },
                            onOpenDrawer = {
                                scope.launch { drawerState.open() }
                            },
                            viewModel = chatViewModel
                        )
                    }

                    composable(
                        route = "chat/{userId}/",
                        arguments = listOf(
                            navArgument("userId") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        LaunchedEffect(Unit) {
                            chatViewModel.startSession()
                        }

                        ChatScreen(
                            receiverId = "",
                            onBack = { },
                            onOpenDrawer = {
                                scope.launch { drawerState.open() }
                            },
                            viewModel = chatViewModel
                        )
                    }
                }
            }
        }
    }
}