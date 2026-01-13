package com.miremote.domain.usecase

import com.miremote.data.model.TvDevice
import com.miremote.data.repository.BluetoothRepository
import com.miremote.data.repository.WifiRepository
import com.miremote.domain.model.KeyEvent

/**
 * Use case for sending key events to a connected TV
 */
class SendKeyEventUseCase(
    private val wifiRepository: WifiRepository,
    private val bluetoothRepository: BluetoothRepository
) {
    /**
     * Send a key event based on connection type
     */
    suspend fun execute(keyEvent: KeyEvent, connectionType: TvDevice.ConnectionType): Result<Unit> {
        return when (connectionType) {
            TvDevice.ConnectionType.WIFI -> wifiRepository.sendKeyEvent(keyEvent)
            TvDevice.ConnectionType.BLUETOOTH -> bluetoothRepository.sendKeyEvent(keyEvent)
        }
    }
}
