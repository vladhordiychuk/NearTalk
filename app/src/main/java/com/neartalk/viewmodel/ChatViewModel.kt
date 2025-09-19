package com.neartalk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neartalk.data.repository.MessageRepository
import com.neartalk.data.repository.UserRepository
import com.neartalk.domain.model.Message
import com.neartalk.domain.model.MessageStatus
import com.neartalk.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.UUID

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    fun getUser(receiverId: String): Flow<User?> { // Змінено з Int на String
        return userRepository.getUserById(receiverId)
    }

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    fun loadMessages(userId: String, receiverId: String) { // Змінено з Int на String
        viewModelScope.launch {
            try {
                messageRepository.getMessagesForUser(userId, receiverId).collect { messages ->
                    _messages.value = messages
                }
            } catch (e: Exception) {
                println("DEBUG: Error loading messages: ${e.message}")
            }
        }
    }

    fun onInputChanged(newText: String) {
        _inputText.value = newText
    }

    fun onMessageSent(userId: String, receiverId: String) { // Змінено з Int на String
        val text = _inputText.value
        if (text.isBlank()) return

        val newMessage = Message(
            id = UUID.randomUUID().toString(),
            text = text,
            senderId = userId,
            receiverId = receiverId,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT.name.lowercase()
        )

        _inputText.value = ""

        viewModelScope.launch {
            try {
                messageRepository.insertMessage(newMessage)
            } catch (e: Exception) {
                println("DEBUG: Error sending message: ${e.message}")
            }
        }
    }
}