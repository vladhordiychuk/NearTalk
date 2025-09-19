package com.neartalk.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.neartalk.data.local.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert
    suspend fun insert(message: MessageEntity)

    @Delete
    suspend fun deleteMessage(message: MessageEntity)

    @Query("""
        SELECT * FROM messages 
        WHERE (senderId = :userId AND receiverId = :receiverId)
           OR (senderId = :receiverId AND receiverId = :userId)
        ORDER BY timestamp ASC
    """)
    fun getMessagesForUser(userId: String, receiverId: String): Flow<List<MessageEntity>>
}