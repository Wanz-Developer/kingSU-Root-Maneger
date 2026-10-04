package com.kingsu.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kingsu.model.RootStatus
import com.kingsu.model.SELinuxMode
import com.kingsu.model.SystemInfo
import com.kingsu.ui.theme.DarkCardBorder
import com.kingsu.ui.theme.DarkSurface
import com.kingsu.ui.theme.DarkSurfaceVariant
import com.kingsu.ui.theme.KingGoldDark
import com.kingsu.ui.theme.KingGoldPrimary
import com.kingsu.ui.theme.NeonCyan
import com.kingsu.ui.theme.NeonGreen
import com.kingsu.ui.theme.NeonRed
import com.kingsu.ui.theme.TextMuted
import com.kingsu.ui.theme.TextPrimary
import com.kingsu.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    rootStatus: RootStatus,
    systemInfo: SystemInfo,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    onToggleSELinux: (SELinuxMode) -> Unit,
    onReboot: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "KingSU Console",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = KingGoldPrimary
                    )
                )
                Text(
                    text = "Kernel & Boot Image Root Manager Engine",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
            }

            IconButton(
                onClick = onRefresh,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = KingGoldPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = KingGoldPrimary
                    )
                }
            }
        }

        // Hero Root Status Banner Card
        val gradientColors = if (rootStatus.isRooted) {
            listOf(KingGoldDark, Color(0xFF1E1A00))
        } else {
            listOf(Color(0xFF331111), Color(0xFF1A0A0A))
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (rootStatus.isRooted) KingGoldPrimary else NeonRed, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(gradientColors))
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (rootStatus.isRooted) Icons.Default.Shield else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (rootStatus.isRooted) KingGoldPrimary else NeonRed,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (rootStatus.isRooted) "ROOT ACCESS ACTIVE" else "NO ROOT DETECTED",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (rootStatus.isRooted) KingGoldPrimary else NeonRed
                                )
                                Text(
                                    text = "SU Solution: ${rootStatus.solutionType.displayName}",
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (rootStatus.hasGrantPermission) NeonGreen.copy(alpha = 0.2f) else NeonRed.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (rootStatus.hasGrantPermission) "GRANTED" else "DENIED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (rootStatus.hasGrantPermission) NeonGreen else NeonRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoChip(label = "SU Path", value = rootStatus.suBinaryPath)
                        InfoChip(label = "SU Version", value = rootStatus.suVersion)
                        InfoChip(label = "BusyBox", value = rootStatus.busyBoxVersion)
                    }
                }
            }
        }

        // SELinux Control Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = NeonCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SELinux Security Mode", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }

                    Text(
                        text = rootStatus.seLinuxMode.displayName,
                        fontWeight = FontWeight.Bold,
                        color = if (rootStatus.seLinuxMode == SELinuxMode.ENFORCING) NeonGreen else NeonRed
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onToggleSELinux(SELinuxMode.ENFORCING) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (rootStatus.seLinuxMode == SELinuxMode.ENFORCING) NeonGreen else TextSecondary
                        )
                    ) {
                        Text("Set Enforcing")
                    }

                    OutlinedButton(
                        onClick = { onToggleSELinux(SELinuxMode.PERMISSIVE) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (rootStatus.seLinuxMode == SELinuxMode.PERMISSIVE) NeonRed else TextSecondary
                        )
                    ) {
                        Text("Set Permissive")
                    }
                }
            }
        }

        // Hardware & Memory Monitor Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Device Hardware & Memory Monitor", fontWeight = FontWeight.Bold, color = KingGoldPrimary)
                Spacer(modifier = Modifier.height(12.dp))

                // RAM Bar
                val usedRam = systemInfo.ramTotalMb - systemInfo.ramAvailableMb
                val ramProgress = if (systemInfo.ramTotalMb > 0) usedRam.toFloat() / systemInfo.ramTotalMb else 0f

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Memory, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("RAM Usage", fontSize = 12.sp, color = TextSecondary)
                    }
                    Text("${usedRam}MB / ${systemInfo.ramTotalMb}MB", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { ramProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (ramProgress > 0.85f) NeonRed else NeonCyan,
                    trackColor = DarkSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ZRAM Bar
                val zramProgress = if (systemInfo.zramTotalMb > 0) systemInfo.zramUsedMb.toFloat() / systemInfo.zramTotalMb else 0f
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Memory, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ZRAM Swap", fontSize = 12.sp, color = TextSecondary)
                    }
                    Text("${systemInfo.zramUsedMb}MB / ${systemInfo.zramTotalMb}MB", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { zramProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = NeonGreen,
                    trackColor = DarkSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Grid Info
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    DetailItem(label = "Model", value = systemInfo.deviceModel)
                    DetailItem(label = "CPU Arch", value = systemInfo.cpuArchitecture)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    DetailItem(label = "Kernel", value = systemInfo.kernelVersion)
                    DetailItem(label = "Treble / Slot", value = if (systemInfo.isTrebleSupported) "Treble (${systemInfo.abSlot})" else "No Treble")
                }
            }
        }

        // Reboot Quick Power Actions
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, DarkCardBorder, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = NeonRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reboot & Power Controls", fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onReboot("reboot") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                    ) {
                        Text("Reboot", fontSize = 11.sp, color = TextPrimary)
                    }

                    Button(
                        onClick = { onReboot("recovery") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                    ) {
                        Text("Recovery", fontSize = 11.sp, color = KingGoldPrimary)
                    }

                    Button(
                        onClick = { onReboot("bootloader") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                    ) {
                        Text("Bootloader", fontSize = 11.sp, color = NeonCyan)
                    }

                    Button(
                        onClick = { onReboot("soft") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
                    ) {
                        Text("Soft Reboot", fontSize = 11.sp, color = NeonGreen)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 10.sp, color = TextMuted)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 11.sp, color = TextMuted)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}
