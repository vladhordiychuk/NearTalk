package com.neartalk.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.neartalk.data.local.entity.MessageEntity

@Dao
interface MessageDao {
    @Insert
    suspend fun insert(message: MessageEntity)

    @Query("""
    SELECT * FROM messages 
    WHERE (senderId = :userId AND receiverId = :receiverId)
       OR (senderId = :receiverId AND receiverId = :userId)
    ORDER BY timestamp ASC
""")
    fun getMessagesForUser(userId: String, receiverId: String): Flow<List<MessageEntity>>
}