package com.a4a.g8invoicing.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log
import com.a4a.g8invoicing.Database
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

/**
 * Two-phase database restore.
 *
 * Phase 1 (pick-time, inside the UI thread):
 *   validateBackup opens the zip, extracts its g8_invoicing.db entry to a
 *   temp file, and reads PRAGMA user_version. Rejects if:
 *     - zip is unreadable / missing the DB entry
 *     - entry contains a path-traversal segment (../)
 *     - user_version > the current app's Database.Schema.version
 *   Then queueRestore copies the pending zip into filesDir/pending_import.zip
 *   and hands control back to the UI, which prompts the user to force-close.
 *
 * Phase 2 (next launch, inside Application.onCreate, BEFORE Koin/SQLDelight):
 *   applyPendingRestoreIfAny performs the atomic swap:
 *     - renames the current DB + WAL + SHM to *.bak
 *     - renames filesDir/logos/ to logos.bak/
 *     - extracts the pending zip into place
 *     - opens the freshly-restored DB read-only to prove it's valid
 *     - on success: deletes the .bak set + the pending zip
 *     - on failure: rolls .bak back, deletes half-written files, drops a
 *       RESTORE_FAILED_FILE carrying the error message so the UI can surface
 *       it via consumeRestoreFailureIfAny on the first composition
 */
object RestoreManager {
    private const val TAG = "RestoreManager"

    private const val DB_NAME = "g8_invoicing.db"
    private const val LOGOS_DIR = "logos"
    private const val LOGOS_BAK_DIR = "logos.bak"

    private const val PENDING_IMPORT_FILE = "pending_import.zip"
    private const val RESTORE_FAILED_FILE = "restore_failed_reason.txt"
    // Sentinel file written when the applied backup was on a schema older than
    // Database.Schema.version. MainCompose consumes it at boot to reset the
    // 1.9-wizard "seen" flag so migration paths re-run against the restored
    // rows (issuer/client/product assignment). See MainCompose LaunchedEffect.
    private const val MIGRATION_WIZARD_RESET_FILE = "restore_needs_wizard.txt"
    // Sentinel file written when the applied backup came from a schema
    // predating 5.sqm (schema version < 6, i.e. pre-1.8, before country_code
    // columns landed on the address tables). MainCompose consumes it at boot
    // to clear HAS_SEEN_ONBOARDING_1_8 so the 1.8 wizard re-fires and asks
    // the "clients tous dans le même pays ?" question against the restored
    // clients that have no country_code populated.
    private const val ONBOARDING_1_8_RESET_FILE = "restore_needs_onboarding_1_8.txt"

    // Schema version at which the 1.8 country_code columns were added
    // (5.sqm — Factur-X 1.8 fields). Backups older than this need the 1.8
    // onboarding to re-fire so the user can bulk-assign a country to
    // clients that came in without one.
    private const val SCHEMA_VERSION_1_8 = 6L

    // Cap the accepted zip at 200 MB — a full backup with logos should sit
    // well under this. Blocks zip-bomb style inputs before we ever unpack.
    private const val MAX_ZIP_BYTES = 200L * 1024 * 1024

    sealed class ValidationResult {
        data object Ok : ValidationResult()
        data class Error(val reason: String) : ValidationResult()
        data class DowngradeBlocked(val backupVersion: Long, val appVersion: Long) : ValidationResult()
    }

