package com.a4a.g8invoicing.data

/**
 * Interface for AlertDialogDataSourceImpl
 * Makes db implementation swappable
 *
 */

interface AlertDialogDataSourceInterface {
    fun fetch(id: Long): Boolean?
    fun update(id: Long)
}
