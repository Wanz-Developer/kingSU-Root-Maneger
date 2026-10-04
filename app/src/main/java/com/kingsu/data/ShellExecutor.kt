package com.kingsu.data

import com.kingsu.model.ShellResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import kotlin.coroutines.cancellation.CancellationException

object ShellExecutor {

    suspend fun runCommand(
        command: String,
        useRoot: Boolean = true,
        timeoutMs: Long = 8000L
    ): ShellResult = withContext(Dispatchers.IO) {
        val result = withTimeoutOrNull(timeoutMs) {
            var process: Process? = null
            var os: DataOutputStream? = null
            var stdoutReader: BufferedReader? = null
            var stderrReader: BufferedReader? = null

            try {
                val shell = if (useRoot) "su" else "sh"
                process = try {
                    Runtime.getRuntime().exec(shell)
                } catch (e: Exception) {
                    if (useRoot) {
                        // Fallback to normal shell if su binary doesn't exist
                        Runtime.getRuntime().exec("sh")
                    } else {
                        throw e
                    }
                }

                os = DataOutputStream(process.outputStream)
                try {
                    os.writeBytes("$command\n")
                    os.writeBytes("exit\n")
                    os.flush()
                } catch (_: Exception) {
                    // Ignore broken pipe if process exited early
                }

                stdoutReader = BufferedReader(InputStreamReader(process.inputStream))
                stderrReader = BufferedReader(InputStreamReader(process.errorStream))

                val stdoutBuilder = StringBuilder()
                val stderrBuilder = StringBuilder()

                var line: String?
                while (stdoutReader.readLine().also { line = it } != null) {
                    stdoutBuilder.append(line).append("\n")
                }

                while (stderrReader.readLine().also { line = it } != null) {
                    stderrBuilder.append(line).append("\n")
                }

                val exitCode = try {
                    process.waitFor()
                } catch (_: Exception) {
                    -1
                }

                ShellResult(
                    exitCode = exitCode,
                    stdout = stdoutBuilder.toString().trim(),
                    stderr = stderrBuilder.toString().trim()
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                ShellResult(
                    exitCode = -1,
                    stdout = "",
                    stderr = e.localizedMessage ?: "Shell execution error"
                )
            } finally {
                try { os?.close() } catch (_: Throwable) {}
                try { stdoutReader?.close() } catch (_: Throwable) {}
                try { stderrReader?.close() } catch (_: Throwable) {}
                try { process?.destroy() } catch (_: Throwable) {}
            }
        }

        result ?: ShellResult(
            exitCode = -1,
            stdout = "",
            stderr = "Command execution timed out (${timeoutMs}ms)"
        )
    }

    suspend fun isRootAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val res = runCommand("id", useRoot = true, timeoutMs = 3000L)
            res.isSuccess && (res.stdout.contains("uid=0") || res.stdout.contains("root"))
        } catch (_: Throwable) {
            false
        }
    }
}
