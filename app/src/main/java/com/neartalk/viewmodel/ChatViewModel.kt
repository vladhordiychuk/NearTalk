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
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    fun getUser(receiverId: Int): Flow<User?> {
        return userRepository.getUserById(receiverId)
    }

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    fun loadMessages(userId: Int, receiverId: Int) {
        viewModelScope.launch {
            try {
                messageRepository.getMessages(userId.toString(), receiverId.toString()).collect { messages ->
                    _messages.value = messages
                }
            } catch (e: Exception) {

            }
        }
    }

    fun onInputChanged(newText: String) {
        _inputText.value = newText
    }

    fun onMessageSent(userId: Int, receiverId: Int) {
        val text = _inputText.value
        if (text.isBlank()) return

        val newMessage = Message(
            text = text,
            senderId = userId.toString(),
            receiverId = receiverId.toString(),
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT.name.lowercase()
        )

        _inputText.value = ""

        viewModelScope.launch {
            try {
                messageRepository.sendMessage(newMessage)
            } catch (e: Exception) {
            }
        }
    }

}