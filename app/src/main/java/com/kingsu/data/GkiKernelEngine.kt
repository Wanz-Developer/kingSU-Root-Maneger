package com.kingsu.data

import com.kingsu.model.GkiKernelInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object GkiKernelEngine {

    suspend fun detectGkiInfo(): GkiKernelInfo = withContext(Dispatchers.IO) {
        try {
            var kernelRelease = System.getProperty("os.version") ?: ""
            
            try {
                val procVer = File("/proc/version")
                if (procVer.exists()) {
                    val text = procVer.readText().trim()
                    if (text.isNotEmpty()) {
                        val parts = text.split("\\s+".toRegex())
                        if (parts.size >= 3) {
                            kernelRelease = parts[2]
                        }
                    }
                }
            } catch (_: Throwable) {}

            val releaseLower = kernelRelease.lowercase()
            val isGki2 = releaseLower.contains("gki") || releaseLower.contains("android12") ||
                    releaseLower.contains("android13") || releaseLower.contains("android14") ||
                    releaseLower.contains("android15") || releaseLower.startsWith("5.10") ||
                    releaseLower.startsWith("5.15") || releaseLower.startsWith("6.1") || releaseLower.startsWith("6.6")

            val isGki1 = releaseLower.contains("android11") || releaseLower.startsWith("5.4")

            val gkiVersionStr = when {
                isGki2 -> "GKI 2.0 (Kernel ${kernelRelease.take(15)})"
                isGki1 -> "GKI 1.0 (Kernel ${kernelRelease.take(15)})"
                else -> "Non-GKI Legacy Kernel"
            }

            // Check Kallsyms symbols
            val kallsymsCheck = ShellExecutor.runCommand("grep -E 'sys_call_table|security_file_open' /proc/kallsyms", useRoot = true)
            val hasKallsyms = kallsymsCheck.isSuccess && kallsymsCheck.stdout.isNotEmpty()
            val hasSyscall = kallsymsCheck.stdout.contains("sys_call_table")
            val hasSecurityOpen = kallsymsCheck.stdout.contains("security_file_open")

            val recommended = when {
                isGki2 -> "KernelSU LKM Modul & APatch kpimg (GKI 2.0 Direct Hook)"
                isGki1 -> "init_boot.img Ramdisk Patching (GKI 1.0)"
                else -> "boot.img Magisk Ramdisk Patch (Legacy Kernel)"
            }

            GkiKernelInfo(
                isGkiSupported = isGki1 || isGki2,
                gkiVersion = gkiVersionStr,
                kernelRelease = kernelRelease,
                kallsymsAvailable = hasKallsyms,
                hasSyscallTable = hasSyscall,
                hasSecurityFileOpen = hasSecurityOpen,
                recommendedMethod = recommended
            )
        } catch (_: Throwable) {
            GkiKernelInfo()
        }
    }
}
