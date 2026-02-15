package com.a4a.g8invoicing.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.a4a.g8invoicing.Database

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        // NativeSqliteDriver handles migrations automatically via Database.Schema
        return NativeSqliteDriver(Database.Schema, "g8invoicing.db")
    }
}
