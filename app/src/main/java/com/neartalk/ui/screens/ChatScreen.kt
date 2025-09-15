package com.neartalk.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.filled.Mic
import com.neartalk.models.Message
import com.neartalk.viewmodel.ChatViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import com.neartalk.ui.theme.SecondaryText
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.lazy.rememberLazyListState
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToFiles: () -> Unit,
    viewModel: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val messages = viewModel.messages.value
    val inputText = viewModel.inputText.value

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Dima",
                            color = PrimaryText,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "online",
                            color = Online,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Arrow back",
                            tint = Primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "User",
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
                Column {
                    ChatInputBar(
                        text = inputText,
                        onTextChange = viewModel::onInputChanged,
                        onNavigateToFiles = onNavigateToFiles,
                        onSent = { viewModel.onMessageSent() }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            ChatMessages(messages = messages, modifier = Modifier.weight(1f))
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
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateToFiles) {
            Icon(Icons.Default.AttachFile, contentDescription = "Attach", tint = Primary)
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
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Primary)
            }
        } else {
            IconButton(onClick = { /* TODO: Voice/Video */ }) {
                Icon(Icons.Default.Mic, contentDescription = "Record", tint = Primary)
            }
        }
    }
}

@Composable
fun ChatMessages(messages: List<Message>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(0)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        reverseLayout = true
    ) {
        items(messages) { message ->
            MessageBubble(message)
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        horizontalArrangement = if (message.isSentByMe) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (message.isSentByMe) Primary else Surface,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                color = if (message.isSentByMe) SecondaryText else PrimaryText
            )
        }
    }
}
