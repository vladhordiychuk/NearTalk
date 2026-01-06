package com.neartalk.domain.model

data class BluetoothDeviceDomain(
    val name: String?,
    val address: String,
    val rssi: Short? = null
)