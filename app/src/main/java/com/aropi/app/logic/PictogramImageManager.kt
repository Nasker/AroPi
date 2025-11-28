package com.aropi.app.logic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Manages pictogram images stored in internal storage.
 * Handles saving, loading, and deleting custom pictogram images.
 */
class PictogramImageManager(private val context: Context) {
    
    private val imagesDir: File
        get() = File(context.filesDir, "pictogram_images").apply {
            if (!exists()) mkdirs()
        }
    
    /**
     * Save an image from URI to internal storage.
     * @param uri Source image URI (from gallery/camera)
     * @return Filename of saved image, or null if failed
     */
    fun saveImage(uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            // Generate unique filename
            val filename = "picto_${UUID.randomUUID()}.png"
            val file = File(imagesDir, filename)
            
            // Compress and save
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            
            filename
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Get the file for a given filename.
     */
    fun getImageFile(filename: String): File {
        return File(imagesDir, filename)
    }
    
    /**
     * Check if an image file exists.
     */
    fun imageExists(filename: String): Boolean {
        return getImageFile(filename).exists()
    }
    
    /**
     * Delete an image file.
     */
    fun deleteImage(filename: String): Boolean {
        return try {
            getImageFile(filename).delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    /**
     * Get URI for an image file.
     */
    fun getImageUri(filename: String): Uri {
        return Uri.fromFile(getImageFile(filename))
    }
}
