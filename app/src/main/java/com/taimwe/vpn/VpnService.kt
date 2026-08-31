package com.taimwe.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.File

class VpnService : VpnService() {
    private var vpnInterface: ParcelFileDescriptor? = null
    private var isRunning = false
    private var wireGuardConfig: String? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        AdBlocker.initialize(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        wireGuardConfig = intent?.getStringExtra("config")
        
        if (isRunning) {
            stopVpn()
        } else if (wireGuardConfig != null) {
            startVpn()
        } else {
            Log.e(TAG, "No WireGuard config provided")
            stopVpn()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    private fun startVpn() {
        try {
            val config = wireGuardConfig ?: return
            
            val builder = Builder()
            builder.setSession("TaimweVPN")
            
            val interfaceAddress = extractInterfaceAddress(config)
            val dnsServers = extractDnsServers(config)
            val routes = extractRoutes(config)
            
            builder.addAddress(interfaceAddress.first, interfaceAddress.second)
            dnsServers.forEach { dns ->
                builder.addDnsServer(dns)
            }
            routes.forEach { route ->
                builder.addRoute(route.first, route.second)
            }
            
            builder.setMtu(extractMtu(config) ?: 1420)
            builder.setBlocking(true)

            val activityIntent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this, 0, activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.setConfigureIntent(pendingIntent)

            vpnInterface = builder.establish()
            if (vpnInterface == null) {
                Log.e(TAG, "Failed to establish VPN interface")
                return
            }

            isRunning = true
            startForeground(NOTIFICATION_ID, createNotification())
            
            Log.d(TAG, "VPN started successfully")
            Log.d(TAG, "Interface: $interfaceAddress")
            Log.d(TAG, "DNS: $dnsServers")
            Log.d(TAG, "Routes: $routes")
            Log.d(TAG, "Config preview:\n$config")
            
            saveConfig(config)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting VPN", e)
            stopVpn()
        }
    }

    private fun extractInterfaceAddress(config: String): Pair<String, Int> {
        val addressRegex = Regex("Address\\s*=\\s*([\\d.]+)/(\\d+)")
        val match = addressRegex.find(config)
        return if (match != null) {
            Pair(match.groupValues[1], match.groupValues[2].toInt())
        } else {
            Pair("10.8.0.2", 32)
        }
    }

    private fun extractDnsServers(config: String): List<String> {
        val dnsRegex = Regex("DNS\\s*=\\s*([\\d.]+(?:,\\s*[\\d.]+)*)")
        val match = dnsRegex.find(config)
        return if (match != null) {
            match.groupValues[1].split(",").map { it.trim() }.filter { it.isNotEmpty() }
        } else {
            listOf("10.8.0.1", "1.1.1.1")
        }
    }

    private fun extractRoutes(config: String): List<Pair<String, Int>> {
        val routes = mutableListOf<Pair<String, Int>>()
        val allowedIpsRegex = Regex("AllowedIPs\\s*=\\s*(.+)")
        val match = allowedIpsRegex.find(config)
        
        if (match != null) {
            val allowedIps = match.groupValues[1]
            val ipRegex = Regex("([\\d.]+)/(\\d+)")
            ipRegex.findAll(allowedIps).forEach { ipMatch ->
                routes.add(Pair(ipMatch.groupValues[1], ipMatch.groupValues[2].toInt()))
            }
        }
        
        if (routes.isEmpty()) {
            routes.add(Pair("0.0.0.0", 0))
        }
        
        return routes
    }

    private fun extractMtu(config: String): Int? {
        val mtuRegex = Regex("MTU\\s*=\\s*(\\d+)")
        val match = mtuRegex.find(config)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun saveConfig(config: String) {
        try {
            val configFile = File(filesDir, "wireguard.conf")
            configFile.writeText(config)
            Log.d(TAG, "Config saved to: ${configFile.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save config", e)
        }
    }

    private fun stopVpn() {
        try {
            vpnInterface?.close()
            vpnInterface = null
            isRunning = false
            wireGuardConfig = null
            stopForeground(STOP_FOREGROUND_REMOVE)
            Log.d(TAG, "VPN stopped")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping VPN", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVpn()
    }

    override fun onRevoke() {
        stopVpn()
        super.onRevoke()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "DT VPN",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "VPN connection status"
                setSound(null, null)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("DT VPN")
            .setContentText("VPN is running")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "vpn_channel"
        const val NOTIFICATION_ID = 1
        const val TAG = "VpnService"
    }
}
