package com.neartalk.data.repository

import com.neartalk.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun getMessages(userId: String, receiverId: String): Flow<List<Message>>
    suspend fun sendMessage(message: Message)
}