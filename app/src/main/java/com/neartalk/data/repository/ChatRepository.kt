package com.neartalk.data.repository

import com.neartalk.domain.model.Chat
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getAllChats(): Flow<List<Chat>>
    suspend fun deleteChat(chatId: String)
    suspend fun updatePinStatus(chatId: String, isPinned: Boolean)
    suspend fun updateMuteStatus(chatId: String, isMuted: Boolean)
}