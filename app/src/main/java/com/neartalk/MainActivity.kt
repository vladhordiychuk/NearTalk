package com.neartalk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.neartalk.ui.theme.NearTalkTheme
import androidx.compose.runtime.Composable
import com.neartalk.ui.screens.ChatScreen
import com.neartalk.ui.screens.HomeScreen
import com.neartalk.ui.screens.LoginScreen
import com.neartalk.ui.screens.ProfileScreen
import com.neartalk.ui.screens.FilesScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NearTalkTheme {
                val navController = rememberNavController()
                AppNavigation(navController)
            }
        }
    }
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNavigateToChat = { navController.navigate("chat") },
                onNavigateToProfile = { navController.navigate("profile") },
            )
        }
        composable("chat") {
            ChatScreen(
                onBack = { navController.popBackStack() },
                onNavigateToProfile = { navController.navigate("profile") },
                onNavigateToFiles = { navController.navigate("files") }
            )
        }
        composable("profile") {
            ProfileScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("login") {
            LoginScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("files") {
            FilesScreen(
                onBack = { navController.popBackStack()}
            )
        }
    }
}

