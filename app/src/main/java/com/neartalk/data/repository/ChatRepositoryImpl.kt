package com.neartalk.data.repository

import com.neartalk.data.local.ChatDao
import com.neartalk.data.local.entity.ChatEntity
import com.neartalk.domain.model.Chat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val chatDao: ChatDao
) : ChatRepository {

    override fun getAllChats(): Flow<List<Chat>> {
        return chatDao.getAllChats().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun deleteChat(chatId: String) {
        val chat = chatDao.getChatById(chatId).firstOrNull()
        if (chat != null) {
            chatDao.deleteChat(chat)
        }
    }

    override suspend fun updatePinStatus(chatId: String, isPinned: Boolean) {
        chatDao.updatePinStatus(chatId, isPinned)
    }

    override suspend fun updateMuteStatus(chatId: String, isMuted: Boolean) {
        chatDao.updateMuteStatus(chatId, isMuted)
    }

    private fun ChatEntity.toDomain(): Chat {
        return Chat(
            id = id,
            name = name,
            participantId = participantId,
            lastMessage = lastMessage,
            time = time,
            unreadCount = unreadCount,
            isPinned = isPinned,
            isMuted = isMuted,
            draftMessage = draftMessage,
            archived = archived,
            chatType = chatType,
            avatarUrl = avatarUrl,
            isSentByMe = false,
            isRead = unreadCount == 0,
            isOnline = false,
            messages = emptyList()
        )
    }
}