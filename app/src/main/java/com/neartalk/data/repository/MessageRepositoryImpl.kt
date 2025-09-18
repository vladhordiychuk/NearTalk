package com.neartalk.data.repository

import com.neartalk.data.local.MessageDao
import com.neartalk.domain.model.Message
import com.neartalk.domain.transport.Transport
import com.neartalk.data.local.entity.MessageEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class MessageRepositoryImpl @Inject constructor(
    private val messageDao: MessageDao,
    private val transport: Transport,
) : MessageRepository {
    override fun getMessages(userId: String, receiverId: String): Flow<List<Message>> {
        return messageDao.getMessagesForUser(userId, receiverId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun sendMessage(message: Message) {
        messageDao.insert(message.toEntity())
        transport.sendMessage(message)
    }

    init {
        CoroutineScope(Dispatchers.IO).launch {
            transport.incomingMessages.collect { incoming ->
                messageDao.insert(incoming.toEntity())
            }
        }
    }

    private fun Message.toEntity() = MessageEntity(
        id = id,
        text = text,
        senderId = senderId,
        receiverId = receiverId,
        timestamp = timestamp,
        status = status
    )

    private fun MessageEntity.toDomain() = Message(
        id = id,
        text = text,
        senderId = senderId,
        receiverId = receiverId,
        timestamp = timestamp,
        status = status,
    )
}