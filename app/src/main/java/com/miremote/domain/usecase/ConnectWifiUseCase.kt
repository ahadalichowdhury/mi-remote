package com.miremote.domain.usecase

import com.miremote.data.model.TvDevice
import com.miremote.data.repository.WifiRepository

/**
 * Use case for connecting to a TV via Wi-Fi
 */
class ConnectWifiUseCase(private val wifiRepository: WifiRepository) {
    /**
     * Connect to a TV device via Wi-Fi
     */
    suspend fun execute(device: TvDevice): Result<Unit> {
        return wifiRepository.connect(device)
    }
}
