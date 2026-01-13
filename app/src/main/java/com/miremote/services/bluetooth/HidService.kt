package com.miremote.services.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import com.miremote.domain.model.KeyEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

/**
 * Service for managing Bluetooth HID connections
 * Note: This is a simplified implementation. Full HID device emulation
 * requires system-level permissions and may not work on all devices.
 */
class HidService {
    
    companion object {
        private const val TAG = "HidService"
        // SPP UUID for serial communication (fallback)
        private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
    
    private var socket: BluetoothSocket? = null
    private var connectedDevice: BluetoothDevice? = null
    
    /**
     * Connects to a Bluetooth device
     * Note: For HID, the device must support the HID profile
     */
    suspend fun connect(device: BluetoothDevice): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Connecting to device: ${device.name} (${device.address})")
            
            // Try to create a socket
            // Note: For HID, we would typically use BluetoothHidDevice API on API 28+
            // For compatibility, we'll use RFCOMM socket
            val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            
            socket.connect()
            
            this@HidService.socket = socket
            this@HidService.connectedDevice = device
            
            Log.d(TAG, "Connected successfully")
            Result.success(Unit)
        } catch (e: IOException) {
            Log.e(TAG, "Error connecting to device", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error connecting", e)
            Result.failure(e)
        }
    }
    
    /**
     * Disconnects from the current device
     */
    suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            socket?.close()
            socket = null
            connectedDevice = null
            Log.d(TAG, "Disconnected")
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting", e)
        }
    }
    
    /**
     * Sends a key event as HID report
     * Note: This is a simplified implementation. Full HID requires proper
     * HID descriptor and report protocol implementation.
     */
    suspend fun sendKeyEvent(keyEvent: KeyEvent): Result<Unit> = withContext(Dispatchers.IO) {
        if (socket == null || !socket!!.isConnected) {
            return@withContext Result.failure(Exception("Not connected"))
        }
        
        return@withContext try {
            val keycode = HidProfile.getHidKeycode(keyEvent)
            if (keycode == null) {
                return@withContext Result.failure(Exception("Unsupported key event: $keyEvent"))
            }
            
            val usagePage = HidProfile.getUsagePage(keyEvent)
            val report = if (usagePage == HidProfile.USAGE_PAGE_KEYBOARD) {
                HidProfile.createKeyboardReport(keycode)
            } else {
                HidProfile.createConsumerReport(keycode)
            }
            
            // Send key press
            socket?.outputStream?.write(report)
            socket?.outputStream?.flush()
            
            // Small delay
            kotlinx.coroutines.delay(50)
            
            // Send key release (null report)
            val nullReport = HidProfile.createNullReport(usagePage)
            socket?.outputStream?.write(nullReport)
            socket?.outputStream?.flush()
            
            Log.d(TAG, "Sent key event: $keyEvent")
            Result.success(Unit)
        } catch (e: IOException) {
            Log.e(TAG, "Error sending key event", e)
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error sending key event", e)
            Result.failure(e)
        }
    }
    
    /**
     * Checks if currently connected
     */
    fun isConnected(): Boolean {
        return socket != null && socket!!.isConnected
    }
    
    /**
     * Gets the connected device
     */
    fun getConnectedDevice(): BluetoothDevice? {
        return connectedDevice
    }
}
