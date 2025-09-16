package com.neartalk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.neartalk.models.Chat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.text.style.TextOverflow
import com.neartalk.ui.theme.*
import androidx.compose.ui.graphics.Color
import com.neartalk.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.draw.rotate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToChat: (Int) -> Unit,
    onNavigateToProfile: () -> Unit,
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
                        text = "Chats",
                        color = PrimaryText,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    TextButton(onClick = { /* дія */ }) {
                        Text(
                            text = "Edit",
                            color = Primary,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToProfile() }) {
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
                    onClick = { viewModel.selectedTab.value = 1 },
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
                    onClick = { viewModel.selectedTab.value = 2 },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.filterChats(it) },
                placeholder = { Text("Search", color = SecondaryText) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = SecondaryText
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Surface,
                    unfocusedContainerColor = Surface,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Primary
                ),
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.filterChats("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = SecondaryText)
                        }
                    }
                }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(chats) { chat ->
                    ChatRow(chat = chat, onClick = { onNavigateToChat(chat.id) })
                    Divider(color = Surface, thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
fun ChatRow(chat: Chat, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (chat.isPinned) Surface else Background)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Avatar",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            tint = Primary
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(chat.name, style = MaterialTheme.typography.bodyLarge, color = PrimaryText)
            }

            Text(
                chat.lastMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault())
                    .format(Date(chat.time * 1000))

                if (chat.isSentByMe) {
                    if (chat.isRead) {
                        Box {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Read",
                                tint = Primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Read",
                                tint = Primary,
                                modifier = Modifier
                                    .size(14.dp)
                                    .offset(x = 4.dp)
                            )
                        }
                    } else {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Sent",
                            tint = SecondaryText,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }


                Spacer(modifier = Modifier.width(4.dp))

                Text(formattedTime, style = MaterialTheme.typography.bodySmall, color = SecondaryText)
            }

            if (chat.isPinned) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = "Pinned",
                    tint = Primary,
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(45f)
                )
            }
        }
    }
}
