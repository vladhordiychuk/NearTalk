package com.neartalk.domain.model

import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MessageType {
    TEXT,
    NAME_UPDATE
}

enum class MessageStatus {
    SENT,
    RECEIVED,
    DELIVERED,
    READ,
    FAILED,
}

@Serializable
data class Message(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val timestamp: Long = 0L,
    val status: String = "",
    val type: MessageType = MessageType.TEXT // Нове поле
) {
    fun formattedTimestamp(): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }
}