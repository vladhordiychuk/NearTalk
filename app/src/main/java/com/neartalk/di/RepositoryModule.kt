package com.neartalk.di

import com.neartalk.data.repository.ChatRepository
import com.neartalk.data.repository.ChatRepositoryImpl
import com.neartalk.data.repository.MessageRepository
import com.neartalk.data.repository.MessageRepositoryImpl
import com.neartalk.data.repository.UserRepository
import com.neartalk.data.repository.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds
    @Singleton
    fun bindMessageRepository(impl: MessageRepositoryImpl): MessageRepository

    @Binds
    @Singleton
    fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
}