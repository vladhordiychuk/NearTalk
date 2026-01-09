package com.neartalk.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.neartalk.viewmodel.DevicesViewModel
import com.neartalk.viewmodel.DevicesViewModel.ScanState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    onConnect: (deviceId: String, deviceName: String) -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DevicesViewModel = hiltViewModel()
) {
    val devices by viewModel.availableDevices.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val scanState by viewModel.scanState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startScan()
    }

    ModalDrawerSheet(
        modifier = modifier.fillMaxWidth(0.85f),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerShape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp, topEnd = 0.dp, bottomEnd = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            DevicesHeader(
                isScanning = isScanning,
                onClose = onCloseDrawer,
                onRefresh = { viewModel.startScan() }
            )

            when {
                scanState is ScanState.Error -> {
                    ErrorState(
                        message = (scanState as ScanState.Error).message,
                        onRetry = { viewModel.startScan() }
                    )
                }
                devices.isEmpty() && isScanning -> {
                    ScanningState()
                }
                devices.isEmpty() && !isScanning -> {
                    EmptyState(onStartScan = { viewModel.startScan() })
                }
                else -> {
                    DevicesList(
                        devices = devices,
                        onConnect = { device ->
                            viewModel.connectToDevice(device.id)
                            onConnect(device.id, device.name)
                            onCloseDrawer()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DevicesHeader(
    isScanning: Boolean,
    onClose: () -> Unit,
    onRefresh: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Пристрої поблизу",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onRefresh,
                        enabled = !isScanning
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Оновити",
                            tint = if (isScanning) MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.scale(
                                if (isScanning) {
                                    infiniteRepeating(1f, 1.2f)
                                } else 1f
                            )
                        )
                    }

                    IconButton(onClick = onClose) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Закрити",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AnimatedVisibility(visible = isScanning) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                            .alpha(pulsatingAlpha())
                    )
                }
                Text(
                    text = if (isScanning) "Пошук активний..." else "Bluetooth активний",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun DevicesList(
    devices: List<DeviceItem>,
    onConnect: (DeviceItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(
            items = devices,
            key = { it.id }
        ) { device ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically()
            ) {
                MinimalDeviceCard(
                    device = device,
                    onConnect = { onConnect(device) }
                )
            }
        }
    }
}

@Composable
fun MinimalDeviceCard(
    device: DeviceItem,
    onConnect: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = if (isPressed) 1.dp else 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                isPressed = true
                onConnect()
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        device.name.contains("phone", ignoreCase = true) -> Icons.Default.Smartphone
                        device.name.contains("tablet", ignoreCase = true) -> Icons.Default.Tablet
                        device.name.contains("laptop", ignoreCase = true) -> Icons.Default.Laptop
                        else -> Icons.Default.Devices
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = device.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.SignalCellularAlt,
                        contentDescription = null,
                        tint = com.neartalk.ui.theme.Online,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = device.distance,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.clickable(onClick = onConnect)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Підключити",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun ScanningState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    .alpha(pulsatingAlpha()),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.BluetoothSearching,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "Пошук пристроїв...",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Переконайтесь, що Bluetooth\nувімкнений на обох пристроях",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun EmptyState(onStartScan: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.BluetoothDisabled,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )

            Text(
                text = "Пристроїв не знайдено",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Спробуйте оновити список\nабо перевірте налаштування",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onStartScan,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Почати пошук")
            }
        }
    }
}

@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Text(
                text = "Помилка",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = message,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Спробувати знову")
            }
        }
    }
}

@Composable
fun pulsatingAlpha(): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    return infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    ).value
}

@Composable
fun infiniteRepeating(from: Float, to: Float): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "rotate")
    return infiniteTransition.animateFloat(
        initialValue = from,
        targetValue = to,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    ).value
}

data class DeviceItem(
    val id: String,
    val name: String,
    val distance: String = "Поблизу",
    val signalStrength: Int = 100
)