package com.miremote.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Represents a TV device that can be controlled
 * @param name Display name of the device
 * @param address IP address (for Wi-Fi) or MAC address (for Bluetooth)
 * @param type Connection type (WiFi or Bluetooth)
 * @param isConnected Whether the device is currently connected
 */
@Parcelize
data class TvDevice(
    val name: String,
    val address: String,
    val type: ConnectionType,
    val isConnected: Boolean = false
) : Parcelable {
    enum class ConnectionType {
        WIFI,
        BLUETOOTH
    }
}
