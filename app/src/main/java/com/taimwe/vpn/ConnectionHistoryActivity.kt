package com.taimwe.vpn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import java.text.SimpleDateFormat
import java.util.*

class ConnectionHistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VpnAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ConnectionHistoryScreen()
                }
            }
        }
    }
}

@Composable
fun ConnectionHistoryScreen() {
    val history = remember {
        mutableStateListOf(
            ConnectionRecord("USA", "2024-01-15 14:30", "2h 15m", "Success"),
            ConnectionRecord("Germany", "2024-01-15 10:20", "1h 45m", "Success"),
            ConnectionRecord("Japan", "2024-01-14 18:45", "3h 20m", "Success"),
            ConnectionRecord("Singapore", "2024-01-14 12:10", "45m", "Disconnected"),
            ConnectionRecord("UK", "2024-01-13 09:30", "5h 10m", "Success")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Connection History",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF151932)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(history) { record ->
                    HistoryItem(record = record)
                }
            }
        }
    }
}

@Composable
fun HistoryItem(record: ConnectionRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = record.server,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = record.date,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8B949E)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = record.duration,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF00D4FF)
            )
            
            Text(
                text = record.status,
                style = MaterialTheme.typography.bodySmall,
                color = if (record.status == "Success") Color(0xFF06D6A0) else Color(0xFFFF006E)
            )
        }
    }
}

data class ConnectionRecord(
    val server: String,
    val date: String,
    val duration: String,
    val status: String
)
