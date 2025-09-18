package com.neartalk.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.neartalk.data.local.MessageDao
import com.neartalk.data.local.MessageDatabase
import com.neartalk.data.local.UserDao
import com.neartalk.data.local.UserDatabase
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
    fun provideMessageDatabase(@ApplicationContext context: Context): MessageDatabase {
        return Room.databaseBuilder(
            context,
            MessageDatabase::class.java,
            "message_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideMessageDao(db: MessageDatabase): MessageDao {
        return db.messageDao()
    }

    @Provides
    @Singleton
    fun provideUserDatabase(@ApplicationContext context: Context): UserDatabase {
        return Room.databaseBuilder(
            context,
            UserDatabase::class.java,
            "user_db"
        ).addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                println("Database: Initializing users table")
                db.execSQL("""
                    INSERT INTO users (id, name, username, phone, email, birthday, status)
                    VALUES 
                    (1, 'Alice', 'alice_user', '1234567890', 'alice@example.com', '1995-01-01', 'online'),
                    (2, 'Bob', 'bob_user', '0987654321', 'bob@example.com', '1990-02-02', 'offline'),
                    (3, 'Charlie', 'charlie_user', '1112223334', 'charlie@example.com', '1992-03-03', 'online'),
                    (4, 'Oleg', 'oleg_user', '4445556667', 'oleg@example.com', '1988-04-04', 'offline'),
                    (5, 'Sasha', 'sasha_user', '7778889990', 'sasha@example.com', '1993-05-05', 'online'),
                    (0, 'Current User', 'current_user', '0000000000', 'user@example.com', '1990-01-01', 'online')
                """)
                println("Database: Test users inserted")
                val cursor = db.query("SELECT COUNT(*) FROM users")
                if (cursor.moveToFirst()) {
                    val count = cursor.getInt(0)
                    println("Database: Users count after insert: $count")
                }
                cursor.close()
            }
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                println("Database: Opened user_db")
            }
        })
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideUserDao(db: UserDatabase): UserDao {
        return db.userDao()
    }
}