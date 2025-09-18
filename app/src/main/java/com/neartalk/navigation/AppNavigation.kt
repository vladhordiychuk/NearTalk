package com.neartalk.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.neartalk.ui.screens.ChatScreen
import com.neartalk.ui.screens.ContactsScreen
import com.neartalk.ui.screens.FilesScreen
import com.neartalk.ui.screens.HomeScreen
import com.neartalk.ui.screens.LoginScreen
import com.neartalk.ui.screens.ProfileScreen
import com.neartalk.ui.screens.SettingsScreen
import com.neartalk.viewmodel.HomeViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AppNavigation(navController: NavHostController) {
    val viewModel: HomeViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            viewModel.selectedTab.value = 1
            HomeScreen(
                onNavigateToChat = { receiverId ->
                    navController.navigate("chat/0/$receiverId")
                },
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToContacts = { navController.navigate("contacts") },
                onNavigateToSettings = { navController.navigate("settings") },
                viewModel = viewModel
            )
        }
        composable(
            route = "chat/{userId}/{receiverId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.IntType },
                navArgument("receiverId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            val receiverId = backStackEntry.arguments?.getInt("receiverId") ?: 0
            ChatScreen(
                userId = userId,
                receiverId = receiverId,
                onBack = { navController.popBackStack() },
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToFiles = { navController.navigate("files") }
            )
        }
        composable("profile") {
            ProfileScreen(onBack = { navController.popBackStack() })
        }
        composable("login") {
            LoginScreen(onBack = { navController.popBackStack() })
        }
        composable("files") {
            FilesScreen(onBack = { navController.popBackStack() })
        }
        composable("contacts") {
            viewModel.selectedTab.value = 0
            ContactsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToSettings = { navController.navigate("settings") },
                viewModel = viewModel
            )
        }
        composable("settings") {
            viewModel.selectedTab.value = 2
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onNavigateToContacts = { navController.navigate("contacts") },
                onNavigateToHome = { navController.navigate("home") },
                viewModel = viewModel
            )
        }
    }
}