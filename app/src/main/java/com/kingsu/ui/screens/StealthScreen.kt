package com.kingsu.ui.screens

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
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kingsu.model.CloakedApp
import com.kingsu.model.StealthConfig
import com.kingsu.ui.theme.DarkCardBorder
import com.kingsu.ui.theme.DarkSurface
import com.kingsu.ui.theme.KingGoldPrimary
import com.kingsu.ui.theme.NeonCyan
import com.kingsu.ui.theme.NeonGreen
import com.kingsu.ui.theme.TextMuted
import com.kingsu.ui.theme.TextPrimary
import com.kingsu.ui.theme.TextSecondary

@Composable
fun StealthScreen(
    stealthConfig: StealthConfig,
    cloakedApps: List<CloakedApp>,
    onToggleZygisk: (Boolean) -> Unit,
    onToggleDenyList: (Boolean) -> Unit,
    onToggleAppCloak: (String, Boolean) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredApps = remember(cloakedApps, searchQuery) {
        cloakedApps.filter {
            it.label.contains(searchQuery, ignoreCase = true) || it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Penyamaran & Keamanan Root",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = KingGoldPrimary
                )
            )
            Text(
                text = "Sembunyikan akses root dari aplikasi Perbankan, E-Wallet & Game",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Master Stealth Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, KingGoldPrimary, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // DenyList Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = KingGoldPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Sembunyikan Root (DenyList)", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                            Text("Proteksi Shamiko / Zygisk Cloak", fontSize = 10.sp, color = TextMuted)
                        }
                    }

                    Switch(
                        checked = stealthConfig.isDenyListActive,
                        onCheckedChange = onToggleDenyList,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonGreen,
                            checkedTrackColor = NeonGreen.copy(alpha = 0.3f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Zygisk Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Zygisk Stealth Module", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 14.sp)
                            Text("Modul Injeksi Zygote Ramdisk", fontSize = 10.sp, color = TextMuted)
                        }
                    }

                    Switch(
                        checked = stealthConfig.isZygiskEnabled,
                        onCheckedChange = onToggleZygisk,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonGreen,
                            checkedTrackColor = NeonGreen.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // App Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Cari aplikasi perbankan/game untuk disembunyikan...", fontSize = 12.sp, color = TextMuted) },
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

        // Cloaked App List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredApps) { app ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, if (app.isCloaked) NeonGreen else DarkCardBorder, RoundedCornerShape(10.dp)),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (app.isCloaked) Icons.Default.VisibilityOff else Icons.Default.Apps,
                                contentDescription = null,
                                tint = if (app.isCloaked) NeonGreen else TextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(app.label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                                Text(app.packageName, fontSize = 10.sp, color = TextMuted)
                            }
                        }

                        Switch(
                            checked = app.isCloaked,
                            onCheckedChange = { onToggleAppCloak(app.packageName, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonGreen,
                                checkedTrackColor = NeonGreen.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        }
    }
}
