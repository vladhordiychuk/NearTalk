package com.neartalk.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neartalk.ui.theme.Primary
import com.neartalk.ui.theme.PrimaryText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Send
import com.neartalk.ui.theme.Online
import com.neartalk.ui.theme.Surface
import com.neartalk.ui.components.NearTalkTextField
import androidx.compose.material.icons.filled.Mic
import com.neartalk.viewmodel.ChatViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import com.neartalk.ui.theme.SecondaryText
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.rememberCoroutineScope
import com.neartalk.domain.model.Message
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    userId: String,
    receiverId: String,
    onBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToFiles: () -> Unit,
    userName: String = "Dima",
    isOnline: Boolean = true,
    viewModel: ChatViewModel = hiltViewModel()
) {
    LaunchedEffect(userId) {
        viewModel.loadMessages(userId)
    }

    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = userName,
                            color = PrimaryText,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (isOnline) "online" else "offline",
                            color = if (isOnline) Online else SecondaryText,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back",
                            tint = Primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile",
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
            Surface(
                color = Surface,
                shadowElevation = 4.dp
            ) {
                ChatInputBar(
                    text = inputText,
                    onTextChange = viewModel::onInputChanged,
                    onNavigateToFiles = onNavigateToFiles,
                    onSent = { viewModel.onMessageSent(userId, receiverId) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            ChatMessages(
                messages = messages,
                userId = userId, // Pass userId to ChatMessages
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ChatInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onNavigateToFiles: () -> Unit,
    onSent: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateToFiles) {
            Icon(Icons.Default.AttachFile, contentDescription = "Attach file", tint = Primary)
        }
        NearTalkTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = "Type a message...",
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )
        if (text.isNotBlank()) {
            IconButton(onClick = onSent) {
                Icon(Icons.Default.Send, contentDescription = "Send message", tint = Primary)
            }
        } else {
            IconButton(onClick = { /* TODO: Implement voice recording */ }) {
                Icon(Icons.Default.Mic, contentDescription = "Record voice", tint = Primary)
            }
        }
    }
}

@Composable
fun ChatMessages(messages: List<Message>, userId: String, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        reverseLayout = true,
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(messages, key = { it.id }) { message ->
            MessageBubble(message = message, userId = userId)
        }
    }
}

@Composable
fun MessageBubble(message: Message, userId: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = if (message.senderId == userId) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (message.senderId == userId) Primary else Surface,
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.text,
                    color = if (message.senderId == userId) SecondaryText else PrimaryText
                )
                Text(
                    text = message.formattedTimestamp(),
                    color = SecondaryText,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}