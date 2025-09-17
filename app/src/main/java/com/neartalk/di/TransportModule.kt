package com.neartalk.di

import android.bluetooth.BluetoothAdapter
import android.content.Context
import com.neartalk.data.bluetooth.BluetoothTransport
import com.neartalk.domain.transport.Transport
import com.neartalk.domain.transport.TransportManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TransportModule {

    @Provides
    @Singleton
    fun provideBluetoothAdapter(): BluetoothAdapter {
        return BluetoothAdapter.getDefaultAdapter()
    }

    @Provides
    @Singleton
    fun provideBluetoothTransport(
        adapter: BluetoothAdapter,
        @ApplicationContext context: Context
    ): Transport {
        return BluetoothTransport(adapter, context)
    }

    @Provides
    @Singleton
    fun provideTransportManager(bluetoothTransport: Transport): TransportManager {
        return TransportManager(bluetoothTransport)
    }
}
