package com.miremote.domain.model

/**
 * Represents a key event that can be sent to the TV
 */
enum class KeyEvent(val code: Int, val description: String) {
    DPAD_UP(19, "DPAD_UP"),
    DPAD_DOWN(20, "DPAD_DOWN"),
    DPAD_LEFT(21, "DPAD_LEFT"),
    DPAD_RIGHT(22, "DPAD_RIGHT"),
    DPAD_CENTER(23, "DPAD_CENTER"),
    HOME(3, "HOME"),
    BACK(4, "BACK"),
    VOLUME_UP(24, "VOLUME_UP"),
    VOLUME_DOWN(25, "VOLUME_DOWN");
    
    companion object {
        fun fromCode(code: Int): KeyEvent? {
            return values().find { it.code == code }
        }
    }
}
