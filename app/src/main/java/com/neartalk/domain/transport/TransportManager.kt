package com.neartalk.domain.transport

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TransportManager(
    private val bluetoothTransport: Transport
) {
    var activeTransport: Transport = bluetoothTransport
        private set

    suspend fun connect() {
        withContext(Dispatchers.IO) {
            activeTransport.connect()
        }
    }

    suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            activeTransport.disconnect()
        }
    }
}