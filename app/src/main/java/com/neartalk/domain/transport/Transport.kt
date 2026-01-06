package com.neartalk.domain.transport
import com.neartalk.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface Transport {
    suspend fun connect()
    suspend fun disconnect()
    suspend fun sendMessage(message: Message)
    val incomingMessages: Flow<Message>
}