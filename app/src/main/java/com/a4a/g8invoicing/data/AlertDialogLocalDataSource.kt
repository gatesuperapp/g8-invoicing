package com.a4a.g8invoicing.data

import com.a4a.g8invoicing.Database

class AlertDialogLocalDataSource(
    db: Database,
) : AlertDialogDataSourceInterface {
    private val alertDialogQueries = db.alertDialogQueries
    override fun fetch(id: Long): Boolean? {
        try {
            val result = alertDialogQueries.fetch(id)
                .executeAsOneOrNull()?.hasBeenShown

            if(result?.toInt() == 0) return true
            else return false
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
        return null
    }

    override fun update(id: Long) {
        try {
            alertDialogQueries.update(id)
        } catch (e: Exception) {
            //Log.e(ContentValues.TAG, "Error: ${e.message}")
        }
    }

}