    fun validateBackup(context: Context, sourceUri: Uri): Pair<ValidationResult, File?> {
        val staging = File(context.cacheDir, "restore_staging_" + System.currentTimeMillis() + ".zip")
        try {
            val input = context.contentResolver.openInputStream(sourceUri)
                ?: return Pair(ValidationResult.Error("Impossible d'ouvrir le fichier"), null)
            val output = FileOutputStream(staging)
            val bytesCopied: Long
            try {
                bytesCopied = input.copyTo(output)
            } finally {
                output.close()
                input.close()
            }
            if (bytesCopied > MAX_ZIP_BYTES) {
                staging.delete()
                return Pair(ValidationResult.Error("Fichier trop volumineux (>200 Mo)"), null)
            }

            val isSqliteRaw = looksLikeRawSqlite(staging)
            val backupVersion: Long
            if (isSqliteRaw) {
                backupVersion = readUserVersion(staging)
            } else {
                val extractedDb = extractDbFromZip(staging, context.cacheDir)
                if (extractedDb == null) {
                    staging.delete()
                    return Pair(
                        ValidationResult.Error("Archive invalide (fichier de base introuvable)"),
                        null,
                    )
                }
                try {
                    backupVersion = readUserVersion(extractedDb)
                } finally {
                    extractedDb.delete()
                }
            }

            val appVersion = Database.Schema.version
            if (backupVersion > appVersion) {
                staging.delete()
                return Pair(ValidationResult.DowngradeBlocked(backupVersion, appVersion), null)
            }
            return Pair(ValidationResult.Ok, staging)
        } catch (e: Exception) {
            Log.w(TAG, "validateBackup failed", e)
            staging.delete()
            return Pair(ValidationResult.Error(e.message ?: "Erreur inconnue"), null)
        }
    }

    fun queueRestore(context: Context, stagingFile: File) {
        val target = File(context.filesDir, PENDING_IMPORT_FILE)
        if (target.exists()) target.delete()
        stagingFile.copyTo(target, overwrite = true)
        stagingFile.delete()
    }

    fun applyPendingRestoreIfAny(context: Context) {
        val pending = File(context.filesDir, PENDING_IMPORT_FILE)
        if (!pending.exists()) return

        val dbFile = context.getDatabasePath(DB_NAME)
        val dbParent = dbFile.parentFile
        val walFile = File(dbParent, DB_NAME + "-wal")
        val shmFile = File(dbParent, DB_NAME + "-shm")

        val dbBak = File(dbParent, DB_NAME + ".bak")
        val walBak = File(dbParent, DB_NAME + "-wal.bak")
        val shmBak = File(dbParent, DB_NAME + "-shm.bak")

        val logosDir = File(context.filesDir, LOGOS_DIR)
        val logosBak = File(context.filesDir, LOGOS_BAK_DIR)

        // Peek the incoming backup's schema version BEFORE the swap. Reading
        // from the freshly-restored DB after opening would be misleading —
        // SQLDelight's driver auto-runs the migration chain to bring it up
        // to Database.Schema.version, so PRAGMA user_version would already
        // read the current one and we couldn't tell "was pre-1.9" apart from
        // "was already 1.9".
        val incomingVersion: Long = try {
            if (looksLikeRawSqlite(pending)) {
                readUserVersion(pending)
            } else {
                val tempDb = extractDbFromZip(pending, context.cacheDir)
                if (tempDb == null) 0L
                else try { readUserVersion(tempDb) } finally { tempDb.delete() }
            }
        } catch (_: Exception) {
            0L
        }
        val backupWasPreCurrent: Boolean = incomingVersion in 1 until Database.Schema.version
        val backupWasPre18: Boolean = incomingVersion in 1 until SCHEMA_VERSION_1_8

        try {
            dbParent?.mkdirs()

            if (dbFile.exists()) dbFile.renameTo(dbBak)
            if (walFile.exists()) walFile.renameTo(walBak)
            if (shmFile.exists()) shmFile.renameTo(shmBak)
            if (logosDir.exists()) {
                if (logosBak.exists()) logosBak.deleteRecursively()
                logosDir.renameTo(logosBak)
            }

            if (looksLikeRawSqlite(pending)) {
                pending.copyTo(dbFile, overwrite = true)
            } else {
                extractZipInto(pending, dbFile, File(context.filesDir, LOGOS_DIR))
            }

            val probe = SQLiteDatabase.openDatabase(
                dbFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY,
            )
            probe.close()

            dbBak.delete()
            walBak.delete()
            shmBak.delete()
            if (logosBak.exists()) logosBak.deleteRecursively()
            pending.delete()

            if (backupWasPreCurrent) {
                File(context.filesDir, MIGRATION_WIZARD_RESET_FILE).writeText("1")
            }
            if (backupWasPre18) {
                File(context.filesDir, ONBOARDING_1_8_RESET_FILE).writeText("1")
            }

            Log.i(TAG, "Restore applied successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Restore failed, rolling back", e)
            if (dbFile.exists()) dbFile.delete()
            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()
            if (dbBak.exists()) dbBak.renameTo(dbFile)
            if (walBak.exists()) walBak.renameTo(walFile)
            if (shmBak.exists()) shmBak.renameTo(shmFile)
            if (logosDir.exists()) logosDir.deleteRecursively()
            if (logosBak.exists()) logosBak.renameTo(logosDir)

            File(context.filesDir, RESTORE_FAILED_FILE).writeText(
                e.message ?: "Erreur inconnue lors de la restauration"
            )
            pending.delete()
        }
    }

