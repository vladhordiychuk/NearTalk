package com.neartalk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neartalk.data.bluetooth.AndroidBluetoothController
import com.neartalk.domain.model.Message
import com.neartalk.domain.model.MessageStatus
import com.neartalk.domain.model.MessageType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val bluetoothController: AndroidBluetoothController
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _currentPeerName = MutableStateFlow<String>("Unknown")
    val currentPeerName: StateFlow<String> = _currentPeerName.asStateFlow()

    private val _myDisplayName = MutableStateFlow("Me")
    val myDisplayName: StateFlow<String> = _myDisplayName.asStateFlow()

    private val myDeviceId = UUID.randomUUID().toString()

    val isConnected = bluetoothController.isConnected

    init {
        startSession()

        // Обробка вхідних повідомлень
        viewModelScope.launch {
            bluetoothController.incomingMessages.collect { msg ->
                when (msg.type) {
                    MessageType.TEXT -> {
                        val receivedMsg = msg.copy(status = MessageStatus.RECEIVED.name.lowercase())
                        _messages.update { it + receivedMsg }
                    }
                    MessageType.NAME_UPDATE -> {
                        _currentPeerName.value = msg.text
                    }
                }
            }
        }

        // Обробка підключення пристрою
        viewModelScope.launch {
            bluetoothController.connectedDevice.collect { device ->
                if (device != null) {
                    // Оновлюємо ім'я тільки якщо воно ще "Unknown"
                    if (_currentPeerName.value == "Unknown") {
                        _currentPeerName.value = device.name ?: "Unknown Device"
                    }
                    // Відправляємо наше ім'я при підключенні
                    sendNameUpdate(_myDisplayName.value)
                } else {
                    // При відключенні скидаємо ім'я
                    _currentPeerName.value = "Unknown"
                }
            }
        }
    }

    fun startSession() {
        viewModelScope.launch {
            bluetoothController.closeConnection()
            _messages.value = emptyList()
            _inputText.value = ""
            _currentPeerName.value = "Unknown" // ДОДАНО: Скидаємо ім'я при новій сесії
            bluetoothController.startServer()
        }
    }

    fun connectToDevice(address: String) {
        viewModelScope.launch {
            try {
                bluetoothController.connectToDevice(address)
            } catch (e: Exception) {
                // ДОДАНО: Обробка помилок підключення
                println("Error connecting to device: ${e.message}")
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            bluetoothController.closeConnection()
            _messages.value = emptyList()
            _inputText.value = ""
            _currentPeerName.value = "Unknown"
            // Перезапускаємо сервер після відключення
            bluetoothController.startServer()
        }
    }

    fun onInputChanged(text: String) {
        _inputText.value = text
    }

    fun updateMyName(newName: String) {
        _myDisplayName.value = newName
        val displayName = newName.trim().ifEmpty { "Unknown" } // Або будь-який дефолт
        sendNameUpdate(displayName)
    }

    private fun sendNameUpdate(name: String) {
        val updateMsg = Message(
            id = UUID.randomUUID().toString(),
            text = name,
            senderId = myDeviceId,
            timestamp = System.currentTimeMillis(),
            type = MessageType.NAME_UPDATE
        )
        viewModelScope.launch {
            try {
                bluetoothController.sendMessage(updateMsg)
            } catch (e: Exception) {
                // ДОДАНО: Обробка помилок відправки
                println("Error sending name update: ${e.message}")
            }
        }
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isBlank()) return

        val message = Message(
            id = UUID.randomUUID().toString(),
            text = text,
            senderId = myDeviceId,
            receiverId = "peer",
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT.name.lowercase(),
            type = MessageType.TEXT
        )

        // Додаємо повідомлення в кінець списку (нове в кінці)
        _messages.update { it + message }
        _inputText.value = ""

        viewModelScope.launch {
            try {
                bluetoothController.sendMessage(message)
                // ОПЦІОНАЛЬНО: Можна оновити статус на DELIVERED після успішної відправки
                // updateMessageStatus(message.id, MessageStatus.DELIVERED)
            } catch (e: Exception) {
                // ДОДАНО: Обробка помилок відправки
                println("Error sending message: ${e.message}")
                // Можна оновити статус на ERROR
                // updateMessageStatus(message.id, MessageStatus.ERROR)
            }
        }
    }

    fun clearChat() {
        _messages.value = emptyList()
        _inputText.value = ""
    }

    fun getMyUserId(): String = myDeviceId

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            bluetoothController.closeConnection()
        }
    }
}