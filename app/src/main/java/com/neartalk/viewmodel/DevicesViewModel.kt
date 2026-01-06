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

@HiltViewModel
class DevicesViewModel @Inject constructor(
    private val bluetoothController: AndroidBluetoothController
) : ViewModel() {
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
                        String.format("%.1f m", Math.pow(10.0, ((-69 - rssi) / (10.0 * 2.0))))
                    } else "Поблизу"
                    DeviceItem(
                        id = domain.address,
                        name = domain.name ?: "Невідомий пристрій",
                        distance = dist,
                        signalStrength = domain.rssi?.toInt()?.plus(100) ?: 0 // Приблизно
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
        _isScanning.value = true
        _scanState.value = ScanState.Scanning
        bluetoothController.startDiscovery()
    }

    fun stopScan() {
        _isScanning.value = false
        _scanState.value = ScanState.Idle
        bluetoothController.stopDiscovery()
    }

    fun connectToDevice(deviceId: String) {
        stopScan()
    }

    override fun onCleared() {
        super.onCleared()
        stopScan()
    }

    sealed class ScanState {
        object Idle : ScanState()
        object Scanning : ScanState()
        data class Success(val devicesFound: Int) : ScanState()
        data class Error(val message: String) : ScanState()
    }
}