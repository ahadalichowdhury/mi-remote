package com.miremote.domain.usecase

import com.miremote.data.model.TvDevice
import com.miremote.data.repository.BluetoothRepository
import com.miremote.data.repository.WifiRepository

/**
 * Use case for discovering TV devices
 */
class DiscoverTvDevicesUseCase(
    private val wifiRepository: WifiRepository,
    private val bluetoothRepository: BluetoothRepository
) {
    /**
     * Discover devices based on connection type
     */
    suspend fun execute(type: TvDevice.ConnectionType): List<TvDevice> {
        return when (type) {
            TvDevice.ConnectionType.WIFI -> wifiRepository.discoverDevices()
            TvDevice.ConnectionType.BLUETOOTH -> bluetoothRepository.discoverDevices()
        }
    }
}
