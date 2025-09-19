package com.neartalk.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.neartalk.data.local.entity.MessageEntity

@Database(entities = [MessageEntity::class], version = 1, exportSchema = false)
abstract class MessageDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}