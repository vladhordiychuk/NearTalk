package com.neartalk.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val username: String = "",
    val phone: String = "",
    val email: String = "",
    val birthday: String = "",
    val status: String = "",
    val isPinned: Boolean = false,
    val isMuted: Boolean = false
)