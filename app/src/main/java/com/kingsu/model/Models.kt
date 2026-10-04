package com.kingsu.model

enum class RootSolutionType(val displayName: String) {
    MAGISK("MagiskSU"),
    KERNEL_SU("KernelSU"),
    APATCH("APatch"),
    LEGACY_SU("SuperSU / Legacy SU"),
    NONE("Not Rooted")
}

enum class SELinuxMode(val displayName: String) {
    ENFORCING("Enforcing"),
    PERMISSIVE("Permissive"),
    DISABLED("Disabled"),
    UNKNOWN("Unknown")
}

data class RootStatus(
    val isRooted: Boolean = false,
    val suBinaryPath: String = "Not Found",
    val solutionType: RootSolutionType = RootSolutionType.NONE,
    val suVersion: String = "N/A",
    val isBusyBoxInstalled: Boolean = false,
    val busyBoxVersion: String = "N/A",
    val seLinuxMode: SELinuxMode = SELinuxMode.UNKNOWN,
    val hasGrantPermission: Boolean = false
)

data class SystemInfo(
    val cpuArchitecture: String = "Unknown",
    val kernelVersion: String = "Unknown",
    val deviceModel: String = "Unknown",
    val androidVersion: String = "Unknown",
    val sdkVersion: Int = 0,
    val ramTotalMb: Long = 0L,
    val ramAvailableMb: Long = 0L,
    val zramTotalMb: Long = 0L,
    val zramUsedMb: Long = 0L,
    val batteryTemp: Float = 0f,
    val isTrebleSupported: Boolean = false,
    val abSlot: String = "N/A"
)

enum class TweakCategory(val title: String) {
    PERFORMANCE("Performa & CPU"),
    GAMING("Gaming Mode"),
    NETWORK("Jaringan & TCP"),
    MEMORY("Memori & ZRAM"),
    SYSTEM("Sistem & Cache")
}

data class TweakOption(
    val id: String,
    val title: String,
    val description: String,
    val category: TweakCategory,
    val command: String,
    val isApplied: Boolean = false
)

data class AppInfo(
    val packageName: String,
    val label: String,
    val versionName: String = "1.0",
    val isSystemApp: Boolean = false,
    val isDisabled: Boolean = false
)

data class SystemProperty(
    val key: String,
    val value: String
)

data class ShellResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String
) {
    val isSuccess: Boolean get() = exitCode == 0
}

data class TerminalEntry(
    val id: Long = System.currentTimeMillis(),
    val command: String,
    val output: String,
    val isError: Boolean = false
)

data class KingSUModule(
    val id: String,
    val name: String,
    val version: String = "1.0",
    val versionCode: Int = 1,
    val author: String = "Unknown",
    val description: String = "",
    val isEnabled: Boolean = true,
    val moduleDir: String = ""
)

data class CloakedApp(
    val packageName: String,
    val label: String,
    val isCloaked: Boolean = false
)

data class StealthConfig(
    val isZygiskEnabled: Boolean = true,
    val isDenyListActive: Boolean = true,
    val isAppDisguised: Boolean = false,
    val disguisedName: String = "KingSU",
    val isBiometricLockEnabled: Boolean = false
)

data class PatchedBootImgInfo(
    val exists: Boolean = false,
    val isInitBoot: Boolean = false,
    val fileName: String = "kingsu_patched_boot.img",
    val partitionTarget: String = "boot",
    val path: String = "/sdcard/Download/kingsu_patched_boot.img",
    val sizeMb: Float = 0f,
    val sha256: String = "N/A",
    val fastbootCmd: String = "fastboot flash boot kingsu_patched_boot.img"
)

data class GkiKernelInfo(
    val isGkiSupported: Boolean = false,
    val gkiVersion: String = "Non-GKI / Legacy Kernel",
    val kernelRelease: String = "Unknown",
    val kallsymsAvailable: Boolean = false,
    val hasSyscallTable: Boolean = false,
    val hasSecurityFileOpen: Boolean = false,
    val recommendedMethod: String = "Boot Ramdisk Patch"
)





