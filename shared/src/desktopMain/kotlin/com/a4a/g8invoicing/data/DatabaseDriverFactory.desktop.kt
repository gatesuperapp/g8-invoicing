package com.a4a.g8invoicing.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.a4a.g8invoicing.Database
import java.io.File

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        // Get user's home directory for storing the database
        val dbPath = File(System.getProperty("user.home"), ".g8invoicing")
        if (!dbPath.exists()) {
            dbPath.mkdirs()
        }
        val dbFile = File(dbPath, "g8_invoicing.db")

        // Check if we need to create schema BEFORE opening connection
        val needsSchema = !dbFile.exists() || dbFile.length() == 0L

        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")

        // Create schema if database is new
        if (needsSchema) {
            Database.Schema.create(driver)
        }

        // Enable foreign keys
        driver.execute(null, "PRAGMA foreign_keys=ON;", 0)

        return driver
    }
}
