package com.neartalk.data.repository

import com.neartalk.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserById(userId: String): Flow<User?>
    fun getAllUsers(): Flow<List<User>>
    suspend fun deleteUser(userId: String)
    suspend fun updatePinStatus(userId: String, isPinned: Boolean)
}