package com.neartalk.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neartalk.data.bluetooth.AndroidBluetoothController
import com.neartalk.domain.model.Message
import com.neartalk.domain.model.MessageType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val bluetoothController: AndroidBluetoothController
) : ViewModel() {

    companion object {
        private const val TAG = "ChatViewModel"
        private const val HANDSHAKE_DELAY = 1000L
        private const val HANDSHAKE_JITTER = 500L
        private const val HANDSHAKE_TIMEOUT = 8000L
        private const val ANNOUNCE_INTERVAL = 5000L
        private const val PEER_TIMEOUT = 30000L
    }

    enum class ChatMode {
        BROADCAST,
        PRIVATE
    }

    data class PeerInfo(
        val id: String,
        val name: String,
        val lastSeen: Long = System.currentTimeMillis(),
        val macAddress: String = ""
    )

    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        FAILED
    }

    // Public StateFlows
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _currentPeerName = MutableStateFlow("Mesh Chat")
    val currentPeerName: StateFlow<String> = _currentPeerName.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val myUserId = bluetoothController.myId
    private val _myDisplayName = MutableStateFlow("User ${myUserId.take(4)}")
    val myDisplayName: StateFlow<String> = _myDisplayName.asStateFlow()

    private val _chatMode = MutableStateFlow(ChatMode.BROADCAST)
    val chatMode: StateFlow<ChatMode> = _chatMode.asStateFlow()

    private val _availablePeers = MutableStateFlow<List<PeerInfo>>(emptyList())
    val availablePeers: StateFlow<List<PeerInfo>> = _availablePeers.asStateFlow()

    private val _selectedPeer = MutableStateFlow<PeerInfo?>(null)
    val selectedPeer: StateFlow<PeerInfo?> = _selectedPeer.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    val isConnected = bluetoothController.isConnected

    private val localProcessedIds = mutableSetOf<String>()
    private val handshakeSent = AtomicBoolean(false)
    private var lastConnectionCount = 0

    private val messagesByMode = mutableMapOf<String, List<Message>>()
    private val processedIdsByMode = mutableMapOf<String, MutableSet<String>>()
    private val peerMacAddresses = mutableMapOf<String, String>()

    private val pendingMacAddress = MutableStateFlow<String?>(null)

    init {
        Log.d(TAG, "ViewModel init, myId: ${myUserId.take(8)}")
        bluetoothController.startServer()
        collectIncomingMessages()
        monitorConnection()
        startMeshDiscovery()
        cleanupStalePeers()
    }

    private fun monitorConnection() {
        viewModelScope.launch {
            bluetoothController.isConnected.collect { connected ->
                val count = bluetoothController.getActiveConnectionsCount()

                if (connected && count > lastConnectionCount) {
                    Log.d(TAG, "New connection detected (count: $count)")
                    if (_connectionState.value != ConnectionState.CONNECTING) {
                        _connectionState.value = ConnectionState.CONNECTED
                    }

                    handshakeSent.set(false)
                    val jitter = (0..HANDSHAKE_JITTER).random()
                    delay(HANDSHAKE_DELAY + jitter)

                    if (isConnected.value) {
                        sendHandshake()
                    }
                } else if (!connected) {
                    _connectionState.value = ConnectionState.DISCONNECTED
                    _availablePeers.value = emptyList()
                    _selectedPeer.value = null
                    _currentPeerName.value = "Mesh Chat"
                    handshakeSent.set(false)
                    peerMacAddresses.clear()
                    pendingMacAddress.value = null
                }
                lastConnectionCount = count
            }
        }
    }

    private fun sendHandshake() {
        if (!handshakeSent.compareAndSet(false, true)) {
            Log.d(TAG, "Handshake skipping (already sent recently)")
            return
        }

        viewModelScope.launch {
            val name = _myDisplayName.value.ifBlank { "User ${myUserId.take(4)}" }
            Log.d(TAG, "Sending handshake: $name")
            try {
                bluetoothController.broadcastMessage(name, name, MessageType.NAME_UPDATE)
            } catch (e: Exception) {
                Log.e(TAG, "Handshake failed: ${e.message}")
                handshakeSent.set(false)
            }
        }
    }

    private fun startMeshDiscovery() {
        viewModelScope.launch {
            while (isActive) {
                delay(ANNOUNCE_INTERVAL)

                if (isConnected.value) {
                    try {
                        val name = _myDisplayName.value.ifBlank { "User ${myUserId.take(4)}" }
                        Log.d(TAG, "Broadcasting mesh announce: $name")

                        bluetoothController.broadcastMessage(
                            text = name,
                            senderName = name,
                            type = MessageType.DEVICE_ANNOUNCE
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Mesh announce failed: ${e.message}")
                    }
                }
            }
        }
    }

    private fun cleanupStalePeers() {
        viewModelScope.launch {
            while (isActive) {
                delay(10000)

                val now = System.currentTimeMillis()
                _availablePeers.update { peers ->
                    val activePeers = peers.filter { peer ->
                        (now - peer.lastSeen) < PEER_TIMEOUT
                    }

                    if (activePeers.size != peers.size) {
                        Log.d(TAG, "Removed ${peers.size - activePeers.size} stale peers")
                    }

                    activePeers
                }
            }
        }
    }

    private fun collectIncomingMessages() {
        viewModelScope.launch {
            bluetoothController.incomingMessages.collect { msg ->
                handleIncomingMessage(msg)
            }
        }
    }

    private fun handleIncomingMessage(msg: Message) {
        if (msg.id in localProcessedIds) {
            return
        }
        localProcessedIds.add(msg.id)

        when (msg.type) {
            MessageType.PEER_LIST_REQUEST, MessageType.PEER_LIST_RESPONSE -> {
                Log.d(TAG, "Ignoring ${msg.type} - handled by BluetoothController")
                return
            }

            MessageType.NAME_UPDATE, MessageType.DEVICE_ANNOUNCE -> {
                if (msg.senderId == myUserId) return

                val isAnnounce = msg.type == MessageType.DEVICE_ANNOUNCE
                Log.d(TAG, "${if (isAnnounce) "Announce" else "Handshake"} from: ${msg.text} (${msg.senderId.take(8)})")

                val currentPendingMac = pendingMacAddress.value
                if (currentPendingMac != null && !isAnnounce) {
                    peerMacAddresses[msg.senderId] = currentPendingMac
                }

                val newPeer = PeerInfo(
                    id = msg.senderId,
                    name = msg.text.ifBlank { "User ${msg.senderId.take(4)}" },
                    lastSeen = System.currentTimeMillis(),
                    macAddress = peerMacAddresses[msg.senderId] ?: ""
                )

                var isNewPeer = false
                _availablePeers.update { peers ->
                    val existing = peers.find { it.id == newPeer.id }
                    if (existing != null) {
                        peers.map { if (it.id == newPeer.id) newPeer else it }
                    } else {
                        isNewPeer = true
                        peers + newPeer
                    }
                }

                if (_connectionState.value == ConnectionState.CONNECTING && currentPendingMac != null && !isAnnounce) {
                    _connectionState.value = ConnectionState.CONNECTED
                }

                if (isNewPeer && !isAnnounce && isConnected.value) {
                    Log.d(TAG, "New peer detected (${newPeer.name}), sending ECHO handshake")
                    viewModelScope.launch {
                        delay((500..2000).random().toLong())
                        handshakeSent.set(false)
                        sendHandshake()
                    }
                }
            }

            MessageType.DELIVERY_ACK -> {
                val originalMessageId = msg.text
                Log.d(TAG, "✅ ACK received for: ${originalMessageId.take(8)}")

                _messages.update { list ->
                    list.map {
                        if (it.id == originalMessageId) it.copy(status = "delivered") else it
                    }
                }
            }

            MessageType.TEXT -> {
                if (msg.senderId == myUserId) return

                val shouldShow = when (_chatMode.value) {
                    ChatMode.BROADCAST -> msg.receiverId == "ALL"
                    ChatMode.PRIVATE -> {
                        val selected = _selectedPeer.value
                        selected != null && (
                                (msg.receiverId == myUserId && msg.senderId == selected.id) ||
                                        (msg.senderId == myUserId && msg.receiverId == selected.id)
                                )
                    }
                }

                if (shouldShow) {
                    Log.d(TAG, "Show msg from ${msg.senderName}")
                    addIncomingMessage(msg)

                    if (msg.receiverId == myUserId) {
                        sendDeliveryAck(msg)
                    }
                }
            }
        }
    }

    private fun sendDeliveryAck(originalMessage: Message) {
        viewModelScope.launch {
            try {
                val ackMsg = Message(
                    id = java.util.UUID.randomUUID().toString(),
                    text = originalMessage.id,
                    senderId = myUserId,
                    senderName = _myDisplayName.value,
                    receiverId = originalMessage.senderId,
                    type = MessageType.DELIVERY_ACK,
                    timestamp = System.currentTimeMillis(),
                    ttl = 3
                )
                bluetoothController.sendPrivateMessage(ackMsg)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send ACK")
            }
        }
    }

    private fun addIncomingMessage(msg: Message) {
        _messages.update { it + msg.copy(status = "received") }
    }

    fun setChatMode(mode: ChatMode) {
        if (_chatMode.value == mode) return
        val currentKey = getCurrentModeKey()
        messagesByMode[currentKey] = _messages.value
        processedIdsByMode[currentKey] = localProcessedIds.toMutableSet()
        _chatMode.value = mode
        val newKey = getCurrentModeKey()
        _messages.value = messagesByMode[newKey] ?: emptyList()
        localProcessedIds.clear()
        localProcessedIds.addAll(processedIdsByMode[newKey] ?: emptySet())
        if (mode == ChatMode.BROADCAST) {
            _currentPeerName.value = "Всі учасники"
            _selectedPeer.value = null
        }
    }

    fun selectPeer(peer: PeerInfo) {
        if (_selectedPeer.value?.id == peer.id) return
        val currentKey = getCurrentModeKey()
        messagesByMode[currentKey] = _messages.value
        processedIdsByMode[currentKey] = localProcessedIds.toMutableSet()
        _selectedPeer.value = peer
        _currentPeerName.value = peer.name
        _chatMode.value = ChatMode.PRIVATE
        val newKey = getCurrentModeKey()
        _messages.value = messagesByMode[newKey] ?: emptyList()
        localProcessedIds.clear()
        localProcessedIds.addAll(processedIdsByMode[newKey] ?: emptySet())
    }

    private fun getCurrentModeKey(): String {
        return when (_chatMode.value) {
            ChatMode.BROADCAST -> "broadcast"
            ChatMode.PRIVATE -> "private_${_selectedPeer.value?.id ?: "none"}"
        }
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isBlank()) return
        val name = _myDisplayName.value
        val receiverId = when (_chatMode.value) {
            ChatMode.BROADCAST -> "ALL"
            ChatMode.PRIVATE -> _selectedPeer.value?.id ?: return
        }

        val localMsg = Message(
            text = text,
            senderId = myUserId,
            senderName = name,
            receiverId = receiverId,
            status = "sent",
            timestamp = System.currentTimeMillis(),
            type = MessageType.TEXT
        )

        localProcessedIds.add(localMsg.id)
        _messages.update { it + localMsg }
        _inputText.value = ""

        viewModelScope.launch {
            try {
                if (receiverId == "ALL") {
                    bluetoothController.broadcastMessage(text, name, MessageType.TEXT)
                } else {
                    bluetoothController.sendPrivateMessage(localMsg)
                }
            } catch (e: Exception) {
                updateMessageStatus(localMsg.id, "error")
            }
        }
    }

    private fun updateMessageStatus(messageId: String, newStatus: String) {
        _messages.update { list ->
            list.map { if (it.id == messageId) it.copy(status = newStatus) else it }
        }
    }

    fun connectToDevice(macAddress: String) {
        if (macAddress.isBlank()) return
        Log.d(TAG, "Connecting to MAC: $macAddress")
        _connectionState.value = ConnectionState.CONNECTING
        pendingMacAddress.value = macAddress

        viewModelScope.launch {
            try {
                bluetoothController.connectToDevice(macAddress)
                val startTime = System.currentTimeMillis()
                while (System.currentTimeMillis() - startTime < HANDSHAKE_TIMEOUT) {
                    delay(200)
                    if (_availablePeers.value.isNotEmpty()) {
                        _connectionState.value = ConnectionState.CONNECTED
                        pendingMacAddress.value = null
                        return@launch
                    }
                }
                if (bluetoothController.getActiveConnectionsCount() > 0) {
                    _connectionState.value = ConnectionState.CONNECTED
                } else {
                    _connectionState.value = ConnectionState.FAILED
                }
                pendingMacAddress.value = null
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.FAILED
                pendingMacAddress.value = null
            }
        }
    }

    fun disconnect() {
        bluetoothController.closeConnections()
        _messages.value = emptyList()
        localProcessedIds.clear()
        messagesByMode.clear()
        processedIdsByMode.clear()
        _availablePeers.value = emptyList()
        _selectedPeer.value = null
        _chatMode.value = ChatMode.BROADCAST
        _currentPeerName.value = "Mesh Chat"
        _connectionState.value = ConnectionState.DISCONNECTED
        handshakeSent.set(false)
        lastConnectionCount = 0
        peerMacAddresses.clear()
        pendingMacAddress.value = null
        viewModelScope.launch {
            delay(500)
            bluetoothController.startServer()
        }
    }

    fun onInputChanged(text: String) { _inputText.value = text }

    fun updateMyName(newName: String) {
        val trimmed = newName.trim()
        if (trimmed == _myDisplayName.value) return
        _myDisplayName.value = trimmed
        if (isConnected.value) {
            handshakeSent.set(false)
            sendHandshake()
        }
    }

    fun getMyUserId(): String = myUserId

    override fun onCleared() {
        super.onCleared()
        localProcessedIds.clear()
        messagesByMode.clear()
        processedIdsByMode.clear()
        peerMacAddresses.clear()
    }
}