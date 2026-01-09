package com.neartalk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neartalk.data.bluetooth.AndroidBluetoothController
import com.neartalk.ui.screens.DeviceItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class DevicesViewModel @Inject constructor(
    private val bluetoothController: AndroidBluetoothController
) : ViewModel() {

    companion object {
        private const val TAG = "DevicesViewModel"
    }

    private val _availableDevices = MutableStateFlow<List<DeviceItem>>(emptyList())
    val availableDevices: StateFlow<List<DeviceItem>> = _availableDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanState = MutableStateFlow<ScanState>(ScanState.Idle)
    val scanState: StateFlow<ScanState> = _scanState.asStateFlow()

    init {
        viewModelScope.launch {
            bluetoothController.scannedDevices.collect { domainDevices ->
                val uiDevices = domainDevices.map { domain ->
                    val dist = if (domain.rssi != null) {
                        val rssi = domain.rssi.toInt()
                        String.format("%.1f m", Math.pow(10.0, ((-69 - rssi) / (20.0))))
                    } else "Unknown"

                    DeviceItem(
                        id = domain.address,
                        name = domain.name ?: "Unknown Device",
                        distance = dist,
                        signalStrength = domain.rssi?.toInt()?.plus(100) ?: 0
                    )
                }
                _availableDevices.value = uiDevices

                if (uiDevices.isNotEmpty()) {
                    _scanState.value = ScanState.Success(uiDevices.size)
                }
            }
        }
    }

    fun startScan() {
        Log.d(TAG, "Starting scan")
        _isScanning.value = true
        _scanState.value = ScanState.Scanning
        bluetoothController.startDiscovery()
    }

    fun stopScan() {
        Log.d(TAG, "Stopping scan")
        _isScanning.value = false
        _scanState.value = ScanState.Idle
        bluetoothController.stopDiscovery()
    }

    fun connectToDevice(deviceId: String) {
        Log.d(TAG, "Device selected: $deviceId")
        stopScan()
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared - stopping scan")
        stopScan()
    }

    sealed class ScanState {
        object Idle : ScanState()
        object Scanning : ScanState()
        data class Success(val devicesFound: Int) : ScanState()
        data class Error(val message: String) : ScanState()
    }
}