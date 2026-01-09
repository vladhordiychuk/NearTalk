package com.neartalk.data.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import com.neartalk.domain.model.BluetoothDeviceDomain
import com.neartalk.domain.model.Message
import com.neartalk.domain.model.MessageType
import com.neartalk.utils.SecurityUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.UUID
import java.util.Collections
import java.util.LinkedHashMap
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.coroutineContext

@SuppressLint("MissingPermission")
class AndroidBluetoothController(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?
) {
    companion object {
        private const val TAG = "BT_Controller"
        private const val MAX_PROCESSED_IDS = 1000
        private const val CONNECTION_TIMEOUT_MS = 15_000L
        private const val MAX_MESSAGE_SIZE = 1024 * 100 // 100 KB
        private const val SERVER_START_DELAY = 1000L
        private const val ROUTE_CLEANUP_INTERVAL = 60_000L // 1 минута
    }

    private val SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val SERVICE_NAME = "NearTalkMesh"
    val myId = SecurityUtils.getMyAnonymousId(context)

    private val jsonFormat = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    val scannedDevices: StateFlow<List<BluetoothDeviceDomain>> = _scannedDevices.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<Message>(replay = 0, extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<Message> = _incomingMessages.asSharedFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _bluetoothState = MutableStateFlow(BluetoothState.UNKNOWN)
    val bluetoothState: StateFlow<BluetoothState> = _bluetoothState.asStateFlow()

    // Internal State
    private var controllerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val activeConnections = ConcurrentHashMap<String, ConnectedNode>()
    private val activeJobs = ConcurrentHashMap<String, Job>()

    private val routingTable = ConcurrentHashMap<String, RouteInfo>()

    private val processedMessageIds = Collections.synchronizedSet(
        Collections.newSetFromMap(
            object : LinkedHashMap<String, Boolean>(500, 0.75f, true) {
                override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Boolean>?): Boolean {
                    return size > MAX_PROCESSED_IDS
                }
            }
        )
    )

    private var serverSocket: BluetoothServerSocket? = null
    private var isReceiverRegistered = false
    private var serverJob: Job? = null

    private data class RouteInfo(
        val nextHopMac: String,
        val timestamp: Long = System.currentTimeMillis()
    )

    private data class ConnectedNode(
        val socket: BluetoothSocket,
        val outputStream: DataOutputStream,
        val macAddress: String,
        @Volatile var isActive: Boolean = true
    )

    enum class BluetoothState { UNKNOWN, ON, OFF }

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                updateBluetoothState()
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)

                when (state) {
                    BluetoothAdapter.STATE_OFF -> {
                        Log.w(TAG, "Bluetooth OFF - cleaning up")
                        closeConnections()
                    }
                    BluetoothAdapter.STATE_ON -> {
                        Log.d(TAG, "Bluetooth ON - starting server")
                        controllerScope.launch {
                            delay(SERVER_START_DELAY)
                            startServer()
                        }
                    }
                }
            }
        }
    }

    private fun updateBluetoothState() {
        _bluetoothState.value = if (bluetoothAdapter?.isEnabled == true)
            BluetoothState.ON else BluetoothState.OFF
    }

    init {
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(bluetoothStateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(bluetoothStateReceiver, filter)
        }
        updateBluetoothState()

        startRouteCleanup()
    }

    private fun startRouteCleanup() {
        controllerScope.launch {
            while (isActive) {
                delay(ROUTE_CLEANUP_INTERVAL)

                val now = System.currentTimeMillis()
                val staleRoutes = routingTable.filter { (_, routeInfo) ->
                    (now - routeInfo.timestamp) > ROUTE_CLEANUP_INTERVAL * 2
                }

                staleRoutes.keys.forEach { userId ->
                    routingTable.remove(userId)
                    Log.d(TAG, "Removed stale route to ${userId.take(6)}")
                }
            }
        }
    }

    private val foundDeviceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BluetoothDevice.ACTION_FOUND) {
                val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                }
                val rssi = intent?.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)

                device?.let { d ->
                    if (!d.address.isNullOrBlank()) {
                        val newDevice = BluetoothDeviceDomain(d.name, d.address, rssi ?: Short.MIN_VALUE)
                        _scannedDevices.update { devices ->
                            val idx = devices.indexOfFirst { it.address == newDevice.address }
                            if (idx >= 0) {
                                devices.toMutableList().apply { set(idx, newDevice) }
                            } else {
                                devices + newDevice
                            }
                        }
                    }
                }
            }
        }
    }

    fun startDiscovery() {
        if (!hasPermission(android.Manifest.permission.BLUETOOTH_SCAN)) return

        try {
            if (!isReceiverRegistered) {
                val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    context.registerReceiver(foundDeviceReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
                } else {
                    context.registerReceiver(foundDeviceReceiver, filter)
                }
                isReceiverRegistered = true
            }

            _scannedDevices.value = emptyList()
            bluetoothAdapter?.cancelDiscovery()
            bluetoothAdapter?.startDiscovery()
            Log.d(TAG, "🔍 Discovery started")
        } catch (e: Exception) {
            Log.e(TAG, "Discovery error", e)
        }
    }

    fun stopDiscovery() {
        if (!hasPermission(android.Manifest.permission.BLUETOOTH_SCAN)) return
        try {
            bluetoothAdapter?.cancelDiscovery()
            Log.d(TAG, "🔍 Discovery stopped")
        } catch (e: Exception) {}

        if (isReceiverRegistered) {
            try {
                context.unregisterReceiver(foundDeviceReceiver)
                isReceiverRegistered = false
            } catch (e: Exception) {}
        }
    }

    fun connectToDevice(macAddress: String) {
        if (!hasPermission(android.Manifest.permission.BLUETOOTH_CONNECT)) return

        if (activeConnections.containsKey(macAddress)) {
            Log.d(TAG, "Already connected to $macAddress")
            return
        }

        stopDiscovery()

        controllerScope.launch {
            delay(1000)

            try {
                Log.d(TAG, "🔌 Connecting to $macAddress (Insecure)...")
                val device = bluetoothAdapter?.getRemoteDevice(macAddress) ?: return@launch

                val socket = withTimeout(CONNECTION_TIMEOUT_MS) {
                    val sock = device.createInsecureRfcommSocketToServiceRecord(SERVICE_UUID)
                    withContext(Dispatchers.IO) {
                        sock.connect()
                    }
                    sock
                }

                Log.d(TAG, "Connected to $macAddress")
                handleSocket(socket)

            } catch (e: Exception) {
                Log.e(TAG, "Connection failed: $macAddress - ${e.message}")
            }
        }
    }

    fun startServer() {
        if (!hasPermission(android.Manifest.permission.BLUETOOTH_CONNECT)) return

        if (serverJob?.isActive == true) {
            Log.d(TAG, "Server already running")
            return
        }

        serverJob?.cancel()

        serverJob = controllerScope.launch {
            while (isActive) {
                try {
                    serverSocket?.close()
                    serverSocket = bluetoothAdapter?.listenUsingInsecureRfcommWithServiceRecord(
                        SERVICE_NAME, SERVICE_UUID
                    )
                    Log.d(TAG, "🎧 Server listening (Insecure)...")

                    while (isActive) {
                        val socket = try {
                            withContext(Dispatchers.IO) { serverSocket?.accept() }
                        } catch (e: IOException) {
                            if (isActive) {
                                Log.w(TAG, "Accept failed, retrying...")
                                delay(1000)
                            }
                            null
                        } ?: continue

                        Log.d(TAG, "📞 Incoming connection from ${socket.remoteDevice.address}")
                        launch { handleSocket(socket) }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Server crashed, restarting...", e)
                    delay(2000)
                }
            }
        }
    }

    private fun handleSocket(socket: BluetoothSocket) {
        val address = socket.remoteDevice.address

        if (activeConnections.containsKey(address)) {
            Log.w(TAG, "Duplicate connection from $address, closing")
            try { socket.close() } catch (e: Exception) {}
            return
        }

        val node = try {
            ConnectedNode(
                socket = socket,
                outputStream = DataOutputStream(socket.outputStream),
                macAddress = address
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create node for $address", e)
            try { socket.close() } catch (ex: Exception) {}
            return
        }

        activeConnections[address] = node
        updateConnectionStatus()
        Log.d(TAG, "✅ Node added: $address (total: ${activeConnections.size})")

        val job = controllerScope.launch(Dispatchers.IO) {
            readLoop(node)
        }
        activeJobs[address] = job
    }

    private suspend fun readLoop(node: ConnectedNode) {
        val inputStream = DataInputStream(node.socket.inputStream)
        val buffer = ByteArray(MAX_MESSAGE_SIZE)

        try {
            while (coroutineContext.isActive && node.isActive) {
                val length = try {
                    withContext(Dispatchers.IO) { inputStream.readInt() }
                } catch (e: Exception) {
                    Log.w(TAG, "Connection closed: ${node.macAddress}")
                    break
                }

                if (length <= 0 || length > MAX_MESSAGE_SIZE) {
                    Log.w(TAG, "Invalid message length: $length from ${node.macAddress}")
                    continue
                }

                val bytes = try {
                    withContext(Dispatchers.IO) {
                        inputStream.readFully(buffer, 0, length)
                        buffer.copyOfRange(0, length)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to read message from ${node.macAddress}")
                    break
                }

                val jsonString = String(bytes, Charsets.UTF_8)
                try {
                    val message = jsonFormat.decodeFromString(Message.serializer(), jsonString)
                    processIncomingMessage(message, node)
                } catch (e: Exception) {
                    Log.e(TAG, "JSON decode error from ${node.macAddress}", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Read loop error: ${node.macAddress} - ${e.message}")
        } finally {
            closeNode(node)
        }
    }

    private suspend fun processIncomingMessage(message: Message, source: ConnectedNode) {
        if (!processedMessageIds.add(message.id)) {
            return
        }

        val expectedHash = SecurityUtils.generateHash(message.text, message.timestamp, message.senderId)
        if (message.contentHash != expectedHash) {
            Log.w(TAG, "⚠️ Hash mismatch for message ${message.id.take(8)}")
            return
        }

        if (message.senderId != myId) {
            val oldRoute = routingTable[message.senderId]
            routingTable[message.senderId] = RouteInfo(
                nextHopMac = source.macAddress,
                timestamp = System.currentTimeMillis()
            )

            if (oldRoute?.nextHopMac != source.macAddress) {
                Log.d(TAG, "📍 Route updated: ${message.senderId.take(6)} via ${source.macAddress}")
            }
        }

        Log.d(TAG, "📨 [${message.type}] from ${message.senderName} (${message.senderId.take(6)}): ${message.text.take(30)}")

        if (message.receiverId == myId || message.receiverId == "ALL") {
            _incomingMessages.emit(message)
        }

        if (message.ttl > 0) {
            val forwardedMsg = message.copy(ttl = message.ttl - 1)
            relayToMesh(forwardedMsg, excludeNode = source)
        }
    }

    private fun relayToMesh(message: Message, excludeNode: ConnectedNode?) {
        val json = jsonFormat.encodeToString(message)
        val bytes = json.toByteArray(Charsets.UTF_8)

        if (message.receiverId != "ALL" && message.receiverId != myId) {
            val routeInfo = routingTable[message.receiverId]

            if (routeInfo != null) {
                val targetNode = activeConnections[routeInfo.nextHopMac]

                if (targetNode != null && targetNode.isActive && targetNode != excludeNode) {
                    Log.d(TAG, "🔀 Routing private msg to ${message.receiverId.take(6)} via ${routeInfo.nextHopMac}")
                    try {
                        sendToNode(bytes, targetNode)
                        return
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to route via ${routeInfo.nextHopMac}, trying flood", e)
                    }
                } else {
                    Log.w(TAG, "⚠️ Next hop ${routeInfo.nextHopMac} is offline, using flood")
                }
            } else {
                Log.d(TAG, "🌊 No route to ${message.receiverId.take(6)}, using flood")
            }
        }

        val targets = activeConnections.values.filter {
            it.isActive && it != excludeNode
        }

        if (targets.isEmpty()) {
            Log.w(TAG, "⚠️ No targets for relay")
            return
        }

        Log.d(TAG, "📢 Broadcasting to ${targets.size} nodes")
        targets.forEach { node ->
            try {
                sendToNode(bytes, node)
            } catch (e: Exception) {
                Log.e(TAG, "Relay failed to ${node.macAddress}", e)
            }
        }
    }

    private fun sendToNode(bytes: ByteArray, node: ConnectedNode) {
        synchronized(node.outputStream) {
            node.outputStream.writeInt(bytes.size)
            node.outputStream.write(bytes)
            node.outputStream.flush()
        }
    }

    suspend fun broadcastMessage(text: String, senderName: String, type: MessageType) {
        withContext(Dispatchers.Default) {
            val ts = System.currentTimeMillis()
            val hash = SecurityUtils.generateHash(text, ts, myId)
            val msg = Message(
                id = UUID.randomUUID().toString(),
                text = text,
                senderId = myId,
                senderName = senderName,
                receiverId = "ALL",
                timestamp = ts,
                ttl = 5,
                contentHash = hash,
                type = type
            )
            processedMessageIds.add(msg.id)

            Log.d(TAG, "📤 Broadcasting [${type}]: $text")
            relayToMesh(msg, null)
        }
    }

    suspend fun sendPrivateMessage(message: Message) {
        withContext(Dispatchers.Default) {
            val hash = SecurityUtils.generateHash(message.text, message.timestamp, message.senderId)
            val secureMsg = message.copy(contentHash = hash)
            processedMessageIds.add(secureMsg.id)

            Log.d(TAG, "📤 Sending private to ${message.receiverId.take(6)}: ${message.text.take(30)}")
            relayToMesh(secureMsg, null)
        }
    }

    private fun closeNode(node: ConnectedNode) {
        node.isActive = false
        activeConnections.remove(node.macAddress)

        val removedRoutes = routingTable.entries
            .filter { it.value.nextHopMac == node.macAddress }
            .map { it.key }

        removedRoutes.forEach { userId ->
            routingTable.remove(userId)
            Log.d(TAG, "🗑️ Route removed: ${userId.take(6)} (via ${node.macAddress})")
        }

        try { node.socket.close() } catch (e: Exception) {}
        updateConnectionStatus()

        Log.d(TAG, "❌ Node removed: ${node.macAddress} (total: ${activeConnections.size})")
    }

    fun closeConnections() {
        Log.d(TAG, "🔌 Closing all connections...")
        activeConnections.values.forEach { closeNode(it) }
        activeConnections.clear()
        routingTable.clear()
        try { serverSocket?.close() } catch (e: Exception) {}
        updateConnectionStatus()
    }

    fun cleanup() {
        Log.d(TAG, "🧹 Cleanup started")
        stopDiscovery()
        closeConnections()
        try { context.unregisterReceiver(bluetoothStateReceiver) } catch (e: Exception) {}
        controllerScope.cancel()
        Log.d(TAG, "✅ Cleanup completed")
    }

    private fun updateConnectionStatus() {
        val wasConnected = _isConnected.value
        val isNowConnected = activeConnections.isNotEmpty()

        _isConnected.value = isNowConnected

        if (wasConnected != isNowConnected) {
            Log.d(TAG, "🔄 Connection status: ${if (isNowConnected) "CONNECTED" else "DISCONNECTED"}")
        }
    }

    fun getActiveConnectionsCount() = activeConnections.size

    private fun hasPermission(permission: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.checkSelfPermission(permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else true
    }

    fun getRoutingTableSnapshot(): Map<String, String> {
        return routingTable.mapValues { it.value.nextHopMac }
    }
}