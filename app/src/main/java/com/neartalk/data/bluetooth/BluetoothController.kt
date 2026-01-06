package com.neartalk.data.bluetooth
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import com.neartalk.domain.model.BluetoothDeviceDomain
import com.neartalk.domain.model.Message
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.UUID

@SuppressLint("MissingPermission")
class AndroidBluetoothController(
    private val context: Context,
    private val bluetoothAdapter: BluetoothAdapter?
) {
    private val SERVICE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    private val _scannedDevices = MutableStateFlow<List<BluetoothDeviceDomain>>(emptyList())
    val scannedDevices: StateFlow<List<BluetoothDeviceDomain>> = _scannedDevices.asStateFlow()
    private val _incomingMessages = MutableSharedFlow<Message>()
    val incomingMessages: SharedFlow<Message> = _incomingMessages.asSharedFlow()
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    private val _connectedDevice = MutableStateFlow<BluetoothDeviceDomain?>(null)
    val connectedDevice: StateFlow<BluetoothDeviceDomain?> = _connectedDevice.asStateFlow()

    private var serverSocket: BluetoothServerSocket? = null
    private var clientSocket: BluetoothSocket? = null

    private val foundDeviceReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE) // FIXED: Додано RSSI
                    device?.let { d ->
                        val newDevice = BluetoothDeviceDomain(d.name, d.address, rssi)
                        _scannedDevices.update { devices ->
                            if (newDevice in devices) devices else devices + newDevice
                        }
                    }
                }
            }
        }
    }

    fun startDiscovery() {
        if (bluetoothAdapter == null) return
        context.registerReceiver(
            foundDeviceReceiver,
            IntentFilter(BluetoothDevice.ACTION_FOUND)
        )
        updatePairedDevices()
        bluetoothAdapter.startDiscovery()
    }

    fun stopDiscovery() {
        if (bluetoothAdapter == null) return
        bluetoothAdapter.cancelDiscovery()
        try {
            context.unregisterReceiver(foundDeviceReceiver)
        } catch (e: Exception) {
            Log.e("BT", "Unregister error: ${e.message}")
        }
    }

    private fun updatePairedDevices() {
        bluetoothAdapter?.bondedDevices?.forEach { d ->
            val newDevice = BluetoothDeviceDomain(d.name, d.address) // RSSI null для paired
            _scannedDevices.update { devices ->
                if (newDevice in devices) devices else devices + newDevice
            }
        }
    }

    fun startServer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                serverSocket = bluetoothAdapter?.listenUsingRfcommWithServiceRecord("NearTalk", SERVICE_UUID)
                Log.d("BT", "Server waiting for connection...")
                clientSocket = serverSocket?.accept()
                serverSocket?.close()
                val remote = clientSocket?.remoteDevice
                _connectedDevice.value = BluetoothDeviceDomain(remote?.name, remote?.address ?: "")
                handleConnection()
            } catch (e: IOException) {
                Log.e("BT", "Server error: ${e.message}")
            }
        }
    }

    fun connectToDevice(address: String) {
        if (bluetoothAdapter == null) return
        stopDiscovery()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                closeConnection()
                val device = bluetoothAdapter.getRemoteDevice(address)
                clientSocket = device.createRfcommSocketToServiceRecord(SERVICE_UUID)
                clientSocket?.connect()
                _connectedDevice.value = BluetoothDeviceDomain(device.name, device.address)
                handleConnection()
            } catch (e: IOException) {
                Log.e("BT", "Connection error: ${e.message}")
                closeConnection()
            }
        }
    }

    private suspend fun handleConnection() {
        _isConnected.value = true
        Log.d("BT", "Connected!")
        clientSocket?.let { socket ->
            val inputStream = socket.inputStream
            val reader = BufferedReader(InputStreamReader(inputStream))
            while (socket.isConnected) {
                try {
                    val line = reader.readLine() ?: break
                    try {
                        val message = Json.decodeFromString(Message.serializer(), line)
                        _incomingMessages.emit(message)
                    } catch (e: Exception) {
                        Log.e("BT", "JSON parse error: ${e.message}")
                    }
                } catch (e: IOException) {
                    Log.e("BT", "Socket disconnected: ${e.message}")
                    break
                }
            }
        }
        closeConnection()
    }

    suspend fun sendMessage(message: Message): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (clientSocket == null) return@withContext false
                // Екрануємо переноси рядків, щоб readLine на приймачі не розірвав JSON
                val safeText = message.text.replace("\n", "\\n")
                val safeMessage = message.copy(text = safeText)

                val json = Json.encodeToString(Message.serializer(), safeMessage)
                clientSocket?.outputStream?.write((json + "\n").toByteArray())
                clientSocket?.outputStream?.flush()
                true
            } catch (e: IOException) {
                // ...
                false
            }
        }
    }

    fun closeConnection() {
        try {
            clientSocket?.close()
            serverSocket?.close()
        } catch (e: Exception) {
            Log.e("BT", "Close error: ${e.message}")
        }
        clientSocket = null
        serverSocket = null
        _isConnected.value = false
        _connectedDevice.value = null
    }

    fun getMyAddress(): String = bluetoothAdapter?.address ?: "me"
}