package com.kingsu.data

import android.content.Context
import com.kingsu.model.CloakedApp
import com.kingsu.model.StealthConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StealthRepository {

    suspend fun getStealthConfig(): StealthConfig = withContext(Dispatchers.IO) {
        try {
            val zygiskCheck = ShellExecutor.runCommand("getprop perssys.zygisk", useRoot = false)
            val isZygisk = zygiskCheck.stdout.trim() == "1" || zygiskCheck.stdout.trim() == "true"

            val denylistCheck = ShellExecutor.runCommand("[ -f /data/adb/denylist ] && echo 'active' || echo 'inactive'", useRoot = true)
            val isDenyList = denylistCheck.stdout.trim() == "active" || denylistCheck.isSuccess

            StealthConfig(
                isZygiskEnabled = isZygisk,
                isDenyListActive = isDenyList,
                isAppDisguised = false,
                disguisedName = "KingSU Console",
                isBiometricLockEnabled = false
            )
        } catch (_: Throwable) {
            StealthConfig()
        }
    }

    suspend fun getCloakedApps(context: Context): List<CloakedApp> = withContext(Dispatchers.IO) {
        try {
            val appManager = AppManagerRepository(context)
            val installed = appManager.getInstalledApps()

            val denylistRes = ShellExecutor.runCommand("cat /data/adb/denylist || cat /data/adb/ksu/denylist", useRoot = true)
            val denylistSet = if (denylistRes.isSuccess) {
                denylistRes.stdout.lines().map { it.trim() }.toSet()
            } else emptySet()

            installed.map { app ->
                CloakedApp(
                    packageName = app.packageName,
                    label = app.label,
                    isCloaked = denylistSet.contains(app.packageName)
                )
            }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    suspend fun toggleAppCloak(packageName: String, cloak: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            val cmd = if (cloak) {
                "magisk --denylist add $packageName || ksud denylist add $packageName || echo '$packageName' >> /data/adb/denylist"
            } else {
                "magisk --denylist rm $packageName || ksud denylist rm $packageName || sed -i '/$packageName/d' /data/adb/denylist"
            }
            val res = ShellExecutor.runCommand(cmd, useRoot = true)
            res.isSuccess
        } catch (_: Throwable) {
            false
        }
    }

    suspend fun toggleZygisk(enable: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            val valStr = if (enable) "1" else "0"
            val cmd = "setprop perssys.zygisk $valStr; magisk zygisk $valStr"
            val res = ShellExecutor.runCommand(cmd, useRoot = true)
            res.isSuccess
        } catch (_: Throwable) {
            false
        }
    }

    suspend fun toggleDenyList(enable: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            val cmd = if (enable) {
                "magisk --denylist enable || touch /data/adb/denylist"
            } else {
                "magisk --denylist disable || rm -f /data/adb/denylist"
            }
            val res = ShellExecutor.runCommand(cmd, useRoot = true)
            res.isSuccess
        } catch (_: Throwable) {
            false
        }
    }
}
