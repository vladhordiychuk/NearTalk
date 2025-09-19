package com.neartalk.data.repository

import com.neartalk.data.local.MessageDao
import com.neartalk.domain.model.Message
import com.neartalk.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao
) : MessageRepository {

    override fun getMessagesForUser(userId: String, receiverId: String): Flow<List<Message>> {
        return messageDao.getMessagesForUser(userId, receiverId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertMessage(message: Message) {
        messageDao.insert(message.toEntity())
    }

    override suspend fun deleteMessage(message: Message) {
        messageDao.deleteMessage(message.toEntity())
    }

    private fun MessageEntity.toDomain(): Message {
        return Message(
            id = id,
            text = text,
            senderId = senderId,
            receiverId = receiverId,
            timestamp = timestamp,
            status = status
        )
    }

    private fun Message.toEntity(): MessageEntity {
        return MessageEntity(
            id = id,
            text = text,
            senderId = senderId,
            receiverId = receiverId,
            timestamp = timestamp,
            status = status
        )
    }
}