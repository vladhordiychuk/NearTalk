package com.neartalk.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val text: String,
    val senderId: String,
    val receiverId: String,
    val timestamp: Long,
    val status: String,
)

