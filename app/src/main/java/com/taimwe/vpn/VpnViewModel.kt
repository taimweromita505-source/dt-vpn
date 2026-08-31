package com.taimwe.vpn

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VpnViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesManager = PreferencesManager(application)
    
    private val _uiState = MutableStateFlow(VpnUiState())
    val uiState: StateFlow<VpnUiState> = _uiState.asStateFlow()
    
    init {
        _uiState.value = _uiState.value.copy(
            isAdBlockerEnabled = preferencesManager.isAdBlockerEnabled,
            selectedServer = preferencesManager.selectedServer ?: "Auto",
            connectionCount = preferencesManager.connectionCount
        )
    }
    
    fun connect() {
        _uiState.value = _uiState.value.copy(
            isConnecting = true,
            connectionStatus = ConnectionStatus.CONNECTING
        )
        
        viewModelScope.launch {
            delay(1500)
            
            val selectedServer = _uiState.value.selectedServer
            val endpoint = when (selectedServer) {
                "USA", "🇺🇸 USA" -> "us.example.com:51820"
                "Germany", "🇩🇪 DE" -> "de.example.com:51820"
                "Japan", "🇯🇵 JP" -> "jp.example.com:51820"
                "Singapore", "🇸🇬 SG" -> "sg.example.com:51820"
                "UK", "🇬🇧 UK" -> "uk.example.com:51820"
                else -> "auto.example.com:51820"
            }
            
            val config = WireGuardConfig.generateConfig(
                privateKey = "cGFyb2Rlc2tleWZvcm15cGFyb2Rlc2tleWZvcm15cGFyb2Rlc2tleQ==",
                publicKey = "cHVibGlja2V5cGFyb2Rlc2tleWZvcm15cGFyb2Rlc2tleWZvcm15cGFyb2Rlc2tleQ==",
                endpoint = endpoint,
                allowedIPs = "0.0.0.0/0, ::/0",
                dns = "10.8.0.1"
            )
            
            connectWithConfig(config)
        }
    }
    
    fun connectWithConfig(config: String) {
        _uiState.value = _uiState.value.copy(
            isConnecting = true,
            connectionStatus = ConnectionStatus.CONNECTING
        )
        
        viewModelScope.launch {
            delay(1000)
            
            val context = getApplication<Application>().applicationContext
            val intent = Intent(context, VpnService::class.java).apply {
                putExtra("config", config)
            }
            context.startService(intent)
            
            val newCount = _uiState.value.connectionCount + 1
            preferencesManager.connectionCount = newCount
            
            _uiState.value = _uiState.value.copy(
                isConnected = true,
                isConnecting = false,
                connectionStatus = ConnectionStatus.CONNECTED,
                connectionCount = newCount,
                connectionTime = System.currentTimeMillis()
            )
        }
    }
    
    fun disconnect() {
        val context = getApplication<Application>().applicationContext
        val intent = Intent(context, VpnService::class.java)
        context.stopService(intent)
        
        _uiState.value = _uiState.value.copy(
            isConnected = false,
            isConnecting = false,
            connectionStatus = ConnectionStatus.DISCONNECTED,
            connectionTime = null
        )
    }
    
    fun toggleAdBlocker() {
        val newState = !_uiState.value.isAdBlockerEnabled
        preferencesManager.isAdBlockerEnabled = newState
        AdBlocker.setEnabled(newState)
        _uiState.value = _uiState.value.copy(isAdBlockerEnabled = newState)
    }
    
    fun updateConnectionTime() {
        val currentTime = _uiState.value.connectionTime ?: return
        val elapsed = System.currentTimeMillis() - currentTime
        _uiState.value = _uiState.value.copy(connectionDuration = elapsed)
    }
    
    fun selectServer(server: String) {
        preferencesManager.selectedServer = server
        _uiState.value = _uiState.value.copy(selectedServer = server)
    }
}

data class VpnUiState(
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val connectionTime: Long? = null,
    val connectionDuration: Long = 0,
    val connectionCount: Int = 0,
    val isAdBlockerEnabled: Boolean = true,
    val selectedServer: String = "Auto",
    val serverList: List<String> = listOf("Auto", "🇺🇸 USA", "🇩🇪 DE", "🇯🇵 JP", "🇸🇬 SG", "🇬🇧 UK")
)

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}
