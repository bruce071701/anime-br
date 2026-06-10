package com.animebr.app.data.db

import android.content.Context
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.GZIPInputStream

/**
 * Decrypts and decompresses the encrypted database file from assets.
 * The db is stored as: XOR encrypted -> gzip compressed -> original SQLite.
 * This reduces APK size significantly (SQLite compresses ~60-70%).
 */
object DatabaseDecryptor {

    private const val ENCRYPTED_ASSET = "anime_digital.db.enc"
    private const val DB_FILE_NAME = "anime_digital.db"
    private val KEY = "AnimeBR2026SecretKey!@#".toByteArray()

    /**
     * Get the path to the decrypted database file.
     * If not yet decrypted, performs decryption from assets.
     * Returns the File path for Room's createFromFile().
     */
    fun getDecryptedDbFile(context: Context): File {
        val dbFile = File(context.filesDir, DB_FILE_NAME)

        if (!dbFile.exists()) {
            decryptFromAssets(context, dbFile)
        }

        return dbFile
    }

    /**
     * Force re-decrypt (e.g. after app update with new db).
     */
    fun refreshDatabase(context: Context): File {
        val dbFile = File(context.filesDir, DB_FILE_NAME)
        if (dbFile.exists()) dbFile.delete()
        // Also delete Room's copy
        val roomDb = context.getDatabasePath("animebr.db")
        if (roomDb.exists()) roomDb.delete()
        // Delete journal/wal files
        File(roomDb.path + "-journal").delete()
        File(roomDb.path + "-wal").delete()
        File(roomDb.path + "-shm").delete()

        return decryptFromAssets(context, dbFile)
    }

    private fun decryptFromAssets(context: Context, outputFile: File): File {
        // Read encrypted data from assets
        val encrypted = context.assets.open(ENCRYPTED_ASSET).use { it.readBytes() }

        // XOR decrypt
        val keyLen = KEY.size
        val compressed = ByteArray(encrypted.size) { i ->
            (encrypted[i].toInt() xor KEY[i % keyLen].toInt()).toByte()
        }

        // Gzip decompress
        val decompressed = GZIPInputStream(ByteArrayInputStream(compressed)).use { it.readBytes() }

        // Write to file
        outputFile.parentFile?.mkdirs()
        FileOutputStream(outputFile).use { it.write(decompressed) }

        return outputFile
    }
}
