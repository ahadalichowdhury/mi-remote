package com.miremote.presentation.discovery

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.miremote.data.repository.BluetoothRepository
import com.miremote.data.repository.WifiRepository
import com.miremote.domain.usecase.ConnectBluetoothUseCase
import com.miremote.domain.usecase.ConnectWifiUseCase
import com.miremote.domain.usecase.DiscoverTvDevicesUseCase
import com.miremote.services.bluetooth.BluetoothManager
import com.miremote.services.wifi.WifiService

/**
 * Factory for creating DiscoveryViewModel with dependencies
 */
class DiscoveryViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiscoveryViewModel::class.java)) {
            // Create dependencies
            val wifiService = WifiService(context)
            val bluetoothManager = BluetoothManager(context)
            val wifiRepository = WifiRepository(wifiService)
            val bluetoothRepository = BluetoothRepository(bluetoothManager)
            
            val discoverTvDevicesUseCase = DiscoverTvDevicesUseCase(
                wifiRepository,
                bluetoothRepository
            )
            val connectWifiUseCase = ConnectWifiUseCase(wifiRepository)
            val connectBluetoothUseCase = ConnectBluetoothUseCase(bluetoothRepository)
            
            return DiscoveryViewModel(
                discoverTvDevicesUseCase,
                connectWifiUseCase,
                connectBluetoothUseCase,
                wifiRepository,
                bluetoothRepository
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
