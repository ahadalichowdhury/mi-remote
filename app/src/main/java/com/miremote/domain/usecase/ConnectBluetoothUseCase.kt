package com.miremote.domain.usecase

import com.miremote.data.model.TvDevice
import com.miremote.data.repository.BluetoothRepository

/**
 * Use case for connecting to a TV via Bluetooth
 */
class ConnectBluetoothUseCase(private val bluetoothRepository: BluetoothRepository) {
    /**
     * Connect to a TV device via Bluetooth
     */
    suspend fun execute(device: TvDevice): Result<Unit> {
        return bluetoothRepository.connect(device)
    }
}
