package com.neartalk.domain.model

data class Chat(
    val id: Int,
    val name: String,
    val lastMessage: String,
    val time: Long,
    val isRead: Boolean,
    val isSentByMe: Boolean,
    val unreadCount: Int,
    val isPinned: Boolean = false,
    val isOnline: Boolean = false,
    val muted: Boolean = false,
    val archived: Boolean = false,
    val chatType: ChatType = ChatType.PRIVATE,
    val avatarUrl: String? = null,
    val draftMessage: String = "",
    val messages: List<Message> = emptyList()
)