package com.neartalk.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.neartalk.models.Message

class ChatViewModel : ViewModel() {
    var messages = mutableStateOf<List<Message>>(emptyList())
        private set
    var inputText = mutableStateOf("")
        private set

    fun onInputChanged(newText: String) {
        inputText.value = newText
    }

    fun onMessageSent() {
        if (inputText.value.isNotBlank()) {
            val newMessage = Message(
                id = messages.value.size + 1,
                chatId = 1,
                sender = "Me",
                text = inputText.value,
                time = System.currentTimeMillis(),
                isRead = true,
                isSentByMe = true,
            )
            messages.value = listOf(newMessage) + messages.value
            inputText.value = ""
        }
    }
}