package com.miremote.services.bluetooth

import android.util.Log
import com.miremote.domain.model.KeyEvent

/**
 * Handles HID keycode mapping and report generation
 */
object HidProfile {
    
    companion object {
        private const val TAG = "HidProfile"
        
        // HID Usage Page IDs
        private const val USAGE_PAGE_KEYBOARD = 0x07
        private const val USAGE_PAGE_CONSUMER = 0x0C
        
        // HID Usage IDs for Keyboard
        private const val KEY_ENTER = 0x28
        private const val KEY_ESCAPE = 0x29
        private const val KEY_UP = 0x52
        private const val KEY_DOWN = 0x51
        private const val KEY_LEFT = 0x50
        private const val KEY_RIGHT = 0x4F
        
        // HID Usage IDs for Consumer Control
        private const val CONSUMER_HOME = 0x223
        private const val CONSUMER_VOLUME_INCREMENT = 0xE9
        private const val CONSUMER_VOLUME_DECREMENT = 0xEA
    }
    
    /**
     * Maps Android KeyEvent to HID keycode
     */
    fun getHidKeycode(keyEvent: KeyEvent): Int? {
        return when (keyEvent) {
            KeyEvent.DPAD_UP -> KEY_UP
            KeyEvent.DPAD_DOWN -> KEY_DOWN
            KeyEvent.DPAD_LEFT -> KEY_LEFT
            KeyEvent.DPAD_RIGHT -> KEY_RIGHT
            KeyEvent.DPAD_CENTER -> KEY_ENTER
            KeyEvent.BACK -> KEY_ESCAPE
            KeyEvent.HOME -> CONSUMER_HOME
            KeyEvent.VOLUME_UP -> CONSUMER_VOLUME_INCREMENT
            KeyEvent.VOLUME_DOWN -> CONSUMER_VOLUME_DECREMENT
            else -> null
        }
    }
    
    /**
     * Gets the HID usage page for a key event
     */
    fun getUsagePage(keyEvent: KeyEvent): Int {
        return when (keyEvent) {
            KeyEvent.HOME, KeyEvent.VOLUME_UP, KeyEvent.VOLUME_DOWN -> USAGE_PAGE_CONSUMER
            else -> USAGE_PAGE_KEYBOARD
        }
    }
    
    /**
     * Creates a HID keyboard report
     * Format: [modifier, reserved, key1, key2, key3, key4, key5, key6]
     */
    fun createKeyboardReport(keycode: Int): ByteArray {
        val report = ByteArray(8)
        report[0] = 0x00.toByte() // Modifier keys
        report[1] = 0x00.toByte() // Reserved
        report[2] = keycode.toByte() // Key 1
        report[3] = 0x00.toByte() // Key 2
        report[4] = 0x00.toByte() // Key 3
        report[5] = 0x00.toByte() // Key 4
        report[6] = 0x00.toByte() // Key 5
        report[7] = 0x00.toByte() // Key 6
        return report
    }
    
    /**
     * Creates a HID consumer control report
     */
    fun createConsumerReport(usage: Int): ByteArray {
        // Consumer control reports are typically 2 bytes
        val report = ByteArray(2)
        report[0] = (usage and 0xFF).toByte()
        report[1] = ((usage shr 8) and 0xFF).toByte()
        return report
    }
    
    /**
     * Creates a null report (key release)
     */
    fun createNullReport(usagePage: Int): ByteArray {
        return when (usagePage) {
            USAGE_PAGE_KEYBOARD -> ByteArray(8) // All zeros
            USAGE_PAGE_CONSUMER -> ByteArray(2) // All zeros
            else -> ByteArray(8)
        }
    }
}
