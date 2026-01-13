package com.miremote.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

/**
 * Manages runtime permissions for the app
 */
object PermissionManager {
    
    // Permission request codes
    const val REQUEST_CODE_BLUETOOTH = 100
    const val REQUEST_CODE_LOCATION = 101
    const val REQUEST_CODE_WIFI = 102
    
    /**
     * Gets the required permissions for Bluetooth
     */
    fun getBluetoothPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }
    
    /**
     * Gets the required permissions for Wi-Fi
     */
    fun getWifiPermissions(): Array<String> {
        return arrayOf(
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE
        )
    }
    
    /**
     * Checks if all required permissions are granted
     */
    fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.all { permission ->
            ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Checks if Bluetooth permissions are granted
     */
    fun hasBluetoothPermissions(context: Context): Boolean {
        return hasPermissions(context, getBluetoothPermissions())
    }
    
    /**
     * Checks if Wi-Fi permissions are granted
     */
    fun hasWifiPermissions(context: Context): Boolean {
        return hasPermissions(context, getWifiPermissions())
    }
    
    /**
     * Requests permissions using ActivityResultLauncher
     */
    fun requestPermissions(
        launcher: ActivityResultLauncher<Array<String>>,
        permissions: Array<String>
    ) {
        launcher.launch(permissions)
    }
}
