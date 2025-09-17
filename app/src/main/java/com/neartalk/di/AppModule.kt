package com.neartalk.di

import android.content.Context
import androidx.room.Room
import com.neartalk.data.local.MessageDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMessageDatabase(@ApplicationContext context: Context): MessageDatabase {
        return Room.databaseBuilder(
            context,
            MessageDatabase::class.java,
            "message_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideMessageDao(database: MessageDatabase) = database.messageDao()
}