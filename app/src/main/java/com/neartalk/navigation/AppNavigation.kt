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
import com.neartalk.ui.screens.ModeSelectionScreen
import com.neartalk.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    navController: NavHostController,
    onMakeDiscoverable: () -> Unit
) {
    val chatViewModel: ChatViewModel = hiltViewModel()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    DevicesScreen(
                        onConnect = { deviceMac, _ ->
                            chatViewModel.connectToDevice(deviceMac)

                            scope.launch { drawerState.close() }
                        },
                        onCloseDrawer = {
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                NavHost(navController = navController, startDestination = "hub") {
                    composable("hub") {
                        ModeSelectionScreen(
                            viewModel = chatViewModel,
                            onOpenBroadcast = {
                                navController.navigate("chat_broadcast")
                            },
                            onOpenChat = { peerId ->
                                val key = java.util.UUID.randomUUID().toString()
                                navController.navigate("chat_private/$peerId/$key")
                            },
                            onOpenSettings = {
                                scope.launch { drawerState.open() }
                            },
                            onMakeDiscoverable = onMakeDiscoverable
                        )
                    }

                    composable("chat_broadcast") {
                        LaunchedEffect(Unit) {
                            chatViewModel.setChatMode(ChatViewModel.ChatMode.BROADCAST)
                        }

                        ChatScreen(
                            receiverId = "ALL",
                            isPrivate = false,
                            onBack = {
                                navController.popBackStack()
                            },
                            onOpenDrawer = {
                                scope.launch { drawerState.open() }
                            },
                            viewModel = chatViewModel
                        )
                    }

                    composable(
                        route = "chat_private/{peerId}/{key}",
                        arguments = listOf(
                            navArgument("peerId") { type = NavType.StringType },
                            navArgument("key") { type = NavType.StringType }
                        )
                    ) { entry ->
                        val peerId = entry.arguments?.getString("peerId") ?: ""

                        LaunchedEffect(peerId) {
                            val peer = chatViewModel.availablePeers.value.find { it.id == peerId }
                            if (peer != null) {
                                chatViewModel.selectPeer(peer)
                            }
                        }

                        ChatScreen(
                            receiverId = peerId,
                            isPrivate = true,
                            onBack = {
                                navController.popBackStack()
                            },
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