package com.miremote.presentation.remote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miremote.data.model.ConnectionState
import com.miremote.data.model.TvDevice
import com.miremote.data.repository.BluetoothRepository
import com.miremote.data.repository.WifiRepository
import com.miremote.domain.model.KeyEvent
import com.miremote.domain.usecase.SendKeyEventUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Remote Control screen
 */
class RemoteViewModel(
    private val sendKeyEventUseCase: SendKeyEventUseCase,
    private val wifiRepository: WifiRepository,
    private val bluetoothRepository: BluetoothRepository
) : ViewModel() {
    
    private var connectedDevice: TvDevice? = null
    private var connectionType: TvDevice.ConnectionType = TvDevice.ConnectionType.WIFI
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    /**
     * Sets the connected device
     */
    fun setConnectedDevice(device: TvDevice) {
        connectedDevice = device
        connectionType = device.type
        
        // Observe connection state
        viewModelScope.launch {
            when (connectionType) {
                TvDevice.ConnectionType.WIFI -> {
                    wifiRepository.connectionState.collect { state ->
                        _connectionState.value = state
                    }
                }
                TvDevice.ConnectionType.BLUETOOTH -> {
                    bluetoothRepository.connectionState.collect { state ->
                        _connectionState.value = state
                    }
                }
            }
        }
    }
    
    /**
     * Sends a key event to the connected device
     */
    fun sendKeyEvent(keyEvent: KeyEvent) {
        if (connectedDevice == null) {
            _errorMessage.value = "Not connected to any device"
            return
        }
        
        viewModelScope.launch {
            _errorMessage.value = null
            
            val result = sendKeyEventUseCase.execute(keyEvent, connectionType)
            result.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Failed to send key event"
            }
        }
    }
    
    /**
     * Disconnects from the current device
     */
    fun disconnect() {
        viewModelScope.launch {
            when (connectionType) {
                TvDevice.ConnectionType.WIFI -> wifiRepository.disconnect()
                TvDevice.ConnectionType.BLUETOOTH -> bluetoothRepository.disconnect()
            }
            connectedDevice = null
        }
    }
    
    /**
     * Gets the connected device name
     */
    fun getDeviceName(): String {
        return connectedDevice?.name ?: "Unknown Device"
    }
    
    /**
     * Clears error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}
