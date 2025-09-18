package com.neartalk.di

import android.content.Context
import androidx.room.Room
import com.neartalk.data.local.MessageDao
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

}
