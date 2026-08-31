package com.taimwe.vpn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.taimwe.vpn.ui.theme.VpnAppTheme
import com.taimwe.vpn.ui.theme.MatrixRainBackground
import com.taimwe.vpn.ui.theme.RainbowMatrixTheme

class SettingsActivity : ComponentActivity() {
    private lateinit var preferencesManager: PreferencesManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferencesManager = PreferencesManager(applicationContext)
        val reverseMatrix = preferencesManager.isDarkMode
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
                    SettingsScreen(
                        reverseMatrix = reverseMatrix,
                        onReverseMatrixChange = { enabled ->
                            preferencesManager.isDarkMode = enabled
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    reverseMatrix: Boolean,
    onReverseMatrixChange: (Boolean) -> Unit
) {
    var adBlockerEnabled by remember { mutableStateOf(true) }
    var autoConnect by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .padding(bottom = 24.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 4.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF000000).copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsItem(
                    icon = Icons.Default.Shield,
                    title = "Ad Blocker",
                    subtitle = "Block ads at DNS level",
                    checked = adBlockerEnabled,
                    onToggle = { adBlockerEnabled = it }
                )

                Divider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color(0xFF1E2340)
                )

                SettingsItem(
                    icon = Icons.Default.PlayArrow,
                    title = "Auto Connect",
                    subtitle = "Connect on app startup",
                    checked = autoConnect,
                    onToggle = { autoConnect = it }
                )

                Divider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color(0xFF1E2340)
                )

                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "Show connection notifications",
                    checked = notificationsEnabled,
                    onToggle = { notificationsEnabled = it }
                )

                Divider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Color(0xFF1E2340)
                )

                SettingsItem(
                    icon = Icons.Default.History,
                    title = "Connection History",
                    subtitle = "View past connections",
                    checked = false,
                    onToggle = { /* Navigate to history */ }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF000000).copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Matrix Effect",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                SettingsItem(
                    icon = Icons.Default.KeyboardArrowUp,
                    title = "Reverse Matrix",
                    subtitle = "Make rain go up",
                    checked = reverseMatrix,
                    onToggle = onReverseMatrixChange
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF000000).copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Version",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8B949E)
                    )
                    Text(
                        text = "1.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Build",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8B949E)
                    )
                    Text(
                        text = "Debug",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF00D4FF),
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8B949E)
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF06D6A0),
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = Color(0xFF1E2340)
            )
        )
    }
}
