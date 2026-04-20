package com.aropi.app

import android.app.Application
import android.util.Log
import com.aropi.app.logic.bundle.BundleManager
import com.aropi.app.logic.bundle.BundleUpdateWorker

/**
 * Application entry point.
 *
 * Extracts the APK-bundled offline pictogram/phrase bundle on first
 * run, and schedules the periodic backend update check.
 */
class AroPiApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        try {
            BundleManager.get(this).ensureInitialized()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialise Aropi bundle", e)
        }
        BundleUpdateWorker.schedule(this)
    }

    companion object {
        private const val TAG = "AroPiApplication"
    }
}
