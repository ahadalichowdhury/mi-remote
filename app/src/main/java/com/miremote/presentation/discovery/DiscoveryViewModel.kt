package com.miremote.presentation.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miremote.data.model.ConnectionState
import com.miremote.data.model.TvDevice
import com.miremote.data.repository.BluetoothRepository
import com.miremote.data.repository.WifiRepository
import com.miremote.domain.usecase.ConnectBluetoothUseCase
import com.miremote.domain.usecase.ConnectWifiUseCase
import com.miremote.domain.usecase.DiscoverTvDevicesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Discovery screen
 */
class DiscoveryViewModel(
    private val discoverTvDevicesUseCase: DiscoverTvDevicesUseCase,
    private val connectWifiUseCase: ConnectWifiUseCase,
    private val connectBluetoothUseCase: ConnectBluetoothUseCase,
    private val wifiRepository: WifiRepository,
    private val bluetoothRepository: BluetoothRepository
) : ViewModel() {
    
    private var currentMode: TvDevice.ConnectionType = TvDevice.ConnectionType.WIFI
    
    private val _devices = MutableStateFlow<List<TvDevice>>(emptyList())
    val devices: StateFlow<List<TvDevice>> = _devices
    
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    private val _navigateToRemote = MutableStateFlow<TvDevice?>(null)
    val navigateToRemote: StateFlow<TvDevice?> = _navigateToRemote
    
    init {
        // Observe connection state from repositories
        viewModelScope.launch {
            wifiRepository.connectionState.collect { state ->
                if (currentMode == TvDevice.ConnectionType.WIFI) {
                    _connectionState.value = state
                }
            }
        }
        
        viewModelScope.launch {
            bluetoothRepository.connectionState.collect { state ->
                if (currentMode == TvDevice.ConnectionType.BLUETOOTH) {
                    _connectionState.value = state
                }
            }
        }
    }
    
    /**
     * Sets the connection mode and starts scanning
     */
    fun setMode(mode: TvDevice.ConnectionType) {
        currentMode = mode
        scanDevices()
    }
    
    /**
     * Scans for devices
     */
    fun scanDevices() {
        viewModelScope.launch {
            _isScanning.value = true
            _errorMessage.value = null
            
            try {
                val discoveredDevices = discoverTvDevicesUseCase.execute(currentMode)
                _devices.value = discoveredDevices
                
                if (discoveredDevices.isEmpty()) {
                    _errorMessage.value = "No devices found"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Error scanning for devices"
                _devices.value = emptyList()
            } finally {
                _isScanning.value = false
            }
        }
    }
    
    /**
     * Connects to a device
     */
    fun connectToDevice(device: TvDevice) {
        viewModelScope.launch {
            _errorMessage.value = null
            
            val result = when (currentMode) {
                TvDevice.ConnectionType.WIFI -> connectWifiUseCase.execute(device)
                TvDevice.ConnectionType.BLUETOOTH -> connectBluetoothUseCase.execute(device)
            }
            
            result.onSuccess {
                _navigateToRemote.value = device
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Connection failed"
            }
        }
    }
    
    /**
     * Connects to a device using manual IP address
     */
    fun connectToManualIp(ip: String) {
        if (currentMode != TvDevice.ConnectionType.WIFI) {
            _errorMessage.value = "Manual IP only works with Wi-Fi mode"
            return
        }
        
        val device = TvDevice(
            name = "Manual IP: $ip",
            address = ip,
            type = TvDevice.ConnectionType.WIFI,
            isConnected = false
        )
        
        connectToDevice(device)
    }
    
    /**
     * Clears error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Called after navigation to remote screen
     */
    fun onNavigationComplete() {
        _navigateToRemote.value = null
    }
}
