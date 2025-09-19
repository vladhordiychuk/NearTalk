package com.neartalk.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.neartalk.data.local.entity.ChatEntity
import com.neartalk.data.local.entity.UserEntity
import com.neartalk.domain.model.ChatType
import androidx.room.TypeConverter

@Database(entities = [ChatEntity::class, UserEntity::class], version = 1, exportSchema = false)
@TypeConverters(ChatTypeConverter::class)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun userDao(): UserDao
}

class ChatTypeConverter {
    @TypeConverter
    fun fromChatType(type: ChatType): String = type.name

    @TypeConverter
    fun toChatType(value: String): ChatType = ChatType.valueOf(value)
}