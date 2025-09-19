package com.neartalk.data.local

import androidx.room.Dao
import androidx.room.Query
import com.neartalk.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    fun getUserById(userId: String): Flow<UserEntity?>

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String)

    @Query("UPDATE users SET isPinned = :isPinned WHERE id = :userId")
    suspend fun updatePinStatus(userId: String, isPinned: Boolean)
}