package com.neartalk.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import com.neartalk.ui.theme.Accent
import com.neartalk.ui.theme.OnPrimary
import com.neartalk.ui.theme.Primary
import com.neartalk.ui.theme.PrimaryText
import com.neartalk.ui.theme.Surface
import com.neartalk.viewmodel.HomeViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val selectedTab by viewModel.selectedTab

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "",
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
                    onClick = { viewModel.selectedTab.value = 0
                                onNavigateToContacts()
                    },
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