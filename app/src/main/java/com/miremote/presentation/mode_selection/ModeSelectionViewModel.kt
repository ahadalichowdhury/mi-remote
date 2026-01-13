package com.miremote.presentation.mode_selection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.miremote.data.model.TvDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Mode Selection screen
 */
class ModeSelectionViewModel : ViewModel() {
    
    private val _selectedMode = MutableStateFlow<TvDevice.ConnectionType?>(null)
    val selectedMode: StateFlow<TvDevice.ConnectionType?> = _selectedMode
    
    /**
     * Selects Wi-Fi mode
     */
    fun selectWifiMode() {
        _selectedMode.value = TvDevice.ConnectionType.WIFI
    }
    
    /**
     * Selects Bluetooth mode
     */
    fun selectBluetoothMode() {
        _selectedMode.value = TvDevice.ConnectionType.BLUETOOTH
    }
    
    /**
     * Clears the selected mode after navigation
     */
    fun onNavigationComplete() {
        _selectedMode.value = null
    }
}
