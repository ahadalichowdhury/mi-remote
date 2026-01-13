package com.miremote.presentation.remote

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.miremote.data.repository.BluetoothRepository
import com.miremote.data.repository.WifiRepository
import com.miremote.domain.usecase.SendKeyEventUseCase
import com.miremote.services.bluetooth.BluetoothManager
import com.miremote.services.wifi.WifiService

/**
 * Factory for creating RemoteViewModel with dependencies
 */
class RemoteViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RemoteViewModel::class.java)) {
            // Create dependencies
            val wifiService = WifiService(context)
            val bluetoothManager = BluetoothManager(context)
            val wifiRepository = WifiRepository(wifiService)
            val bluetoothRepository = BluetoothRepository(bluetoothManager)
            
            val sendKeyEventUseCase = SendKeyEventUseCase(
                wifiRepository,
                bluetoothRepository
            )
            
            return RemoteViewModel(
                sendKeyEventUseCase,
                wifiRepository,
                bluetoothRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
