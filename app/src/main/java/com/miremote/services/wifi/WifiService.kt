package com.miremote.services.wifi

import android.content.Context
import android.util.Log
import com.miremote.data.model.TvDevice

/**
 * Service for managing Wi-Fi ADB connections
 */
class WifiService(private val context: Context) {
    
    companion object {
        private const val TAG = "WifiService"
    }
    
    private val networkScanner = NetworkScanner(context)
    private val adbClient = AdbClient()
    
    /**
     * Scans the network for TV devices
     */
    suspend fun scanNetwork(): List<TvDevice> {
        Log.d(TAG, "Starting network scan")
        return try {
            networkScanner.scanNetwork()
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning network", e)
            emptyList()
        }
    }
    
    /**
     * Connects to a TV device via IP address
     */
    suspend fun connect(ip: String): Result<Unit> {
        Log.d(TAG, "Connecting to device at $ip")
        return adbClient.connect(ip)
    }
    
    /**
     * Disconnects from the current device
     */
    suspend fun disconnect() {
        Log.d(TAG, "Disconnecting")
        adbClient.disconnect()
    }
    
    /**
     * Sends a key event to the connected device
     */
    suspend fun sendKeyEvent(keyCode: Int): Result<Unit> {
        Log.d(TAG, "Sending key event: $keyCode")
        return adbClient.sendKeyEvent(keyCode)
    }
    
    /**
     * Checks if currently connected
     */
    fun isConnected(): Boolean {
        return adbClient.isConnected()
    }
    
    /**
     * Gets the connected device IP
     */
    fun getConnectedIp(): String? {
        return adbClient.getConnectedIp()
    }
}
