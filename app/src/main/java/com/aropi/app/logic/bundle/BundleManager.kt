package com.aropi.app.logic.bundle

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.zip.ZipInputStream

/**
 * Manages the offline Aropi bundle on the device.
 *
 * The bundle contains:
 * - pictos.db              (pictogram catalogue)
 * - pictogram_phrases.db   (pre-computed subject+verb conjugations)
 * - png/<word>.png         (pictogram images, one per English word)
 *
 * On first launch we extract the APK-bundled zip from
 * `assets/aropi-bundle/initial.zip` into `filesDir/aropi/<version>/`.
 * Later updates are performed by [BundleUpdater]; this manager is then
 * pointed at the new folder by flipping [ACTIVE_VERSION_KEY] in prefs.
 *
 * Both databases are opened read-only; no mutation APIs are exposed.
 */
class BundleManager private constructor(
    private val appContext: Context
) {
    private val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    @Volatile private var pictosDbRef: SQLiteDatabase? = null
    @Volatile private var phrasesDbRef: SQLiteDatabase? = null

    val activeVersion: String?
        get() = prefs.getString(ACTIVE_VERSION_KEY, null)

    /** Root directory for the currently active bundle version. */
    val activeDir: File?
        get() = activeVersion?.let { bundleDir(it) }

    /**
     * Ensure a bundle exists on disk. Extracts the APK-bundled zip on
     * first run. Safe to call repeatedly and from any thread (synchronised).
     */
    @Synchronized
    fun ensureInitialized() {
        if (activeDir?.let { it.exists() && File(it, PICTOS_DB).exists() } == true) return

        val pointer = readAssetPointer()
        val target = bundleDir(pointer.version)
        if (!File(target, PICTOS_DB).exists()) {
            Log.i(TAG, "Extracting initial bundle ${pointer.version} -> $target")
            target.mkdirs()
            appContext.assets.open(INITIAL_ZIP_ASSET).use { input ->
                extractZip(input, target)
            }
        }
        prefs.edit().putString(ACTIVE_VERSION_KEY, pointer.version).apply()
        closeOpenDbs()
    }

    /**
     * Swap the active bundle to [newVersion]. The caller is expected to
     * have already populated `filesDir/aropi/<newVersion>/` with valid
     * DBs and images. Old version folders (except the new one) are
     * deleted.
     */
    @Synchronized
    fun activateVersion(newVersion: String) {
        prefs.edit().putString(ACTIVE_VERSION_KEY, newVersion).apply()
        closeOpenDbs()
        // Clean up older versions.
        rootDir().listFiles()?.forEach { dir ->
            if (dir.isDirectory && dir.name != newVersion) dir.deleteRecursively()
        }
    }

    fun pictosDb(): SQLiteDatabase {
        pictosDbRef?.let { if (it.isOpen) return it }
        return openReadOnly(PICTOS_DB).also { pictosDbRef = it }
    }

    fun phrasesDb(): SQLiteDatabase {
        phrasesDbRef?.let { if (it.isOpen) return it }
        return openReadOnly(PHRASES_DB).also { phrasesDbRef = it }
    }

    /** Absolute path to the PNG for the given English picto `word`, or null if missing. */
    fun imageFileForWord(word: String): File? {
        val dir = activeDir ?: return null
        val f = File(File(dir, PNG_DIR), "$word.png")
        return if (f.exists()) f else null
    }

    @Synchronized
    private fun closeOpenDbs() {
        pictosDbRef?.takeIf { it.isOpen }?.close()
        phrasesDbRef?.takeIf { it.isOpen }?.close()
        pictosDbRef = null
        phrasesDbRef = null
    }

    private fun openReadOnly(name: String): SQLiteDatabase {
        val dir = activeDir
            ?: error("BundleManager not initialised; call ensureInitialized() first")
        val file = File(dir, name)
        check(file.exists()) { "Missing bundle DB: $file" }
        return SQLiteDatabase.openDatabase(
            file.absolutePath,
            null,
            SQLiteDatabase.OPEN_READONLY
        )
    }

    private fun rootDir(): File =
        File(appContext.filesDir, BUNDLES_SUBDIR).apply { if (!exists()) mkdirs() }

    private fun bundleDir(version: String): File = File(rootDir(), version)

    private fun readAssetPointer(): AssetPointer {
        return try {
            appContext.assets.open(INITIAL_POINTER_ASSET).bufferedReader().use { r ->
                val txt = r.readText()
                // Extract "version" without pulling in a JSON dep dance.
                val v = Regex("\"version\"\\s*:\\s*\"([^\"]+)\"")
                    .find(txt)?.groupValues?.getOrNull(1)
                val sha = Regex("\"sha256\"\\s*:\\s*\"([^\"]+)\"")
                    .find(txt)?.groupValues?.getOrNull(1)
                AssetPointer(
                    version = v ?: DEFAULT_FALLBACK_VERSION,
                    sha256 = sha
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "No initial pointer in assets; falling back", e)
            AssetPointer(DEFAULT_FALLBACK_VERSION, null)
        }
    }

    private data class AssetPointer(val version: String, val sha256: String?)

    companion object {
        private const val TAG = "BundleManager"
        private const val PREFS = "aropi_bundle"
        const val ACTIVE_VERSION_KEY = "active_version"

        const val BUNDLES_SUBDIR = "aropi"
        const val PICTOS_DB = "pictos.db"
        const val PHRASES_DB = "pictogram_phrases.db"
        const val PNG_DIR = "png"

        private const val INITIAL_ZIP_ASSET = "aropi-bundle/initial.zip"
        private const val INITIAL_POINTER_ASSET = "aropi-bundle/initial.json"
        private const val DEFAULT_FALLBACK_VERSION = "initial"

        @Volatile private var instance: BundleManager? = null

        fun get(context: Context): BundleManager =
            instance ?: synchronized(this) {
                instance ?: BundleManager(context.applicationContext).also { instance = it }
            }

        /**
         * Extract a zip stream into [targetDir]. Guards against Zip Slip.
         * `targetDir` must already exist.
         */
        fun extractZip(input: InputStream, targetDir: File) {
            val targetCanonical = targetDir.canonicalFile
            ZipInputStream(input).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val outFile = File(targetDir, entry.name).canonicalFile
                    if (!outFile.path.startsWith(targetCanonical.path)) {
                        throw SecurityException("Zip entry escapes target: ${entry.name}")
                    }
                    if (entry.isDirectory) {
                        outFile.mkdirs()
                    } else {
                        outFile.parentFile?.mkdirs()
                        FileOutputStream(outFile).use { out -> zis.copyTo(out) }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        }

        /** Compute SHA-256 of [file] as a lowercase hex string. */
        fun sha256(file: File): String {
            val md = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { fis ->
                val buf = ByteArray(8 * 1024)
                while (true) {
                    val read = fis.read(buf)
                    if (read <= 0) break
                    md.update(buf, 0, read)
                }
            }
            return md.digest().joinToString("") { "%02x".format(it) }
        }
    }
}
