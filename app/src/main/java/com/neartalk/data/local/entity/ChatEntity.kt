package com.neartalk.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.neartalk.domain.model.ChatType

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val participantId: String? = null,
    val chatType: ChatType = ChatType.PRIVATE,
    val lastMessage: String = "",
    val time: Long = 0L,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val draftMessage: String = "",
    val archived: Boolean = false,
    val avatarUrl: String? = null
)