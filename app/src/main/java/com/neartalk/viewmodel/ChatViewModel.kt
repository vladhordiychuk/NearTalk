package com.neartalk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neartalk.data.repository.MessageRepository
import com.neartalk.domain.model.Message
import com.neartalk.domain.model.MessageStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: MessageRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    fun loadMessages(userId: String) {
        viewModelScope.launch {
            try {
                repository.getMessages(userId).collect { messages ->
                    _messages.value = messages
                }
            } catch (e: Exception) {
                // Handle error (log, snackbar, etc.)
            }
        }
    }

    fun onInputChanged(newText: String) {
        _inputText.value = newText
    }

    fun onMessageSent(userId: String, receiverId: String) {
        val text = _inputText.value
        if (text.isNotBlank()) {
            val newMessage = Message(
                text = text,
                senderId = userId,
                receiverId = receiverId,
                timestamp = System.currentTimeMillis(),
                status = MessageStatus.SENT.name.lowercase()
            )

            viewModelScope.launch {
                try {
                    repository.sendMessage(newMessage)
                    _inputText.value = ""
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }
    }
}
