package com.neartalk.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.neartalk.domain.model.Message
import com.neartalk.ui.theme.Online
import com.neartalk.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    receiverId: String,
    isPrivate: Boolean,
    onBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val peerName by viewModel.currentPeerName.collectAsState()
    val myName by viewModel.myDisplayName.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val myUserId = remember { viewModel.getMyUserId() }

    var isNameEditVisible by remember { mutableStateOf(false) }

    LaunchedEffect(receiverId, isPrivate) {
        if (isPrivate) {
            val peer = viewModel.availablePeers.value.find { it.id == receiverId }
            if (peer != null) {
                viewModel.selectPeer(peer)
            }
        } else {
            viewModel.setChatMode(ChatViewModel.ChatMode.BROADCAST)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {

            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            Column {
                MinimalTopBar(
                    title = if (isPrivate) peerName else "Глобальний Чат",
                    isPrivate = isPrivate,
                    connectionState = connectionState,
                    onBack = {
                        viewModel.setChatMode(ChatViewModel.ChatMode.BROADCAST)
                        onBack()
                    },
                    onToggleNameEdit = { isNameEditVisible = !isNameEditVisible },
                    onOpenDrawer = if (isPrivate) onOpenDrawer else null,
                    isEditActive = isNameEditVisible
                )

                AnimatedVisibility(
                    visible = isNameEditVisible,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    MyNameEditorBar(
                        name = myName,
                        onNameChange = { viewModel.updateMyName(it) }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        },
        bottomBar = {
            MinimalInputBar(
                text = inputText,
                onTextChange = viewModel::onInputChanged,
                onSent = { viewModel.sendMessage() },
                enabled = true,
                modifier = Modifier.imePadding()
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (messages.isEmpty()) {
                EmptyChatState(
                    text = if (isPrivate) "Немає повідомлень з цим пристроєм" else "Напишіть повідомлення всім навколо",
                    isConnecting = isPrivate && connectionState == ChatViewModel.ConnectionState.CONNECTING
                )
            } else {
                ChatMessages(
                    messages = messages,
                    userId = myUserId,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun MinimalTopBar(
    title: String,
    isPrivate: Boolean,
    connectionState: ChatViewModel.ConnectionState,
    onBack: () -> Unit,
    onToggleNameEdit: () -> Unit,
    onOpenDrawer: (() -> Unit)?,
    isEditActive: Boolean
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 4.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedContent(
                    targetState = title,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "title"
                ) { targetTitle ->
                    Text(
                        text = targetTitle,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (isPrivate) {
                    val statusText = when (connectionState) {
                        ChatViewModel.ConnectionState.CONNECTING -> "підключення..."
                        ChatViewModel.ConnectionState.CONNECTED -> "онлайн"
                        ChatViewModel.ConnectionState.FAILED -> "помилка"
                        else -> "офлайн"
                    }

                    val statusColor = when (connectionState) {
                        ChatViewModel.ConnectionState.CONNECTING -> MaterialTheme.colorScheme.primary
                        ChatViewModel.ConnectionState.CONNECTED -> Online
                        ChatViewModel.ConnectionState.FAILED -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(statusColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = statusText,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = "Mesh Network",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            }

            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleNameEdit) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Ім'я",
                        tint = if (isEditActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (onOpenDrawer != null) {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            imageVector = Icons.Default.BluetoothSearching,
                            contentDescription = "Пристрої",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MyNameEditorBar(name: String, onNameChange: (String) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(16.dp, 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Я відображаюсь як:",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 8.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                    .padding(12.dp, 6.dp)
            ) {
                if (name.isEmpty()) {
                    Text(
                        text = "Введіть ім'я...",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontSize = 14.sp
                    )
                }

                BasicTextField(
                    value = name,
                    onValueChange = onNameChange,
                    textStyle = TextStyle(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun MinimalInputBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSent: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(12.dp, 8.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val inputBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

            TextField(
                value = text,
                onValueChange = onTextChange,
                placeholder = {
                    Text(
                        "Повідомлення...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 15.sp
                    )
                },
                modifier = Modifier.weight(1f),
                enabled = true,
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = inputBg,
                    unfocusedContainerColor = inputBg,
                    disabledContainerColor = inputBg,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                maxLines = 4
            )

            val hasText = text.isNotBlank()

            IconButton(
                onClick = onSent,
                enabled = hasText,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .size(40.dp)
                    .background(
                        if (hasText) MaterialTheme.colorScheme.primary else inputBg,
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = null,
                    tint = if (hasText) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyChatState(text: String, isConnecting: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isConnecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 4.dp
                )
                Text(
                    text = "Підключення...",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Icon(
                    Icons.Default.ChatBubbleOutline,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = text,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ChatMessages(messages: List<Message>, userId: String, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    val reversedMessages = remember(messages) { messages.reversed() }

    LaunchedEffect(reversedMessages.size) {
        if (reversedMessages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        reverseLayout = true,
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom)
    ) {
        items(reversedMessages, key = { it.id }) { msg ->
            MinimalMessageBubble(msg, userId)
        }
    }
}

@Composable
fun MinimalMessageBubble(message: Message, userId: String) {
    val isMe = message.senderId == userId
    val bubbleColor = if (isMe) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isMe) MaterialTheme.colorScheme.onPrimary
    else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Surface(
            color = bubbleColor,
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isMe) 18.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 18.dp
            ),
            shadowElevation = 1.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp, 10.dp)) {
                if (!isMe && message.senderName.isNotBlank()) {
                    Text(
                        text = message.senderName,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Text(
                    text = message.text,
                    color = contentColor,
                    fontSize = 15.sp
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.formattedTimestamp(),
                        color = contentColor.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )

                    if (isMe) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = when (message.status) {
                                "delivered" -> Icons.Default.DoneAll
                                "error" -> Icons.Default.ErrorOutline
                                else -> Icons.Default.Done
                            },
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = when (message.status) {
                                "error" -> MaterialTheme.colorScheme.error
                                else -> contentColor.copy(alpha = 0.7f)
                            }
                        )
                    }
                }
            }
        }
    }
}