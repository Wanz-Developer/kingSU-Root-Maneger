package com.kingsu.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kingsu.model.AppInfo
import com.kingsu.ui.theme.DarkCardBorder
import com.kingsu.ui.theme.DarkSurface
import com.kingsu.ui.theme.DarkSurfaceVariant
import com.kingsu.ui.theme.KingGoldPrimary
import com.kingsu.ui.theme.NeonCyan
import com.kingsu.ui.theme.NeonGreen
import com.kingsu.ui.theme.NeonRed
import com.kingsu.ui.theme.TextMuted
import com.kingsu.ui.theme.TextPrimary
import com.kingsu.ui.theme.TextSecondary

@Composable
fun AppManagerScreen(
    apps: List<AppInfo>,
    onFreezeApp: (String) -> Unit,
    onUnfreezeApp: (String) -> Unit,
    onClearData: (String) -> Unit,
    onUninstallApp: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterMode by remember { mutableStateOf(0) } // 0: All, 1: System, 2: Frozen

    val filteredApps = remember(apps, searchQuery, filterMode) {
        apps.filter { app ->
            val matchesQuery = app.label.contains(searchQuery, ignoreCase = true) || app.packageName.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (filterMode) {
                1 -> app.isSystemApp
                2 -> app.isDisabled
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Root App Freezer & Manager",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = KingGoldPrimary
            )
        )
        Text(
            text = "Bekukan, aktifkan & kelola aplikasi sistem via PM Root",
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Cari nama aplikasi atau package...", fontSize = 12.sp, color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = KingGoldPrimary) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = KingGoldPrimary,
                unfocusedBorderColor = DarkCardBorder,
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Semua", "Aplikasi Sistem", "Dibekukan").forEachIndexed { index, label ->
                Button(
                    onClick = { filterMode = index },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (filterMode == index) KingGoldPrimary else DarkSurfaceVariant,
                        contentColor = if (filterMode == index) DarkSurface else TextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // App List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredApps) { app ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, if (app.isDisabled) NeonCyan else DarkCardBorder, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (app.isDisabled) Icons.Default.AcUnit else Icons.Default.Apps,
                                contentDescription = null,
                                tint = if (app.isDisabled) NeonCyan else KingGoldPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = app.label,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                )
                                Text(
                                    text = app.packageName,
                                    fontSize = 10.sp,
                                    color = TextMuted
                                )
                                if (app.isSystemApp) {
                                    Text("SYSTEM APP", fontSize = 9.sp, color = NeonRed, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (app.isDisabled) {
                                IconButton(onClick = { onUnfreezeApp(app.packageName) }) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Unfreeze", tint = NeonGreen)
                                }
                            } else {
                                IconButton(onClick = { onFreezeApp(app.packageName) }) {
                                    Icon(Icons.Default.AcUnit, contentDescription = "Freeze", tint = NeonCyan)
                                }
                            }

                            IconButton(onClick = { onUninstallApp(app.packageName) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Uninstall", tint = NeonRed)
                            }
                        }
                    }
                }
            }
        }
    }
}
