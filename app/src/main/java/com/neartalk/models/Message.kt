package com.neartalk.models

data class Message(
    val id: Int,
    val chatId: Int,
    val sender: String,
    val text: String,
    val time: Long,
    val isRead: Boolean,
    val isSentByMe: Boolean
)