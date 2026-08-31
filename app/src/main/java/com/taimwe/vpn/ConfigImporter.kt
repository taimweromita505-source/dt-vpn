package com.taimwe.vpn

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

object ConfigImporter {
    private const val TAG = "ConfigImporter"

    fun importFromText(text: String): String? {
        return try {
            val cleaned = text.trim()
            if (cleaned.isNotEmpty()) {
                cleaned
            } else {
                Log.e(TAG, "Config text is empty")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error importing config from text", e)
            null
        }
    }

    fun getSampleConfig(): String {
        return WireGuardConfig.getSampleConfig()
    }

    fun importFromFile(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val content = reader.readText()
                    if (content.isNotBlank()) {
                        content
                    } else {
                        Log.e(TAG, "Config file is empty")
                        null
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error importing config from file", e)
            null
        }
    }

    fun extractServerInfo(config: String): ServerInfo? {
        return try {
            val endpointRegex = Regex("Endpoint\\s*=\\s*([^:]+):(\\d+)")
            val endpointMatch = endpointRegex.find(config)
            
            val privateKeyRegex = Regex("PrivateKey\\s*=\\s*(.+)")
            val privateKeyMatch = privateKeyRegex.find(config)
            
            val publicKeyRegex = Regex("PublicKey\\s*=\\s*(.+)")
            val publicKeyMatch = publicKeyRegex.find(config)
            
            if (endpointMatch != null && privateKeyMatch != null && publicKeyMatch != null) {
                ServerInfo(
                    endpoint = endpointMatch.groupValues[1],
                    port = endpointMatch.groupValues[2].toIntOrNull() ?: 51820,
                    privateKey = privateKeyMatch.groupValues[1].trim(),
                    publicKey = publicKeyMatch.groupValues[1].trim()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting server info", e)
            null
        }
    }

    data class ServerInfo(
        val endpoint: String,
        val port: Int,
        val privateKey: String,
        val publicKey: String
    )
}
