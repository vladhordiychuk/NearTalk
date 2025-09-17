package com.neartalk.data.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.neartalk.domain.model.Message
import com.neartalk.domain.transport.Transport
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID
import android.annotation.SuppressLint

fun hasBluetoothConnectPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.BLUETOOTH_CONNECT
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

class BluetoothTransport(
    private val bluetoothAdapter: BluetoothAdapter,
    private val context: Context
) : Transport {

    private var socket: BluetoothSocket? = null
    private val incomingMessagesFlow = MutableSharedFlow<Message>(replay = 0)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val serviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    @SuppressLint("MissingPermission")
    override suspend fun connect() {
        withContext(Dispatchers.IO) {
            if (!hasBluetoothConnectPermission(context)) {
                throw SecurityException("BLUETOOTH_CONNECT permission is required")
            }

            val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
            val targetDevice: BluetoothDevice? = pairedDevices.firstOrNull()

            if (targetDevice != null) {
                bluetoothAdapter.cancelDiscovery()
                socket = targetDevice.createRfcommSocketToServiceRecord(serviceUUID)
                socket?.connect()
                startListening()
            } else {
                throw IllegalStateException("No paired devices found")
            }
        }
    }

    override suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            try {
                socket?.close()
            } catch (_: Exception) {}
            socket = null
            scope.coroutineContext.cancelChildren()
        }
    }

    override suspend fun sendMessage(message: Message) {
        withContext(Dispatchers.IO) {
            socket?.let { s ->
                val outputStream = s.outputStream
                val serializedMessage = Json.encodeToString(Message.serializer(), message) + "\n"
                outputStream.write(serializedMessage.toByteArray())
                outputStream.flush()
            } ?: throw IllegalStateException("Socket is not connected")
        }
    }

    override val incomingMessages: Flow<Message> = incomingMessagesFlow

    private fun startListening() {
        scope.launch {
            socket?.let { s ->
                val reader = BufferedReader(InputStreamReader(s.inputStream))
                try {
                    while (isActive) {
                        val line = reader.readLine() ?: break
                        try {
                            val message = Json.decodeFromString(Message.serializer(), line)
                            incomingMessagesFlow.emit(message)
                        } catch (_: Exception) {
                        }
                    }
                } catch (_: Exception) {
                }
            }
        }
    }
}
