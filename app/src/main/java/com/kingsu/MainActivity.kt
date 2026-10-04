package com.kingsu

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kingsu.ui.screens.AppManagerScreen
import com.kingsu.ui.screens.DashboardScreen
import com.kingsu.ui.screens.ModulesScreen
import com.kingsu.ui.screens.RootInstallerScreen
import com.kingsu.ui.screens.ShellTerminalScreen
import com.kingsu.ui.screens.StealthScreen
import com.kingsu.ui.screens.TweaksScreen
import com.kingsu.ui.theme.DarkBackground
import com.kingsu.ui.theme.DarkCardBorder
import com.kingsu.ui.theme.DarkSurface
import com.kingsu.ui.theme.KingGoldPrimary
import com.kingsu.ui.theme.KingSUTheme
import com.kingsu.ui.theme.TextMuted
import com.kingsu.ui.viewmodel.MainViewModel
import java.io.File
import java.io.FileOutputStream

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KingSUTheme {
                MainApp()
            }
        }
    }
}

data class NavigationTab(
    val title: String,
    val icon: ImageVector
)

@Composable
fun MainApp(viewModel: MainViewModel = viewModel()) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val rootStatus by viewModel.rootStatus.collectAsState()
    val systemInfo by viewModel.systemInfo.collectAsState()
    val stealthConfig by viewModel.stealthConfig.collectAsState()
    val cloakedApps by viewModel.cloakedApps.collectAsState()
    val patchedBootInfo by viewModel.patchedBootImgInfo.collectAsState()
    val gkiInfo by viewModel.gkiInfo.collectAsState()
    val tweaks by viewModel.tweaks.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val installedModules by viewModel.installedModules.collectAsState()
    val terminalLogs by viewModel.terminalLogs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    val context = LocalContext.current

    val zipPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = copyUriToTempFile(context, it, "kingsu_mod_")
            if (file != null) {
                viewModel.installModuleZip(file.absolutePath)
            } else {
                Toast.makeText(context, "Gagal membuka file zip modul", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val bootImgPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = copyUriToTempFile(context, it, "boot_img_")
            if (file != null) {
                viewModel.patchBootImg(file.absolutePath)
            } else {
                Toast.makeText(context, "Gagal membuka file boot.img", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    val tabs = listOf(
        NavigationTab("Dashboard", Icons.Default.Dashboard),
        NavigationTab("Penyamaran", Icons.Default.VisibilityOff),
        NavigationTab("Install Root", Icons.Default.Shield),
        NavigationTab("Modules", Icons.Default.Extension),
        NavigationTab("Tweaks", Icons.Default.FlashOn),
        NavigationTab("Terminal", Icons.Default.Terminal),
        NavigationTab("Apps", Icons.Default.Apps)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { viewModel.selectTab(index) },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title, fontSize = 8.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = KingGoldPrimary,
                            selectedTextColor = KingGoldPrimary,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = DarkCardBorder
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen(
                    rootStatus = rootStatus,
                    systemInfo = systemInfo,
                    isLoading = isLoading,
                    onRefresh = { viewModel.refreshAll() },
                    onToggleSELinux = { viewModel.toggleSELinux(it) },
                    onReboot = { viewModel.reboot(it) }
                )
                1 -> StealthScreen(
                    stealthConfig = stealthConfig,
                    cloakedApps = cloakedApps,
                    onToggleZygisk = { viewModel.toggleZygisk(it) },
                    onToggleDenyList = { viewModel.toggleDenyList(it) },
                    onToggleAppCloak = { pkg, cloak -> viewModel.toggleAppCloak(pkg, cloak) }
                )
                2 -> RootInstallerScreen(
                    rootStatus = rootStatus,
                    patchedBootInfo = patchedBootInfo,
                    gkiInfo = gkiInfo,
                    onInstallKernelLkm = { viewModel.installKernelLkm() },
                    onSelectBootImgToPatch = { bootImgPickerLauncher.launch("*/*") },
                    onGenerateFlashScript = { viewModel.generateFlashScript() }
                )
                3 -> ModulesScreen(
                    modules = installedModules,
                    onToggleModule = { mod, enable -> viewModel.toggleModule(mod, enable) },
                    onRemoveModule = { mod -> viewModel.removeModule(mod) },
                    onInstallZipClick = { zipPickerLauncher.launch("application/zip") }
                )
                4 -> TweaksScreen(
                    tweaks = tweaks,
                    onApplyTweak = { viewModel.executeTweak(it) }
                )
                5 -> ShellTerminalScreen(
                    logs = terminalLogs,
                    onExecuteCommand = { viewModel.executeTerminalCommand(it) },
                    onClearLogs = { viewModel.clearTerminalLogs() }
                )
                6 -> AppManagerScreen(
                    apps = installedApps,
                    onFreezeApp = { viewModel.freezeApp(it) },
                    onUnfreezeApp = { viewModel.unfreezeApp(it) },
                    onClearData = { viewModel.clearAppData(it) },
                    onUninstallApp = { viewModel.uninstallApp(it) }
                )
            }
        }
    }
}

private fun copyUriToTempFile(context: android.content.Context, uri: Uri, prefix: String): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = File(context.cacheDir, "${prefix}${System.currentTimeMillis()}.img")
        val outputStream = FileOutputStream(tempFile)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        tempFile
    } catch (_: Exception) {
        null
    }
}
