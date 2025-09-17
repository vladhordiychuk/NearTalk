package com.neartalk.di

import com.neartalk.data.local.MessageDao
import com.neartalk.data.repository.MessageRepository
import com.neartalk.data.repository.MessageRepositoryImpl
import com.neartalk.domain.transport.Transport
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMessageRepository(
        impl: MessageRepositoryImpl
    ): MessageRepository
}
