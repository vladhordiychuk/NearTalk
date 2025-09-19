package com.neartalk.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.neartalk.domain.model.Chat
import com.neartalk.ui.theme.*
import com.neartalk.viewmodel.HomeViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.animateColorAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToChat: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToContacts: () -> Unit,
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
                        text = "Chats",
                        color = PrimaryText,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    TextButton(onClick = { /* TODO: Додати дію для редагування */ }) {
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
                contentColor = OnPrimary
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = {
                        viewModel.selectedTab.value = 0
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
                    onClick = {
                        viewModel.selectedTab.value = 2
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
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = SecondaryText
                            )
                        }
                    }
                }
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(chats, key = { it.id }) { chat ->
                    var visible by rememberSaveable { mutableStateOf(true) }

                    AnimatedVisibility(
                        visible = visible,
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        ChatRow(
                            chat = chat,
                            onClick = {
                                chat.participantId?.let { participantId ->
                                    println("DEBUG: Navigating to chat with participantId: $participantId")
                                    onNavigateToChat(participantId)
                                } ?: println("DEBUG: participantId is null for chat: ${chat.id}")
                            },
                            onDelete = {
                                visible = false
                                viewModel.deleteChat(it)
                            },
                            onPin = { viewModel.togglePinChat(it) },
                            onMute = { viewModel.toggleMuteChat(it) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRow(
    chat: Chat,
    onClick: () -> Unit,
    onDelete: (Chat) -> Unit,
    onPin: (Chat) -> Unit,
    onMute: (Chat) -> Unit
) {
    val swipeState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete(chat)
                    true
                }
                else -> false
            }
        }
    )

    SwipeToDismissBox(
        state = swipeState,
        backgroundContent = {
            val progress by animateFloatAsState(
                targetValue = swipeState.progress,
                label = "swipeProgress"
            )
            val backgroundColor by animateColorAsState(
                targetValue = when {
                    swipeState.targetValue == SwipeToDismissBoxValue.EndToStart -> Color.Red
                    progress > 0.6f -> Color.Gray.copy(alpha = 0.9f)
                    else -> Background
                },
                label = "backgroundColor"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (progress < 0.8f) {
                        IconButton(
                            onClick = { onMute(chat) },
                            modifier = Modifier
                                .size(48.dp)
                                .background(Surface, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (chat.isMuted) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                                contentDescription = if (chat.isMuted) "Unmute" else "Mute",
                                tint = Primary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { onPin(chat) },
                            modifier = Modifier
                                .size(48.dp)
                                .background(Surface, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = if (chat.isPinned) "Unpin" else "Pin",
                                tint = Primary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    IconButton(
                        onClick = { onDelete(chat) },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.Red, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color.White
                        )
                    }
                }
            }
        },
        content = {
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = chat.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = PrimaryText
                    )
                    Text(
                        text = chat.lastMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Top
                ) {
                    val formattedTime = SimpleDateFormat("HH:mm", Locale.getDefault())
                        .format(Date(chat.time * 1000))
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                    if (chat.unreadCount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .background(Primary, shape = CircleShape)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = chat.unreadCount.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }
                    if (chat.isPinned) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = Primary,
                            modifier = Modifier.size(16.dp).rotate(45f)
                        )
                    }
                }
            }
        }
    )
}