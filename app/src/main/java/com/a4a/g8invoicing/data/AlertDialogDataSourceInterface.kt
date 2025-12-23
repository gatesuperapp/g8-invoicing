package com.a4a.g8invoicing.data

import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.viewmodels.PersonType
import kotlinx.coroutines.flow.Flow

/**
 * Interface for ClientLocalDataSourceImpl
 * Makes db implementation swappable
 *
 */

interface AlertDialogDataSourceInterface {
    fun fetch(id: Long): Boolean?
    fun update(id: Long)
}
