package com.taimwe.vpn

import android.content.Intent
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import com.taimwe.vpn.ui.theme.VpnAppTheme
import com.taimwe.vpn.ui.theme.MatrixRainBackground
import com.taimwe.vpn.ui.theme.RainbowMatrixTheme
import kotlinx.coroutines.delay
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    private var viewModel: VpnViewModel? = null
    
    fun setViewModel(vm: VpnViewModel) {
        viewModel = vm
    }
    
    private lateinit var preferencesManager: PreferencesManager
    private var reverseMatrix: Boolean = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesManager = PreferencesManager(applicationContext)
        reverseMatrix = preferencesManager.isDarkMode
        setContent {
            VpnAppTheme(selectedTheme = RainbowMatrixTheme) {
                MatrixRainBackground(
                    enabled = true,
                    themeColor = RainbowMatrixTheme.primary,
                    reverse = reverseMatrix,
                    rainbow = true,
                    modifier = Modifier.fillMaxSize()
                )
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.45f)
                ) {
                    VpnScreen()
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        val newReverse = preferencesManager.isDarkMode
        if (newReverse != reverseMatrix) {
            reverseMatrix = newReverse
            recreate()
        }
    }

    private val importConfigLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val config = result.data?.getStringExtra("config")
            if (config != null) {
                viewModel?.connectWithConfig(config)
            }
        }
    }

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel?.connect()
        }
    }

    fun startVpn(config: String) {
        val intent = Intent(this, VpnService::class.java).apply {
            putExtra("config", config)
        }
        startService(intent)
    }

    fun stopVpn() {
        val intent = Intent(this, VpnService::class.java)
        stopService(intent)
    }

    fun requestVpnPermission() {
        val intent = VpnService.prepare(this)
        if (intent != null) {
            vpnPermissionLauncher.launch(intent)
        } else {
            viewModel?.connect()
        }
    }

    fun launchImportConfig() {
        val intent = Intent(this, ImportConfigActivity::class.java)
        importConfigLauncher.launch(intent)
    }

    fun launchSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    fun launchHistory() {
        val intent = Intent(this, ConnectionHistoryActivity::class.java)
        startActivity(intent)
    }
}

@Suppress("OPT_IN_USAGE")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VpnScreen(viewModel: VpnViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val activity = (LocalContext.current as MainActivity)
    
    LaunchedEffect(Unit) {
        activity.setViewModel(viewModel)
    }
    val infiniteTransition = rememberInfiniteTransition(label = "animation")
    
    LaunchedEffect(uiState.isConnected) {
        while (uiState.isConnected) {
            delay(1000)
            viewModel.updateConnectionTime()
        }
    }
    
    val animatedScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )
    
    val animatedAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha"
    )
    
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (uiState.isConnected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF00D4FF).copy(alpha = glowAlpha * 0.3f),
                                Color.Transparent
                            ),
                            radius = 300f
                        )
                    )
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DT VPN",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .graphicsLayer {
                            shadowElevation = 8f
                        }
                        .background(
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
                
                IconButton(onClick = { activity.launchSettings() }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color.White,
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                color = Color.Black.copy(alpha = 0.6f),
                                shape = CircleShape
                            )
                            .padding(6.dp)
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Fast. Secure. Free.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 48.dp)
                )
                
                AnimatedVisibility(
                    visible = uiState.isConnected,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .graphicsLayer {
                                shadowElevation = 16f
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF000000).copy(alpha = 0.88f)
                        ),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Connected",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = formatDuration(uiState.connectionDuration),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = "Server: ${uiState.selectedServer}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .graphicsLayer {
                            rotationZ = if (uiState.isConnected) rotationAngle else 0f
                            scaleX = if (uiState.isConnected) pulseScale else 1f
                            scaleY = if (uiState.isConnected) pulseScale else 1f
                        }
                ) {
                    if (uiState.isConnected) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF00D4FF).copy(alpha = animatedAlpha),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxSize()
                                .shadow(
                                    elevation = if (uiState.isConnected) 32.dp else 12.dp,
                                    shape = CircleShape,
                                    ambientColor = MaterialTheme.colorScheme.primary,
                                    spotColor = MaterialTheme.colorScheme.primary
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (uiState.isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            ),
                            shape = CircleShape
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (uiState.isConnected) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                Button(
                    onClick = {
                        if (uiState.isConnected) {
                            activity.stopVpn()
                            viewModel.disconnect()
                        } else {
                            activity.requestVpnPermission()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .graphicsLayer {
                            if (uiState.isConnected) {
                                scaleX = pulseScale
                                scaleY = pulseScale
                            }
                        },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isConnected) Color(0xFFFF006E) else MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp),
                    enabled = !uiState.isConnecting,
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 12.dp,
                        disabledElevation = 4.dp
                    )
                ) {
                    if (uiState.isConnecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = Color.White,
                            strokeWidth = 3.dp
                        )
                    } else {
                        Text(
                            text = if (uiState.isConnected) "Disconnect" else "Connect",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = { activity.launchImportConfig() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = ButtonDefaults.outlinedButtonBorder
                        .copy(brush = Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)))
                ) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Import Config", fontSize = 16.sp)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                ServerSelector(
                    selectedServer = uiState.selectedServer,
                    servers = uiState.serverList,
                    onServerSelected = { viewModel.selectServer(it) }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                StatsCard(
                    connectionCount = uiState.connectionCount,
                    blockedCount = AdBlocker.getBlockedCount()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedButton(
                    onClick = { activity.launchHistory() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF00D4FF)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Connection History", fontSize = 16.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Suppress("OPT_IN_USAGE", "OPT_IN_USAGE_ERROR", "EXPERIMENTAL_API_USAGE", "all")
@Composable
fun ServerSelector(
    selectedServer: String,
    servers: List<String>,
    onServerSelected: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF000000).copy(alpha = 0.88f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Select Server",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        servers.forEach { server ->
            val flag = when (server) {
                "Auto" -> "🌐"
                "🇺🇸 USA" -> "🇺🇸"
                "🇩🇪 DE" -> "🇩🇪"
                "🇯🇵 JP" -> "🇯🇵"
                "🇸🇬 SG" -> "🇸🇬"
                "🇬🇧 UK" -> "🇬🇧"
                else -> server.take(2)
            }
            FilterChip(
                selected = selectedServer == server,
                onClick = { onServerSelected(server) },
                label = { Text(flag) },
                modifier = Modifier.weight(1f),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
        }
    }
}

@Composable
fun AdBlockerCard(
    isEnabled: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF000000).copy(alpha = 0.88f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = if (isEnabled) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "Ad Blocker",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isEnabled) "${AdBlocker.getBlockedCount()} domains blocked" else "Disabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Switch(
                checked = isEnabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

@Composable
fun StatsCard(
    connectionCount: Int,
    blockedCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF000000).copy(alpha = 0.88f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$connectionCount",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Connections",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Divider(
                modifier = Modifier
                    .width(1.dp)
                    .height(48.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$blockedCount",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ads Blocked",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun formatDuration(durationMs: Long): String {
    val seconds = (durationMs / 1000) % 60
    val minutes = (durationMs / (1000 * 60)) % 60
    val hours = (durationMs / (1000 * 60 * 60))
    
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
