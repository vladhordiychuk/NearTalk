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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neartalk.domain.model.Message
import com.neartalk.viewmodel.ChatViewModel
import com.neartalk.ui.theme.Online // Імпортуємо статусний колір

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    receiverId: String,
    onBack: () -> Unit,
    onOpenDrawer: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val peerName by viewModel.currentPeerName.collectAsState()
    val myName by viewModel.myDisplayName.collectAsState()
    val isConnected = viewModel.isConnected.collectAsState(initial = false).value

    val myUserId = remember { viewModel.getMyUserId() }
    var isNameEditVisible by remember { mutableStateOf(false) }

    LaunchedEffect(receiverId) {
        if (receiverId.isNotBlank()) {
            viewModel.connectToDevice(receiverId)
        }
    }

    Scaffold(
        topBar = {
            Column {
                MinimalTopBar(
                    peerName = peerName,
                    isConnected = isConnected,
                    onBack = {
                        viewModel.disconnect()
                        onBack()
                    },
                    onToggleNameEdit = { isNameEditVisible = !isNameEditVisible },
                    onOpenDrawer = onOpenDrawer,
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
                // Розділювач кольору outlineVariant (м'який сірий)
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        },
        bottomBar = {
            if (isConnected) {
                MinimalInputBar(
                    text = inputText,
                    onTextChange = viewModel::onInputChanged,
                    onSent = { viewModel.sendMessage() },
                    enabled = isConnected,
                    modifier = Modifier.imePadding()
                )
            }
        },
        // Фон береться з теми
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!isConnected) {
                WaitingState()
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
fun MyNameEditorBar(name: String, onNameChange: (String) -> Unit) {
    // SurfaceVariant - трохи відрізняється від фону
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
fun MinimalTopBar(
    peerName: String,
    isConnected: Boolean,
    onBack: () -> Unit,
    onToggleNameEdit: () -> Unit,
    onOpenDrawer: () -> Unit,
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
            // ЛІВА ЧАСТИНА
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Вихід",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // ЦЕНТР (Абсолютне позиціювання)
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedContent(
                    targetState = peerName,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "name"
                ) { name ->
                    Text(
                        text = name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                AnimatedVisibility(visible = isConnected) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Online, CircleShape) // Зелений статус
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "онлайн",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ПРАВА ЧАСТИНА
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onToggleNameEdit) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Ім'я",
                        tint = if (isEditActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
                enabled = enabled,
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

            val hasText = enabled && text.isNotBlank()
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
                    tint = if (hasText) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun WaitingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.BluetoothSearching,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = "Очікування підключення...",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// У ChatScreen.kt

@Composable
fun ChatMessages(messages: List<Message>, userId: String, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()

    // 1. Беремо список повідомлень і перевертаємо його.
    // [Старе, Нове] -> перетворюється на -> [Нове, Старе]
    // Тепер "Нове" має індекс 0.
    val reversedMessages = remember(messages) { messages.reversed() }

    // Автоскрол до низу (тобто до індексу 0 при reverseLayout) при появі нових повідомлень
    LaunchedEffect(reversedMessages.size) {
        if (reversedMessages.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    if (messages.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Почніть розмову", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            state = listState,
            // 2. Вмикаємо реверс лейауту.
            // Це означає: "Малюй елемент з індексом 0 у самому низу екрану".
            reverseLayout = true,
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom)
        ) {
            // Передаємо перевернутий список, де Нове повідомлення — перше
            items(reversedMessages, key = { it.id }) { msg ->
                MinimalMessageBubble(msg, userId)
            }
        }
    }
}

@Composable
fun MinimalMessageBubble(message: Message, userId: String) {
    val isMe = message.senderId == userId

    // Кольори з теми
    val bubbleColor = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

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
                Text(
                    text = message.text,
                    color = contentColor,
                    fontSize = 15.sp
                )
                Text(
                    text = message.formattedTimestamp(),
                    color = contentColor.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp)
                )
            }
        }
    }
}