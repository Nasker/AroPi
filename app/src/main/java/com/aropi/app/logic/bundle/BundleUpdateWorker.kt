package com.aropi.app.logic.bundle

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import okio.buffer
import okio.sink
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Periodic worker that checks the backend for a newer offline bundle
 * and, if one is available, downloads + verifies + activates it.
 *
 * See `AropiBackend/ARCHITECTURE.md` §5.1 for the update lifecycle.
 * Constrained to unmetered networks (Wi-Fi) to avoid surprising users
 * with large downloads on cellular data.
 */
class BundleUpdateWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val ctx = applicationContext
        val manager = BundleManager.get(ctx)
        manager.ensureInitialized()

        return try {
            val api = AropiNetwork.api(ctx)
            val pointer = api.latestBundle()
            val current = manager.activeVersion
            if (pointer.version == current) {
                Log.i(TAG, "Bundle up to date (${pointer.version})")
                return Result.success()
            }

            Log.i(TAG, "New bundle available: ${pointer.version} (current=$current)")

            val rootBundlesDir = File(ctx.filesDir, BundleManager.BUNDLES_SUBDIR)
                .apply { if (!exists()) mkdirs() }
            val stagingDir = File(rootBundlesDir, "${pointer.version}.staging")
            val targetDir = File(rootBundlesDir, pointer.version)
            if (stagingDir.exists()) stagingDir.deleteRecursively()
            stagingDir.mkdirs()

            // 1. Download the zip.
            val zipFile = File(stagingDir, pointer.filename)
            api.downloadBundle(pointer.url).use { body ->
                body.source().use { src ->
                    zipFile.sink().buffer().use { sink -> sink.writeAll(src) }
                }
            }

            // 2. Verify SHA-256.
            val digest = BundleManager.sha256(zipFile)
            if (!digest.equals(pointer.sha256, ignoreCase = true)) {
                Log.w(TAG, "SHA-256 mismatch: got=$digest expected=${pointer.sha256}")
                stagingDir.deleteRecursively()
                return Result.retry()
            }

            // 3. Extract into staging, then atomically rename.
            zipFile.inputStream().use { BundleManager.extractZip(it, stagingDir) }
            zipFile.delete()

            if (targetDir.exists()) targetDir.deleteRecursively()
            if (!stagingDir.renameTo(targetDir)) {
                Log.w(TAG, "Failed to rename staging -> target; falling back to recursive copy")
                stagingDir.copyRecursively(targetDir, overwrite = true)
                stagingDir.deleteRecursively()
            }

            // 4. Flip the active pointer (this also closes open DB handles).
            manager.activateVersion(pointer.version)
            Log.i(TAG, "Activated bundle ${pointer.version}")
            Result.success()
        } catch (e: Exception) {
            Log.w(TAG, "Bundle update failed", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "BundleUpdateWorker"
        private const val WORK_NAME = "aropi-bundle-update"

        /**
         * Enqueue the periodic updater. Safe to call on every app start
         * (KEEP policy prevents duplicate schedules).
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<BundleUpdateWorker>(
                7, TimeUnit.DAYS,
                12, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
