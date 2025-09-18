package com.neartalk.data.repository

import com.neartalk.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserById(userId: Int): Flow<User?>
    fun getAllUsers(): Flow<List<User>>
}
