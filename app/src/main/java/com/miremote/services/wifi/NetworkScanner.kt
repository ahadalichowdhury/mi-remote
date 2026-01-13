package com.miremote.services.wifi

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.miremote.data.model.TvDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Scans the local network for potential TV devices
 */
class NetworkScanner(private val context: Context) {
    
    companion object {
        private const val TAG = "NetworkScanner"
        private const val ADB_PORT = 5555
        private const val TIMEOUT_MS = 500
        private const val MAX_THREADS = 50
    }
    
    /**
     * Scans the local network for devices with ADB enabled
     * Returns a list of potential TV devices
     */
    suspend fun scanNetwork(): List<TvDevice> = withContext(Dispatchers.IO) {
        val devices = mutableListOf<TvDevice>()
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        
        if (wifiManager == null) {
            Log.e(TAG, "WiFiManager is null")
            return@withContext devices
        }
        
        val dhcpInfo = wifiManager.dhcpInfo
        if (dhcpInfo == null) {
            Log.e(TAG, "DHCP info is null")
            return@withContext devices
        }
        
        // Get network base IP (e.g., 192.168.1.0)
        val networkBase = intToIp(dhcpInfo.ipAddress and dhcpInfo.netmask)
        val networkBaseParts = networkBase.split(".")
        if (networkBaseParts.size != 4) {
            Log.e(TAG, "Invalid network base: $networkBase")
            return@withContext devices
        }
        
        val baseIp = networkBaseParts[0].toInt()
        val secondOctet = networkBaseParts[1].toInt()
        val thirdOctet = networkBaseParts[2].toInt()
        
        Log.d(TAG, "Scanning network: $baseIp.$secondOctet.$thirdOctet.0/24")
        
        // Scan common IP ranges (1-254)
        val ipAddresses = mutableListOf<String>()
        for (i in 1..254) {
            ipAddresses.add("$baseIp.$secondOctet.$thirdOctet.$i")
        }
        
        // Scan IPs in parallel batches
        ipAddresses.chunked(MAX_THREADS).forEach { batch ->
            val batchResults = batch.map { ip ->
                kotlinx.coroutines.async {
                    checkDevice(ip)
                }
            }
            batchResults.forEach { deferred ->
                deferred.await()?.let { device ->
                    devices.add(device)
                }
            }
        }
        
        Log.d(TAG, "Found ${devices.size} devices")
        devices
    }
    
    /**
     * Checks if a device at the given IP has ADB enabled
     */
    private suspend fun checkDevice(ip: String): TvDevice? = withContext(Dispatchers.IO) {
        return@withContext try {
            val socket = Socket()
            socket.connect(InetSocketAddress(ip, ADB_PORT), TIMEOUT_MS)
            socket.close()
            
            // Try to get hostname
            val hostname = try {
                InetAddress.getByName(ip).hostName
            } catch (e: Exception) {
                ip
            }
            
            Log.d(TAG, "Found device at $ip")
            TvDevice(
                name = hostname,
                address = ip,
                type = TvDevice.ConnectionType.WIFI,
                isConnected = false
            )
        } catch (e: Exception) {
            // Device not found or not accessible
            null
        }
    }
    
    /**
     * Converts integer IP address to string format
     */
    private fun intToIp(ip: Int): String {
        return String.format(
            "%d.%d.%d.%d",
            ip and 0xFF,
            ip shr 8 and 0xFF,
            ip shr 16 and 0xFF,
            ip shr 24 and 0xFF
        )
    }
}
