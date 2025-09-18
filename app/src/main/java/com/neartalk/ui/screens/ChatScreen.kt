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
import androidx.compose.animation.animateContentSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    userId: Int,
    receiverId: Int,
    onBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToFiles: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val user by viewModel.getUser(receiverId).collectAsState(initial = null)
    LaunchedEffect(userId, receiverId) {
        viewModel.loadMessages(userId, receiverId)
    }

    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = user?.name ?: "Loading...",
                            color = PrimaryText,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = when (user?.status) {
                                "online" -> "online"
                                else -> "offline"
                            },
                            color = if (user?.status == "online") Online else SecondaryText,
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
                userId = userId.toString(),
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
            Icon(Icons.Default.AttachFile, contentDescription = "Add file", tint = Primary)
        }
        NearTalkTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = "Type message...",
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )
        if (text.isNotBlank()) {
            IconButton(onClick = onSent) {
                Icon(Icons.Default.Send, contentDescription = "Send message", tint = Primary)
            }
        } else {
            IconButton(onClick = { /* TODO:  */ }) {
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
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        verticalArrangement = Arrangement.Bottom,
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(messages, key = { it.id }) { message ->
            MessageBubble(message = message, userId = userId)
        }
    }
}

@Composable
fun MessageBubble(message: Message, userId: String) {
    val isMe = message.senderId == userId
    val backgroundColor = if (isMe) Primary else Surface
    val textColor = if (isMe) MaterialTheme.colorScheme.onPrimary else PrimaryText

    Row(
        modifier = Modifier.fillMaxWidth().padding(4.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = backgroundColor,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.animateContentSize()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = message.text, color = textColor)
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