    /**
     * True exactly once per restored pre-current backup. MainCompose reads
     * this at boot to call [ActivatedModulesRepository.resetMigration19Seen],
     * so the migration wizard re-fires on the restored issuers/clients/products.
     */
    fun consumeMigrationWizardResetIfAny(context: Context): Boolean {
        val f = File(context.filesDir, MIGRATION_WIZARD_RESET_FILE)
        if (!f.exists()) return false
        f.delete()
        return true
    }

    /**
     * True exactly once per restored pre-1.8 backup. MainCompose reads this at
     * boot to clear HAS_SEEN_ONBOARDING_1_8 so the 1.8 wizard re-fires — the
     * restored clients have no country_code (columns didn't exist yet in the
     * source schema) and the wizard's "clients tous dans le même pays ?" step
     * is the bulk fixup path.
     */
    fun consumeOnboarding18ResetIfAny(context: Context): Boolean {
        val f = File(context.filesDir, ONBOARDING_1_8_RESET_FILE)
        if (!f.exists()) return false
        f.delete()
        return true
    }

    fun consumeRestoreFailureIfAny(context: Context): String? {
        val f = File(context.filesDir, RESTORE_FAILED_FILE)
        if (!f.exists()) return null
        val msg: String? = try {
            f.readText()
        } catch (_: Exception) {
            null
        }
        f.delete()
        return msg
    }

    private fun looksLikeRawSqlite(file: File): Boolean {
        return try {
            val input = file.inputStream()
            try {
                val header = ByteArray(16)
                val read = input.read(header)
                if (read < 16) {
                    false
                } else {
                    String(header, Charsets.US_ASCII) == "SQLite format 3 "
                }
            } finally {
                input.close()
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun readUserVersion(dbFile: File): Long {
        val db = SQLiteDatabase.openDatabase(
            dbFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READONLY,
        )
        try {
            val cursor = db.rawQuery("PRAGMA user_version", null)
            try {
                return if (cursor.moveToFirst()) cursor.getLong(0) else 0L
            } finally {
                cursor.close()
            }
        } finally {
            db.close()
        }
    }

    private fun extractDbFromZip(zipFile: File, destDir: File): File? {
        val zip = ZipFile(zipFile)
        try {
            val entry = zip.getEntry(DB_NAME) ?: return null
            if (entry.name.contains("..")) return null
            val out = File(destDir, "restore_check_" + System.currentTimeMillis() + ".db")
            val input = zip.getInputStream(entry)
            try {
                val output = FileOutputStream(out)
                try {
                    input.copyTo(output)
                } finally {
                    output.close()
                }
            } finally {
                input.close()
            }
            return out
        } finally {
            zip.close()
        }
    }

    private fun extractZipInto(zipFile: File, dbTarget: File, logosTarget: File) {
        logosTarget.mkdirs()
        val zin = ZipInputStream(zipFile.inputStream().buffered())
        try {
            while (true) {
                val entry = zin.nextEntry ?: break
                if (entry.isDirectory) {
                    zin.closeEntry()
                    continue
                }
                if (entry.name.contains("..")) {
                    zin.closeEntry()
                    continue
                }
                if (entry.name == DB_NAME) {
                    val out = FileOutputStream(dbTarget)
                    try {
                        zin.copyTo(out)
                    } finally {
                        out.close()
                    }
                } else if (entry.name.startsWith(LOGOS_DIR + "/")) {
                    val filename = entry.name.removePrefix(LOGOS_DIR + "/")
                    if (filename.isNotEmpty()) {
                        val outFile = File(logosTarget, filename)
                        val out = FileOutputStream(outFile)
                        try {
                            zin.copyTo(out)
                        } finally {
                            out.close()
                        }
                    }
                }
                zin.closeEntry()
            }
        } finally {
            zin.close()
        }
    }
}
