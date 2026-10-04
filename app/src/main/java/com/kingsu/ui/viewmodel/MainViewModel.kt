package com.kingsu.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kingsu.data.AppManagerRepository
import com.kingsu.data.ModuleManagerRepository
import com.kingsu.data.RootRepository
import com.kingsu.data.ShellExecutor
import com.kingsu.model.AppInfo
import com.kingsu.model.KingSUModule
import com.kingsu.model.RootStatus
import com.kingsu.model.SELinuxMode
import com.kingsu.model.SystemInfo
import com.kingsu.model.SystemProperty
import com.kingsu.model.TerminalEntry
import com.kingsu.model.TweakCategory
import com.kingsu.model.TweakOption
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val rootRepository = RootRepository()
    private val appRepository = AppManagerRepository(application)
    private val moduleRepository = ModuleManagerRepository()
    private val stealthRepository = com.kingsu.data.StealthRepository()

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _isLoading.value = false
        _toastMessage.value = "Error: ${throwable.localizedMessage ?: "Operation failed"}"
    }

    private val _rootStatus = MutableStateFlow(RootStatus())
    val rootStatus: StateFlow<RootStatus> = _rootStatus.asStateFlow()

    private val _stealthConfig = MutableStateFlow(com.kingsu.model.StealthConfig())
    val stealthConfig: StateFlow<com.kingsu.model.StealthConfig> = _stealthConfig.asStateFlow()

    private val _cloakedApps = MutableStateFlow<List<com.kingsu.model.CloakedApp>>(emptyList())
    val cloakedApps: StateFlow<List<com.kingsu.model.CloakedApp>> = _cloakedApps.asStateFlow()

    private val _patchedBootImgInfo = MutableStateFlow(com.kingsu.model.PatchedBootImgInfo())
    val patchedBootImgInfo: StateFlow<com.kingsu.model.PatchedBootImgInfo> = _patchedBootImgInfo.asStateFlow()

    private val _gkiInfo = MutableStateFlow(com.kingsu.model.GkiKernelInfo())
    val gkiInfo: StateFlow<com.kingsu.model.GkiKernelInfo> = _gkiInfo.asStateFlow()

    private val _systemInfo = MutableStateFlow(SystemInfo())
    val systemInfo: StateFlow<SystemInfo> = _systemInfo.asStateFlow()

    private val _systemProperties = MutableStateFlow<List<SystemProperty>>(emptyList())
    val systemProperties: StateFlow<List<SystemProperty>> = _systemProperties.asStateFlow()

    private val _tweaks = MutableStateFlow<List<TweakOption>>(emptyList())
    val tweaks: StateFlow<List<TweakOption>> = _tweaks.asStateFlow()

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    private val _installedModules = MutableStateFlow<List<KingSUModule>>(emptyList())
    val installedModules: StateFlow<List<KingSUModule>> = _installedModules.asStateFlow()

    private val _terminalLogs = MutableStateFlow<List<TerminalEntry>>(
        listOf(
            TerminalEntry(
                command = "system_init",
                output = "KingSU Root Terminal Session Initialized.\nType commands below or click macro shortcuts."
            )
        )
    )
    val terminalLogs: StateFlow<List<TerminalEntry>> = _terminalLogs.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init {
        setupInitialTweaks()
        refreshAll()
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun refreshAll() {
        viewModelScope.launch(exceptionHandler) {
            _isLoading.value = true
            try {
                _rootStatus.value = rootRepository.checkRootStatus()
                _systemInfo.value = rootRepository.getSystemInfo()
                _systemProperties.value = rootRepository.getSystemProperties()
                _installedModules.value = moduleRepository.getInstalledModules()
                _stealthConfig.value = stealthRepository.getStealthConfig()
                _cloakedApps.value = stealthRepository.getCloakedApps(getApplication())
                _patchedBootImgInfo.value = com.kingsu.data.KernelBootPatcher.getPatchedBootImgInfo()
                _gkiInfo.value = com.kingsu.data.GkiKernelEngine.detectGkiInfo()
                refreshAppsInternal()
            } catch (_: Throwable) {
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleZygisk(enable: Boolean) {
        viewModelScope.launch(exceptionHandler) {
            val ok = stealthRepository.toggleZygisk(enable)
            _toastMessage.value = if (ok) "Zygisk ${if (enable) "diaktifkan" else "dinonaktifkan"}" else "Gagal mengubah Zygisk"
            _stealthConfig.value = stealthRepository.getStealthConfig()
        }
    }

    fun toggleDenyList(enable: Boolean) {
        viewModelScope.launch(exceptionHandler) {
            val ok = stealthRepository.toggleDenyList(enable)
            _toastMessage.value = if (ok) "Root Cloak DenyList ${if (enable) "diaktifkan" else "dinonaktifkan"}" else "Gagal mengubah DenyList"
            _stealthConfig.value = stealthRepository.getStealthConfig()
        }
    }

    fun toggleAppCloak(packageName: String, cloak: Boolean) {
        viewModelScope.launch(exceptionHandler) {
            val ok = stealthRepository.toggleAppCloak(packageName, cloak)
            _toastMessage.value = if (ok) "Aplikasi $packageName ${if (cloak) "disembunyikan dari Root" else "diperlihatkan"}" else "Gagal mengubah status penyamaran"
            _cloakedApps.value = stealthRepository.getCloakedApps(getApplication())
        }
    }

    private suspend fun refreshAppsInternal() {
        try {
            _installedApps.value = appRepository.getInstalledApps()
        } catch (_: Throwable) {}
    }

    fun refreshApps() {
        viewModelScope.launch(exceptionHandler) {
            _isLoading.value = true
            try {
                refreshAppsInternal()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleModule(module: KingSUModule, enable: Boolean) {
        viewModelScope.launch(exceptionHandler) {
            val ok = moduleRepository.toggleModule(module, enable)
            if (ok) {
                _toastMessage.value = if (enable) "Modul ${module.name} diaktifkan" else "Modul ${module.name} dinonaktifkan"
                _installedModules.value = moduleRepository.getInstalledModules()
            } else {
                _toastMessage.value = "Gagal mengubah status modul"
            }
        }
    }

    fun installModuleZip(zipFilePath: String) {
        viewModelScope.launch(exceptionHandler) {
            _toastMessage.value = "Memasang modul dari $zipFilePath..."
            val (ok, msg) = moduleRepository.installZipModule(zipFilePath)
            _toastMessage.value = msg
            if (ok) {
                _installedModules.value = moduleRepository.getInstalledModules()
            }
        }
    }

    fun removeModule(module: KingSUModule) {
        viewModelScope.launch(exceptionHandler) {
            val ok = moduleRepository.removeModule(module)
            if (ok) {
                _toastMessage.value = "Modul ${module.name} ditandai untuk dihapus saat reboot"
                _installedModules.value = moduleRepository.getInstalledModules()
            } else {
                _toastMessage.value = "Gagal menghapus modul"
            }
        }
    }

    fun installKernelLkm() {
        viewModelScope.launch(exceptionHandler) {
            _toastMessage.value = "Menjalankan Kernel LKM Injection..."
            val res = com.kingsu.data.KernelBootPatcher.patchKernelLkm()
            _toastMessage.value = if (res.isSuccess) "Kernel LKM Hook Berhasil Dipasang!" else "Gagal meng-inject Kernel LKM (Membutuhkan modul kernel kompatibel)"
            appendTerminalLog("lkm_patch", res.stdout.ifEmpty { res.stderr }, !res.isSuccess)
        }
    }

    fun patchBootImg(path: String) {
        viewModelScope.launch(exceptionHandler) {
            _toastMessage.value = "Membongkar & patching boot image: $path..."
            val res = com.kingsu.data.KernelBootPatcher.patchBootImage(path)
            _toastMessage.value = if (res.isSuccess) "Boot Image Berhasil Di-patch!" else "Gagal mempatch boot image"
            _patchedBootImgInfo.value = com.kingsu.data.KernelBootPatcher.getPatchedBootImgInfo()
            appendTerminalLog("boot_patch", res.stdout.ifEmpty { res.stderr }, !res.isSuccess)
        }
    }

    fun generateFlashScript() {
        viewModelScope.launch(exceptionHandler) {
            _toastMessage.value = "Membuat & menyinkronkan skrip Fastboot flash..."
            val res = com.kingsu.data.KernelBootPatcher.generateFlashScripts()
            _toastMessage.value = if (res.isSuccess) "Skrip flash_kingsu.sh & .bat tersimpan di Download!" else "Gagal membuat skrip"
            _patchedBootImgInfo.value = com.kingsu.data.KernelBootPatcher.getPatchedBootImgInfo()
        }
    }

    private fun setupInitialTweaks() {
        _tweaks.value = listOf(
            TweakOption(
                id = "perf_boost",
                title = "CPU & GPU Max Performance",
                description = "Set CPU governor ke performance, kunci frekuensi tinggi & flush cache RAM",
                category = TweakCategory.PERFORMANCE,
                command = "echo performance | tee /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; sync; echo 3 > /proc/sys/vm/drop_caches"
            ),
            TweakOption(
                id = "gaming_mode",
                title = "Ultra Gaming & Thermal Boost",
                description = "Nonaktifkan throttling thermal sementara & prioritaskan penjadwalan CPU",
                category = TweakCategory.GAMING,
                command = "stop thermal-engine; stop thermald; setprop sys.io.scheduler cfq"
            ),
            TweakOption(
                id = "tcp_bbr",
                title = "TCP Network Turbo (BBR/Cubic)",
                description = "Aktifkan TCP BBR / Cubic congestion control & tingkatkan buffer jaringan",
                category = TweakCategory.NETWORK,
                command = "sysctl -w net.ipv4.tcp_congestion_control=bbr || sysctl -w net.ipv4.tcp_congestion_control=cubic"
            ),
            TweakOption(
                id = "dns_cloudflare",
                title = "Cloudflare 1.1.1.1 Ultra DNS",
                description = "Atur DNS sistem ke Cloudflare DNS tercepat (1.1.1.1 & 1.0.0.1)",
                category = TweakCategory.NETWORK,
                command = "setprop net.dns1 1.1.1.1; setprop net.dns2 1.0.0.1; iptables -t nat -A OUTPUT -p udp --dport 53 -j DNAT --to-destination 1.1.1.1"
            ),
            TweakOption(
                id = "zram_swap",
                title = "ZRAM & Swappiness Aggressive",
                description = "Tingkatkan nilai Swappiness ke 80% untuk multitasking lebih lancar",
                category = TweakCategory.MEMORY,
                command = "sysctl -w vm.swappiness=80; echo 100 > /proc/sys/vm/vfs_cache_pressure"
            ),
            TweakOption(
                id = "art_cache_clean",
                title = "Flush ART & Dalvik Cache",
                description = "Bersihkan sisa cache aplikasi untuk menghemat penyimpanan internal",
                category = TweakCategory.SYSTEM,
                command = "rm -rf /data/dalvik-cache/*; rm -rf /cache/*"
            )
        )
    }

    fun executeTweak(tweak: TweakOption) {
        viewModelScope.launch(exceptionHandler) {
            _toastMessage.value = "Menjalankan Tweak: ${tweak.title}..."
            val res = ShellExecutor.runCommand(tweak.command, useRoot = true)
            
            val logOutput = if (res.isSuccess) {
                "OK: ${if (res.stdout.isNotEmpty()) res.stdout else "Berhasil diterapkan!"}"
            } else {
                "ERROR (${res.exitCode}): ${res.stderr}"
            }

            appendTerminalLog("tweak:${tweak.id}", logOutput, !res.isSuccess)

            _tweaks.value = _tweaks.value.map {
                if (it.id == tweak.id) it.copy(isApplied = res.isSuccess) else it
            }

            _toastMessage.value = if (res.isSuccess) "Tweak [${tweak.title}] Berhasil!" else "Gagal menerapkan tweak (Butuh Root)"
        }
    }

    fun toggleSELinux(targetMode: SELinuxMode) {
        viewModelScope.launch(exceptionHandler) {
            val success = rootRepository.toggleSELinux(targetMode)
            if (success) {
                _toastMessage.value = "SELinux diubah ke ${targetMode.displayName}"
                _rootStatus.value = rootRepository.checkRootStatus()
            } else {
                _toastMessage.value = "Gagal mengubah mode SELinux"
            }
        }
    }

    fun executeTerminalCommand(cmd: String) {
        if (cmd.trim().isEmpty()) return
        viewModelScope.launch(exceptionHandler) {
            val res = ShellExecutor.runCommand(cmd, useRoot = true)
            val output = when {
                res.stdout.isNotEmpty() -> res.stdout
                res.stderr.isNotEmpty() -> "ERR: ${res.stderr}"
                else -> "Exit Code: ${res.exitCode}"
            }
            appendTerminalLog(cmd, output, !res.isSuccess)
        }
    }

    fun clearTerminalLogs() {
        _terminalLogs.value = emptyList()
    }

    fun freezeApp(pkgName: String) {
        viewModelScope.launch(exceptionHandler) {
            val ok = appRepository.freezeApp(pkgName)
            if (ok) {
                _toastMessage.value = "Aplikasi $pkgName dibekukan"
                refreshAppsInternal()
            } else {
                _toastMessage.value = "Gagal membekukan aplikasi"
            }
        }
    }

    fun unfreezeApp(pkgName: String) {
        viewModelScope.launch(exceptionHandler) {
            val ok = appRepository.unfreezeApp(pkgName)
            if (ok) {
                _toastMessage.value = "Aplikasi $pkgName diaktifkan kembali"
                refreshAppsInternal()
            } else {
                _toastMessage.value = "Gagal mengaktifkan aplikasi"
            }
        }
    }

    fun clearAppData(pkgName: String) {
        viewModelScope.launch(exceptionHandler) {
            val ok = appRepository.clearAppData(pkgName)
            if (ok) {
                _toastMessage.value = "Data $pkgName dibersihkan"
                refreshAppsInternal()
            } else {
                _toastMessage.value = "Gagal membersihkan data"
            }
        }
    }

    fun uninstallApp(pkgName: String) {
        viewModelScope.launch(exceptionHandler) {
            val ok = appRepository.uninstallApp(pkgName)
            if (ok) {
                _toastMessage.value = "Aplikasi $pkgName di-uninstall"
                refreshAppsInternal()
            } else {
                _toastMessage.value = "Gagal meng-uninstall aplikasi"
            }
        }
    }

    fun reboot(mode: String) {
        viewModelScope.launch(exceptionHandler) {
            _toastMessage.value = "Rebooting ($mode)..."
            rootRepository.rebootDevice(mode)
        }
    }

    private fun appendTerminalLog(cmd: String, output: String, isErr: Boolean = false) {
        val entry = TerminalEntry(command = cmd, output = output, isError = isErr)
        _terminalLogs.value = _terminalLogs.value + entry
    }
}
