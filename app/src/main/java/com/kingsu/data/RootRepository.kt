package com.kingsu.data

import android.os.Build
import com.kingsu.model.RootSolutionType
import com.kingsu.model.RootStatus
import com.kingsu.model.SELinuxMode
import com.kingsu.model.SystemInfo
import com.kingsu.model.SystemProperty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class RootRepository {

    private val suPaths = listOf(
        "/system/bin/su",
        "/system/xbin/su",
        "/sbin/su",
        "/vendor/bin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/system/sd/xbin/su"
    )

    suspend fun checkRootStatus(): RootStatus = withContext(Dispatchers.IO) {
        try {
            var foundSuPath = "Not Found"
            var isRooted = false

            for (path in suPaths) {
                try {
                    if (File(path).exists()) {
                        foundSuPath = path
                        isRooted = true
                        break
                    }
                } catch (_: Throwable) {}
            }

            if (!isRooted) {
                val whichRes = ShellExecutor.runCommand("which su", useRoot = false)
                if (whichRes.isSuccess && whichRes.stdout.isNotEmpty()) {
                    foundSuPath = whichRes.stdout.lines().firstOrNull() ?: "su"
                    isRooted = true
                }
            }

            val hasGrant = ShellExecutor.isRootAvailable()

            var solutionType = RootSolutionType.NONE
            var suVersion = "N/A"

            if (isRooted || hasGrant) {
                val ksuCheck = ShellExecutor.runCommand("ksud -V", useRoot = hasGrant)
                val apatchCheck = ShellExecutor.runCommand("apd -V", useRoot = hasGrant)
                val magiskCheck = ShellExecutor.runCommand("magisk -V", useRoot = hasGrant)

                when {
                    ksuCheck.isSuccess || safeFileExists("/data/adb/ksu") -> {
                        solutionType = RootSolutionType.KERNEL_SU
                        suVersion = if (ksuCheck.stdout.isNotEmpty()) ksuCheck.stdout else "KernelSU"
                    }
                    apatchCheck.isSuccess || safeFileExists("/data/adb/ap") -> {
                        solutionType = RootSolutionType.APATCH
                        suVersion = if (apatchCheck.stdout.isNotEmpty()) apatchCheck.stdout else "APatch"
                    }
                    magiskCheck.isSuccess || safeFileExists("/data/adb/magisk") -> {
                        solutionType = RootSolutionType.MAGISK
                        suVersion = if (magiskCheck.stdout.isNotEmpty()) magiskCheck.stdout else "Magisk"
                    }
                    else -> {
                        val suVerRes = ShellExecutor.runCommand("su -v", useRoot = hasGrant)
                        solutionType = RootSolutionType.LEGACY_SU
                        suVersion = if (suVerRes.isSuccess && suVerRes.stdout.isNotEmpty()) suVerRes.stdout else "SU Binary"
                    }
                }
            }

            // Check BusyBox
            val busyBoxRes = ShellExecutor.runCommand("busybox", useRoot = false)
            val isBusyBox = busyBoxRes.stdout.contains("BusyBox v") || safeFileExists("/system/xbin/busybox")
            val busyBoxVer = if (isBusyBox) {
                busyBoxRes.stdout.lines().firstOrNull()?.take(30) ?: "Installed"
            } else "Not Installed"

            // Check SELinux
            val seLinuxRes = ShellExecutor.runCommand("getenforce", useRoot = false)
            val seMode = when (seLinuxRes.stdout.lowercase().trim()) {
                "enforcing" -> SELinuxMode.ENFORCING
                "permissive" -> SELinuxMode.PERMISSIVE
                "disabled" -> SELinuxMode.DISABLED
                else -> SELinuxMode.UNKNOWN
            }

            RootStatus(
                isRooted = isRooted || hasGrant,
                suBinaryPath = foundSuPath,
                solutionType = solutionType,
                suVersion = suVersion,
                isBusyBoxInstalled = isBusyBox,
                busyBoxVersion = busyBoxVer,
                seLinuxMode = seMode,
                hasGrantPermission = hasGrant
            )
        } catch (e: Throwable) {
            RootStatus(
                isRooted = false,
                suBinaryPath = "Not Found",
                solutionType = RootSolutionType.NONE,
                suVersion = "N/A",
                isBusyBoxInstalled = false,
                busyBoxVersion = "N/A",
                seLinuxMode = SELinuxMode.UNKNOWN,
                hasGrantPermission = false
            )
        }
    }

    suspend fun getSystemInfo(): SystemInfo = withContext(Dispatchers.IO) {
        try {
            val abi = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a"

            var kernel = "Linux " + (System.getProperty("os.version") ?: "Android Kernel")
            try {
                val procVersion = File("/proc/version")
                if (procVersion.exists()) {
                    val content = procVersion.readText().trim()
                    if (content.isNotEmpty()) {
                        kernel = content.split(" ").take(3).joinToString(" ")
                    }
                }
            } catch (_: Throwable) {}

            var totalRam = 0L
            var availRam = 0L
            try {
                val meminfo = File("/proc/meminfo")
                if (meminfo.exists()) {
                    meminfo.useLines { lines ->
                        lines.forEach { line ->
                            if (line.startsWith("MemTotal:")) {
                                totalRam = parseKbToMb(line)
                            } else if (line.startsWith("MemAvailable:")) {
                                availRam = parseKbToMb(line)
                            }
                        }
                    }
                }
            } catch (_: Throwable) {}

            var zramTotal = 0L
            var zramUsed = 0L
            try {
                val swaps = File("/proc/swaps")
                if (swaps.exists()) {
                    swaps.useLines { lines ->
                        lines.forEach { line ->
                            if (line.contains("zram") || line.contains("partition") || line.contains("file")) {
                                val parts = line.split("\\s+".toRegex())
                                if (parts.size >= 4) {
                                    zramTotal = (parts[2].toLongOrNull() ?: 0L) / 1024L
                                    zramUsed = (parts[3].toLongOrNull() ?: 0L) / 1024L
                                }
                            }
                        }
                    }
                }
            } catch (_: Throwable) {}

            val trebleRes = ShellExecutor.runCommand("getprop ro.treble.enabled", useRoot = false)
            val isTreble = trebleRes.stdout.trim().lowercase() == "true"

            val abSlotRes = ShellExecutor.runCommand("getprop ro.boot.slot_suffix", useRoot = false)
            val abSlot = if (abSlotRes.stdout.isNotEmpty()) abSlotRes.stdout.trim() else "Single Partition"

            SystemInfo(
                cpuArchitecture = abi,
                kernelVersion = kernel,
                deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
                androidVersion = "Android ${Build.VERSION.RELEASE}",
                sdkVersion = Build.VERSION.SDK_INT,
                ramTotalMb = totalRam,
                ramAvailableMb = availRam,
                zramTotalMb = zramTotal,
                zramUsedMb = zramUsed,
                batteryTemp = 36.5f,
                isTrebleSupported = isTreble,
                abSlot = abSlot
            )
        } catch (e: Throwable) {
            SystemInfo(
                cpuArchitecture = Build.SUPPORTED_ABIS.firstOrNull() ?: "arm64-v8a",
                kernelVersion = "Linux Kernel",
                deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}",
                androidVersion = "Android ${Build.VERSION.RELEASE}",
                sdkVersion = Build.VERSION.SDK_INT
            )
        }
    }

    suspend fun getSystemProperties(): List<SystemProperty> = withContext(Dispatchers.IO) {
        try {
            val res = ShellExecutor.runCommand("getprop", useRoot = false)
            if (!res.isSuccess || res.stdout.isEmpty()) return@withContext emptyList()

            val list = mutableListOf<SystemProperty>()
            res.stdout.lines().forEach { line ->
                val trimmed = line.trim()
                if (trimmed.startsWith("[") && trimmed.contains("]: [")) {
                    val keyEnd = trimmed.indexOf("]: [")
                    val key = trimmed.substring(1, keyEnd)
                    val valStart = keyEnd + 4
                    val valEnd = if (trimmed.endsWith("]")) trimmed.length - 1 else trimmed.length
                    if (valStart <= valEnd) {
                        val value = trimmed.substring(valStart, valEnd)
                        list.add(SystemProperty(key = key, value = value))
                    }
                }
            }
            list
        } catch (e: Throwable) {
            emptyList()
        }
    }

    suspend fun toggleSELinux(targetMode: SELinuxMode): Boolean = withContext(Dispatchers.IO) {
        try {
            val cmd = when (targetMode) {
                SELinuxMode.ENFORCING -> "setenforce 1"
                SELinuxMode.PERMISSIVE -> "setenforce 0"
                else -> return@withContext false
            }
            val res = ShellExecutor.runCommand(cmd, useRoot = true)
            res.isSuccess
        } catch (_: Throwable) {
            false
        }
    }

    suspend fun rebootDevice(mode: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val cmd = when (mode.lowercase()) {
                "recovery" -> "reboot recovery"
                "bootloader" -> "reboot bootloader"
                "soft" -> "setprop ctl.restart zygote"
                else -> "reboot"
            }
            val res = ShellExecutor.runCommand(cmd, useRoot = true)
            res.isSuccess
        } catch (_: Throwable) {
            false
        }
    }

    private fun safeFileExists(path: String): Boolean {
        return try {
            File(path).exists()
        } catch (_: Throwable) {
            false
        }
    }

    private fun parseKbToMb(line: String): Long {
        return try {
            val parts = line.split("\\s+".toRegex())
            if (parts.size >= 2) {
                (parts[1].toLongOrNull() ?: 0L) / 1024L
            } else 0L
        } catch (_: Throwable) {
            0L
        }
    }
}
