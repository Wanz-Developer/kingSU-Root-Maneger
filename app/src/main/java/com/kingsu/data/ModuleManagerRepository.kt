package com.kingsu.data

import com.kingsu.model.KingSUModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ModuleManagerRepository {

    private val moduleDirs = listOf(
        "/data/adb/modules",
        "/data/adb/ksu/modules",
        "/data/adb/ap/modules"
    )

    suspend fun getInstalledModules(): List<KingSUModule> = withContext(Dispatchers.IO) {
        try {
            val modules = mutableListOf<KingSUModule>()

            for (baseDir in moduleDirs) {
                try {
                    val listRes = ShellExecutor.runCommand("ls $baseDir", useRoot = true)
                    if (listRes.isSuccess && listRes.stdout.isNotEmpty()) {
                        val dirNames = listRes.stdout.lines().filter { it.isNotBlank() }
                        for (dirName in dirNames) {
                            try {
                                val modPath = "$baseDir/$dirName"
                                val propRes = ShellExecutor.runCommand("cat $modPath/module.prop", useRoot = true)
                                
                                if (propRes.isSuccess && propRes.stdout.isNotEmpty()) {
                                    val props = parseModuleProp(propRes.stdout)
                                    val disableCheck = ShellExecutor.runCommand("[ -f $modPath/disable ] && echo 'disabled' || echo 'enabled'", useRoot = true)
                                    val isEnabled = disableCheck.stdout.trim() == "enabled"

                                    modules.add(
                                        KingSUModule(
                                            id = props["id"] ?: dirName,
                                            name = props["name"] ?: dirName,
                                            version = props["version"] ?: "1.0",
                                            versionCode = props["versionCode"]?.toIntOrNull() ?: 1,
                                            author = props["author"] ?: "KingSU Developer",
                                            description = props["description"] ?: "No description provided",
                                            isEnabled = isEnabled,
                                            moduleDir = modPath
                                        )
                                    )
                                }
                            } catch (_: Throwable) {}
                        }
                    }
                } catch (_: Throwable) {}
            }
            modules.distinctBy { it.id }
        } catch (_: Throwable) {
            emptyList()
        }
    }

    suspend fun toggleModule(module: KingSUModule, enable: Boolean): Boolean = withContext(Dispatchers.IO) {
        try {
            val cmd = if (enable) {
                "rm -f ${module.moduleDir}/disable"
            } else {
                "touch ${module.moduleDir}/disable"
            }
            val res = ShellExecutor.runCommand(cmd, useRoot = true)
            res.isSuccess
        } catch (_: Throwable) {
            false
        }
    }

    suspend fun installZipModule(zipFilePath: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val targetDir = "/data/adb/modules"
            val cmd = """
                mkdir -p $targetDir
                unzip -o $zipFilePath -d /data/local/tmp/kingsu_mod_temp
                if [ -f /data/local/tmp/kingsu_mod_temp/update-binary ]; then
                    sh /data/local/tmp/kingsu_mod_temp/update-binary 3 1 $zipFilePath
                    rm -rf /data/local/tmp/kingsu_mod_temp
                    echo "MODULE_INSTALL_SUCCESS"
                else
                    rm -rf /data/local/tmp/kingsu_mod_temp
                    echo "INVALID_MODULE_ZIP"
                fi
            """.trimIndent()

            val res = ShellExecutor.runCommand(cmd, useRoot = true)
            if (res.stdout.contains("MODULE_INSTALL_SUCCESS") || res.isSuccess) {
                Pair(true, "Modul berhasil dipasang!")
            } else {
                Pair(false, "Gagal memasang modul: ${res.stderr.ifEmpty { res.stdout }}")
            }
        } catch (e: Throwable) {
            Pair(false, "Error: ${e.localizedMessage}")
        }
    }

    suspend fun removeModule(module: KingSUModule): Boolean = withContext(Dispatchers.IO) {
        try {
            val cmd = "touch ${module.moduleDir}/remove"
            val res = ShellExecutor.runCommand(cmd, useRoot = true)
            res.isSuccess
        } catch (_: Throwable) {
            false
        }
    }

    private fun parseModuleProp(propContent: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            propContent.lines().forEach { line ->
                if (line.contains("=")) {
                    val parts = line.split("=", limit = 2)
                    if (parts.size == 2) {
                        map[parts[0].trim()] = parts[1].trim()
                    }
                }
            }
        } catch (_: Throwable) {}
        return map
    }
}
