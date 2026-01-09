package com.neartalk.domain.model

data class BluetoothDeviceDomain(
    val name: String?,
    val address: String,
    val rssi: Short? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BluetoothDeviceDomain

        return address == other.address
    }

    override fun hashCode(): Int {
        return address.hashCode()
    }
}