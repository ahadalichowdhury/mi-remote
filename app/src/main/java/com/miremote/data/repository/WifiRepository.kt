package com.miremote.data.repository

import com.miremote.data.model.ConnectionState
import com.miremote.data.model.TvDevice
import com.miremote.domain.model.KeyEvent
import com.miremote.services.wifi.WifiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for Wi-Fi ADB connections
 */
class WifiRepository(private val wifiService: WifiService) {
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: Flow<ConnectionState> = _connectionState.asStateFlow()
    
    /**
     * Discover TV devices on the local network
     */
    suspend fun discoverDevices(): List<TvDevice> {
        return wifiService.scanNetwork()
    }
    
    /**
     * Connect to a TV device via IP address
     */
    suspend fun connect(device: TvDevice): Result<Unit> {
        return try {
            _connectionState.value = ConnectionState.Connecting
            val result = wifiService.connect(device.address)
            if (result.isSuccess) {
                _connectionState.value = ConnectionState.Connected(device)
                Result.success(Unit)
            } else {
                _connectionState.value = ConnectionState.Error(result.exceptionOrNull()?.message ?: "Connection failed")
                Result.failure(result.exceptionOrNull() ?: Exception("Connection failed"))
            }
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error(e.message ?: "Unknown error")
            Result.failure(e)
        }
    }
    
    /**
     * Disconnect from the current device
     */
    suspend fun disconnect() {
        wifiService.disconnect()
        _connectionState.value = ConnectionState.Disconnected
    }
    
    /**
     * Send a key event to the connected device
     */
    suspend fun sendKeyEvent(keyEvent: KeyEvent): Result<Unit> {
        return try {
            wifiService.sendKeyEvent(keyEvent.code)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if currently connected
     */
    fun isConnected(): Boolean {
        return _connectionState.value is ConnectionState.Connected
    }
}
