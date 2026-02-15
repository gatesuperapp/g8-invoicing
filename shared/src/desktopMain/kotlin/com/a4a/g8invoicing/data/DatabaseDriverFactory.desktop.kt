package com.a4a.g8invoicing.data

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.a4a.g8invoicing.Database
import java.io.File
import java.sql.DriverManager

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

        // Get current version before creating driver (if db exists)
        val currentVersion = if (!needsSchema) {
            getCurrentVersionJdbc(dbFile.absolutePath)
        } else {
            0L
        }

        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")

        if (needsSchema) {
            // Create schema if database is new
            Database.Schema.create(driver)
        } else {
            // Run migrations if database already exists
            val schemaVersion = Database.Schema.version
            if (currentVersion < schemaVersion) {
                Database.Schema.migrate(driver, currentVersion, schemaVersion)
            }
        }

        // Enable foreign keys
        driver.execute(null, "PRAGMA foreign_keys=ON;", 0)

        return driver
    }

    private fun getCurrentVersionJdbc(dbPath: String): Long {
        return DriverManager.getConnection("jdbc:sqlite:$dbPath").use { connection ->
            connection.createStatement().use { statement ->
                statement.executeQuery("PRAGMA user_version;").use { resultSet ->
                    if (resultSet.next()) {
                        resultSet.getLong(1)
                    } else {
                        0L
                    }
                }
            }
        }
    }
}
