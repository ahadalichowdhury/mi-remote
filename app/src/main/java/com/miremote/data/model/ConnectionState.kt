package com.miremote.data.model

/**
 * Represents the connection state of a remote control session
 */
sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    data class Connected(val device: TvDevice) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}
