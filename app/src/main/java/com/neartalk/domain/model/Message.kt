package com.neartalk.domain.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.serialization.Serializable

enum class MessageStatus {
    SENT, DELIVERED, READ
}

@Serializable
data class Message(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val senderId: String,
    val receiverId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = MessageStatus.SENT.name.lowercase()
) {
    fun formattedTimestamp(): String {
        val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }
}