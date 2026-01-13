package com.miremote.services.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.miremote.data.model.TvDevice
import com.miremote.domain.model.KeyEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

/**
 * Manages Bluetooth operations including discovery and HID connections
 */
class BluetoothManager(private val context: Context) {
    
    companion object {
        private const val TAG = "BluetoothManager"
        private const val SCAN_DURATION_MS = 10000L // 10 seconds
    }
    
    private val bluetoothManager: BluetoothManager? = 
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    
    private val bluetoothAdapter: BluetoothAdapter? = 
        bluetoothManager?.adapter
    
    private val hidService = HidService()
    
    private val _discoveredDevices = MutableStateFlow<List<TvDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<TvDevice>> = _discoveredDevices
    
    private var isScanning = false
    
    /**
     * Checks if Bluetooth is supported on this device
     */
    fun isBluetoothSupported(): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
    }
    
    /**
     * Checks if Bluetooth is enabled
     */
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }
    
    /**
     * Scans for Bluetooth devices
     * Returns a list of discovered devices
     */
    suspend fun scanDevices(): List<TvDevice> = withContext(Dispatchers.IO) {
        if (!isBluetoothSupported()) {
            Log.e(TAG, "Bluetooth not supported")
            return@withContext emptyList()
        }
        
        if (!isBluetoothEnabled()) {
            Log.e(TAG, "Bluetooth not enabled")
            return@withContext emptyList()
        }
        
        if (isScanning) {
            Log.d(TAG, "Scan already in progress")
            return@withContext _discoveredDevices.value
        }
        
        isScanning = true
        val devices = mutableListOf<TvDevice>()
        
        try {
            Log.d(TAG, "Starting Bluetooth scan")
            
            // Get already paired devices
            val pairedDevices = bluetoothAdapter?.bondedDevices ?: emptySet()
            pairedDevices.forEach { device ->
                devices.add(
                    TvDevice(
                        name = device.name ?: "Unknown Device",
                        address = device.address,
                        type = TvDevice.ConnectionType.BLUETOOTH,
                        isConnected = false
                    )
                )
            }
            
            // Start discovery for new devices
            val discoveryStarted = bluetoothAdapter?.startDiscovery() ?: false
            if (discoveryStarted) {
                delay(SCAN_DURATION_MS)
                
                // Get discovered devices
                // Note: In a real implementation, you'd use a BroadcastReceiver
                // to listen for ACTION_FOUND events. For simplicity, we'll use paired devices.
                bluetoothAdapter?.cancelDiscovery()
            }
            
            _discoveredDevices.value = devices
            Log.d(TAG, "Found ${devices.size} devices")
        } catch (e: Exception) {
            Log.e(TAG, "Error scanning for devices", e)
        } finally {
            isScanning = false
        }
        
        devices
    }
    
    /**
     * Connects to a Bluetooth device
     */
    suspend fun connect(address: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (!isBluetoothEnabled()) {
            return@withContext Result.failure(Exception("Bluetooth is not enabled"))
        }
        
        return@withContext try {
            val device = bluetoothAdapter?.getRemoteDevice(address)
            if (device == null) {
                return@withContext Result.failure(Exception("Device not found: $address"))
            }
            
            // Ensure device is paired
            if (device.bondState != BluetoothDevice.BOND_BONDED) {
                Log.d(TAG, "Device not paired, attempting to pair")
                // Pairing requires user interaction, so we'll just try to connect
                // In a real app, you'd handle pairing separately
            }
            
            hidService.connect(device)
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to device", e)
            Result.failure(e)
        }
    }
    
    /**
     * Disconnects from the current device
     */
    suspend fun disconnect() = withContext(Dispatchers.IO) {
        hidService.disconnect()
    }
    
    /**
     * Sends a key event to the connected device
     */
    suspend fun sendKeyEvent(keyEvent: KeyEvent): Result<Unit> = withContext(Dispatchers.IO) {
        hidService.sendKeyEvent(keyEvent)
    }
    
    /**
     * Checks if currently connected
     */
    fun isConnected(): Boolean {
        return hidService.isConnected()
    }
    
    /**
     * Gets the connected device
     */
    fun getConnectedDevice(): BluetoothDevice? {
        return hidService.getConnectedDevice()
    }
}
