package com.kingsu.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.kingsu.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppManagerRepository(private val context: Context) {

    suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        try {
            val pm = context.packageManager
            val appsList = mutableListOf<AppInfo>()

            try {
                val packages = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getInstalledPackages(0)
                }

                for (pkg in packages) {
                    try {
                        val appInfo = pkg.applicationInfo ?: continue
                        val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                        val label = try {
                            appInfo.loadLabel(pm).toString()
                        } catch (_: Throwable) {
                            pkg.packageName ?: "App"
                        }
                        val isDisabled = !appInfo.enabled

                        appsList.add(
                            AppInfo(
                                packageName = pkg.packageName ?: "",
                                label = label,
                                versionName = pkg.versionName ?: "1.0",
                                isSystemApp = isSystem,
                                isDisabled = isDisabled
                            )
                        )
                    } catch (_: Throwable) {}
                }
            } catch (_: Throwable) {
                // Fallback to getInstalledApplications
                val applications = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getInstalledApplications(0)
                }

                for (appInfo in applications) {
                    try {
                        val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                        val label = try {
                            appInfo.loadLabel(pm).toString()
                        } catch (_: Throwable) {
                            appInfo.packageName ?: "App"
                        }
                        val isDisabled = !appInfo.enabled

                        appsList.add(
                            AppInfo(
                                packageName = appInfo.packageName ?: "",
                                label = label,
                                versionName = "1.0",
                                isSystemApp = isSystem,
                                isDisabled = isDisabled
                            )
                        )
                    } catch (_: Throwable) {}
                }
            }

            appsList.sortedBy { it.label.lowercase() }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    suspend fun freezeApp(packageName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val res = ShellExecutor.runCommand("pm disable $packageName", useRoot = true)
            res.isSuccess
        } catch (_: Throwable) {
            false
        }
    }

    suspend fun unfreezeApp(packageName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val res = ShellExecutor.runCommand("pm enable $packageName", useRoot = true)
            res.isSuccess
        } catch (_: Throwable) {
            false
        }
    }

    suspend fun clearAppData(packageName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val res = ShellExecutor.runCommand("pm clear $packageName", useRoot = true)
            res.isSuccess
        } catch (_: Throwable) {
            false
        }
    }

    suspend fun uninstallApp(packageName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val res = ShellExecutor.runCommand("pm uninstall $packageName", useRoot = true)
            res.isSuccess
        } catch (_: Throwable) {
            false
        }
    }
}
