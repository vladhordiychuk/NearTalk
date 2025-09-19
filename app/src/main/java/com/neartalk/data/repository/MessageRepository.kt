package com.neartalk.data.repository

import com.neartalk.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun getMessagesForUser(userId: String, receiverId: String): Flow<List<Message>>
    suspend fun insertMessage(message: Message)
    suspend fun deleteMessage(message: Message)
}

