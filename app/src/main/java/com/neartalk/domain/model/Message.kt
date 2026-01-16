package com.neartalk.domain.model

import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class MessageType {
    TEXT,
    NAME_UPDATE,
    DELIVERY_ACK,
    DEVICE_ANNOUNCE,
    PEER_LIST_REQUEST,
    PEER_LIST_RESPONSE
}

@Serializable
data class Message(
    val id: String = UUID.randomUUID().toString(),
    val text: String = "",
    val senderId: String = "",
    val senderName: String = "User",
    val receiverId: String = "ALL",
    val timestamp: Long = System.currentTimeMillis(),
    val ttl: Int = 7,
    val contentHash: String = "",
    val type: MessageType = MessageType.TEXT,
    val status: String = "sent"
) {
    fun formattedTimestamp(): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }
}