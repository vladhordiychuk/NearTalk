package com.neartalk.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.neartalk.ui.theme.*
import com.neartalk.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    onBack: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val chats by viewModel.chats
    val searchQuery by viewModel.searchQuery
    val selectedTab by viewModel.selectedTab

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Contacts",
                        color = PrimaryText,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    TextButton(onClick = { /* дія */ }) {
                        Text(
                            text = "Sort",
                            color = Primary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {  }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Chat",
                            tint = Primary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Surface,
                    titleContentColor = PrimaryText,
                    actionIconContentColor = Primary,
                    navigationIconContentColor = Primary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Primary,
                contentColor = OnPrimary,
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.selectedTab.value = 0 },
                    icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Contacts") },
                    label = { Text("Contacts") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OnPrimary,
                        unselectedIconColor = OnPrimary.copy(alpha = 0.6f),
                        selectedTextColor = OnPrimary,
                        unselectedTextColor = OnPrimary.copy(alpha = 0.6f),
                        indicatorColor = Accent.copy(alpha = 0.2f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.selectedTab.value = 1
                                onNavigateToHome()
                    },
                    icon = { Icon(Icons.Default.Chat, contentDescription = "Chats") },
                    label = { Text("Chats") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OnPrimary,
                        unselectedIconColor = OnPrimary.copy(alpha = 0.6f),
                        selectedTextColor = OnPrimary,
                        unselectedTextColor = OnPrimary.copy(alpha = 0.6f),
                        indicatorColor = Accent.copy(alpha = 0.2f)
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.selectedTab.value = 2
                                onNavigateToSettings()
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = OnPrimary,
                        unselectedIconColor = OnPrimary.copy(alpha = 0.6f),
                        selectedTextColor = OnPrimary,
                        unselectedTextColor = OnPrimary.copy(alpha = 0.6f),
                        indicatorColor = Accent.copy(alpha = 0.2f)
                    )
                )
            }
        }
    ) { innerPadding ->
        Text(
            text = "",
            modifier = Modifier.padding(innerPadding),
            color = PrimaryText,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
