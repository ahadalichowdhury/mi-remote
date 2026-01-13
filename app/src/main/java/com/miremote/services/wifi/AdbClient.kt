package com.miremote.services.wifi

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

/**
 * Client for ADB over TCP/IP connections
 */
class AdbClient {
    
    companion object {
        private const val TAG = "AdbClient"
        private const val ADB_PORT = 5555
        private const val CONNECT_TIMEOUT_SECONDS = 5L
    }
    
    private var connectedIp: String? = null
    
    /**
     * Connects to a device via ADB over TCP/IP
     * @param ip IP address of the device
     * @return Result indicating success or failure
     */
    suspend fun connect(ip: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Connecting to $ip:$ADB_PORT")
            
            // First, disconnect any existing connection
            disconnect()
            
            // Execute adb connect command
            val process = Runtime.getRuntime().exec("adb connect $ip:$ADB_PORT")
            val success = process.waitFor(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            
            if (!success) {
                process.destroyForcibly()
                return@withContext Result.failure(Exception("Connection timeout"))
            }
            
            val exitCode = process.exitValue()
            val output = readProcessOutput(process)
            
            Log.d(TAG, "ADB connect output: $output")
            
            if (exitCode == 0 && output.contains("connected")) {
                connectedIp = ip
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to connect: $output"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error connecting to $ip", e)
            Result.failure(e)
        }
    }
    
    /**
     * Disconnects from the current device
     */
    suspend fun disconnect() = withContext(Dispatchers.IO) {
        connectedIp?.let { ip ->
            try {
                Log.d(TAG, "Disconnecting from $ip")
                val process = Runtime.getRuntime().exec("adb disconnect $ip:$ADB_PORT")
                process.waitFor(2, TimeUnit.SECONDS)
                val output = readProcessOutput(process)
                Log.d(TAG, "ADB disconnect output: $output")
            } catch (e: Exception) {
                Log.e(TAG, "Error disconnecting", e)
            }
        }
        connectedIp = null
    }
    
    /**
     * Executes a key event command on the connected device
     * @param keyCode Android key event code
     * @return Result indicating success or failure
     */
    suspend fun sendKeyEvent(keyCode: Int): Result<Unit> = withContext(Dispatchers.IO) {
        if (connectedIp == null) {
            return@withContext Result.failure(Exception("Not connected to any device"))
        }
        
        return@withContext try {
            val command = "adb shell input keyevent $keyCode"
            Log.d(TAG, "Executing: $command")
            
            val process = Runtime.getRuntime().exec(command)
            val success = process.waitFor(2, TimeUnit.SECONDS)
            
            if (!success) {
                process.destroyForcibly()
                return@withContext Result.failure(Exception("Command timeout"))
            }
            
            val exitCode = process.exitValue()
            if (exitCode == 0) {
                Result.success(Unit)
            } else {
                val error = readProcessOutput(process)
                Result.failure(Exception("Command failed: $error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending key event", e)
            Result.failure(e)
        }
    }
    
    /**
     * Checks if currently connected to a device
     */
    fun isConnected(): Boolean {
        return connectedIp != null
    }
    
    /**
     * Gets the currently connected IP address
     */
    fun getConnectedIp(): String? {
        return connectedIp
    }
    
    /**
     * Reads the output from a process
     */
    private fun readProcessOutput(process: Process): String {
        return try {
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }
            output.toString().trim()
        } catch (e: Exception) {
            ""
        }
    }
}
