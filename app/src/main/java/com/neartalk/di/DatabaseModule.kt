package com.neartalk.di

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import com.neartalk.data.local.ChatDao
import com.neartalk.data.local.ChatDatabase
import com.neartalk.data.local.MessageDao
import com.neartalk.data.local.MessageDatabase
import com.neartalk.data.local.UserDao
import androidx.room.RoomDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideChatDatabase(@ApplicationContext context: Context): ChatDatabase {
        return Room.databaseBuilder(
            context,
            ChatDatabase::class.java,
            "neartalk_chat_db"
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                println("ChatDatabase: Initializing database")
                // Initialize users
                db.execSQL("""
                    INSERT INTO users (id, name, username, phone, email, birthday, status, isPinned, isMuted)
                    VALUES 
                    ('user1', 'Alice', 'alice_user', '1234567890', 'alice@example.com', '1995-01-01', 'online', 1, 0),
                    ('user2', 'Bob', 'bob_user', '0987654321', 'bob@example.com', '1990-02-02', 'offline', 0, 0),
                    ('user3', 'Charlie', 'charlie_user', '1112223334', 'charlie@example.com', '1992-03-03', 'online', 1, 0),
                    ('user4', 'Oleg', 'oleg_user', '4445556667', 'oleg@example.com', '1988-04-04', 'offline', 0, 0),
                    ('user5', 'Sasha', 'sasha_user', '7778889990', 'sasha@example.com', '1993-05-05', 'online', 0, 0),
                    ('current_user', 'Current User', 'current_user', '0000000000', 'user@example.com', '1990-01-01', 'online', 0, 0)
                """)
                // Initialize chats with all non-nullable fields
                db.execSQL("""
                    INSERT INTO chats (id, name, participantId, chatType, lastMessage, time, unreadCount, isPinned, isMuted, draftMessage, archived, avatarUrl)
                    VALUES 
                    ('chat1', 'Alice', 'user1', 'PRIVATE', 'Hey, how are you?', 1694449500, 0, 1, 0, '', 0, NULL),
                    ('chat2', 'Bob', 'user2', 'PRIVATE', 'Let''s meet tomorrow', 1694446200, 12, 0, 0, '', 0, NULL),
                    ('chat3', 'Charlie', 'user3', 'PRIVATE', 'Cool project!', 1694360000, 102, 1, 0, '', 0, NULL),
                    ('chat4', 'Oleg', 'user4', 'PRIVATE', 'See you soon', 1694260000, 0, 0, 0, '', 0, NULL),
                    ('chat5', 'Sasha', 'user5', 'PRIVATE', 'Thanks', 1694170000, 0, 0, 0, '', 0, NULL)
                """)
                println("ChatDatabase: Test data inserted")
            }
        }).build()
    }

    @Provides
    @Singleton
    fun provideMessageDatabase(@ApplicationContext context: Context): MessageDatabase {
        return Room.databaseBuilder(
            context,
            MessageDatabase::class.java,
            "neartalk_message_db"
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                println("MessageDatabase: Initializing database")
                // Initialize messages
                db.execSQL("""
                    INSERT INTO messages (id, text, senderId, receiverId, timestamp, status)
                    VALUES 
                    ('msg1', 'Hey, how are you?', 'user1', 'current_user', 1694449500, 'SENT'),
                    ('msg2', 'Let''s meet tomorrow', 'user2', 'current_user', 1694446200, 'DELIVERED'),
                    ('msg3', 'Cool project!', 'user3', 'current_user', 1694360000, 'SENT'),
                    ('msg4', 'See you soon', 'user4', 'current_user', 1694260000, 'READ'),
                    ('msg5', 'Thanks', 'user5', 'current_user', 1694170000, 'READ')
                """)
                println("MessageDatabase: Test data inserted")
            }
        }).build()
    }

    @Provides
    @Singleton
    fun provideChatDao(db: ChatDatabase): ChatDao {
        return db.chatDao()
    }

    @Provides
    @Singleton
    fun provideUserDao(db: ChatDatabase): UserDao {
        return db.userDao()
    }

    @Provides
    @Singleton
    fun provideMessageDao(db: MessageDatabase): MessageDao {
        return db.messageDao()
    }
}