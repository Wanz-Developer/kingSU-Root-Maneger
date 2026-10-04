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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeveloperMode
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kingsu.model.GkiKernelInfo
import com.kingsu.model.PatchedBootImgInfo
import com.kingsu.model.RootStatus
import com.kingsu.ui.theme.DarkCardBorder
import com.kingsu.ui.theme.DarkSurface
import com.kingsu.ui.theme.DarkSurfaceVariant
import com.kingsu.ui.theme.KingGoldPrimary
import com.kingsu.ui.theme.NeonCyan
import com.kingsu.ui.theme.NeonGreen
import com.kingsu.ui.theme.TextMuted
import com.kingsu.ui.theme.TextPrimary
import com.kingsu.ui.theme.TextSecondary

@Composable
fun RootInstallerScreen(
    rootStatus: RootStatus,
    patchedBootInfo: PatchedBootImgInfo = PatchedBootImgInfo(),
    gkiInfo: GkiKernelInfo = GkiKernelInfo(),
    onInstallKernelLkm: () -> Unit,
    onSelectBootImgToPatch: () -> Unit,
    onGenerateFlashScript: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Metode Install Root Langsung",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = KingGoldPrimary
                )
            )
            Text(
                text = "Pengondisan GKI, Injeksi Kernel LKM, Patch boot/init_boot & Sinkronisasi Skrip",
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )
        }

        // GKI (Generic Kernel Image) Analysis Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, KingGoldPrimary, RoundedCornerShape(12.dp)),
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
                        Icon(Icons.Default.Memory, contentDescription = null, tint = KingGoldPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Analisis Kernel GKI System", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                            Text(gkiInfo.gkiVersion, fontSize = 11.sp, color = NeonCyan)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (gkiInfo.isGkiSupported) NeonGreen.copy(alpha = 0.2f) else DarkSurfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = if (gkiInfo.isGkiSupported) "GKI READY" else "LEGACY KERNEL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (gkiInfo.isGkiSupported) NeonGreen else TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("Kernel Release: ${gkiInfo.kernelRelease}", fontSize = 11.sp, color = TextSecondary)
                Text("Kallsyms Hooks: ${if (gkiInfo.kallsymsAvailable) "Tersedia (/proc/kallsyms)" else "Tidak terdeteksi"}", fontSize = 11.sp, color = TextMuted)

                Spacer(modifier = Modifier.height(8.dp))
                Text("Rekomendasi Metode Root:", fontSize = 10.sp, color = TextMuted)
                Text(
                    text = gkiInfo.recommendedMethod,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonGreen
                )
            }
        }

        // Patched Boot/Init_Boot Image File Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, if (patchedBootInfo.exists) NeonGreen else DarkCardBorder, RoundedCornerShape(12.dp)),
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
                        Icon(
                            imageVector = if (patchedBootInfo.exists) Icons.Default.CheckCircle else Icons.Default.Storage,
                            contentDescription = null,
                            tint = if (patchedBootInfo.exists) NeonGreen else KingGoldPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (patchedBootInfo.exists) patchedBootInfo.fileName else "kingsu_patched_boot.img / init_boot.img",
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (patchedBootInfo.exists) "File Patched Siap Flash" else "Belum Dibuat / Siap Patch",
                                fontSize = 11.sp,
                                color = if (patchedBootInfo.exists) NeonGreen else TextMuted
                            )
                        }
                    }

                    if (patchedBootInfo.exists) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonCyan.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "PARTITION: ${patchedBootInfo.partitionTarget.uppercase()}",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        }
                    }
                }

                if (patchedBootInfo.exists) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Lokasi: ${patchedBootInfo.path}", fontSize = 11.sp, color = NeonCyan)
                    Text("Ukuran File: %.2f MB".format(patchedBootInfo.sizeMb), fontSize = 11.sp, color = TextSecondary)
                    Text("SHA256: ${patchedBootInfo.sha256}", fontSize = 11.sp, color = TextMuted)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$ ${patchedBootInfo.fastbootCmd}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkSurfaceVariant)
                            .padding(6.dp)
                    )
                }
            }
        }

        // Method 1: Kernel LKM Direct Hooking
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, KingGoldPrimary, RoundedCornerShape(12.dp)),
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
                        Icon(Icons.Default.DeveloperMode, contentDescription = null, tint = KingGoldPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("1. Kernel LKM Direct Injection", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                            Text("KernelSU / APatch LKM Hooking", fontSize = 11.sp, color = NeonCyan)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Menyuntikkan modul Loadable Kernel Module (LKM) langsung ke kernel Android yang sedang berjalan secara dinamis tanpa mengubah partisi boot.img.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onInstallKernelLkm,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = KingGoldPrimary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Jalankan Injeksi Kernel LKM", color = DarkSurface, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Method 2: Boot.img / Init_boot.img Ramdisk Patcher
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
                        Icon(Icons.Default.Storage, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("2. Patch boot.img atau init_boot.img", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                            Text("Otomatis Deteksi boot / init_boot Engine", fontSize = 11.sp, color = NeonGreen)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Membongkar file boot.img atau init_boot.img, menyuntikkan binary su ke ramdisk, dan menghasilkan kingsu_patched_boot.img / kingsu_patched_init_boot.img.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onSelectBootImgToPatch,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Pilih File boot.img atau init_boot.img", color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Method 3: Synchronized Fastboot Script Generator
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
                        Icon(Icons.Default.Build, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("3. Sinkronisasi Script otomatis.sh & .bat", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                            Text("Otomatisasi Nama File & Partisi Fastboot", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Menghasilkan flash_kingsu.sh dan flash_kingsu.bat yang tersinkronisasi otomatis mengenali partisi (boot / init_boot) dan nama file image ter-patch.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onGenerateFlashScript,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Sinkronkan Skrip Flash otomatis.sh & .bat", color = NeonGreen, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
