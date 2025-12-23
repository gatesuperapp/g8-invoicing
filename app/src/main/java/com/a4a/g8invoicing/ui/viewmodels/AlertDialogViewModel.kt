package com.a4a.g8invoicing.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a4a.g8invoicing.data.AlertDialogDataSourceInterface
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlertDialogViewModel @Inject constructor(
    private val alertDialogDataSource: AlertDialogDataSourceInterface,
) : ViewModel() {
    private var updateJob: Job? = null

    fun fetchAlertDialogFromLocalDb(id: Long): Boolean? {
        var result: Boolean? = false
        try {
            result = alertDialogDataSource.fetch(id)
        } catch (e: Exception) {
            //println("Fetching deliveryNote failed with exception: ${e.localizedMessage}")
        }
        return result
    }

    fun updateAlertDialogInLocalDb(id: Long) {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            try {
                alertDialogDataSource.update(id)
            } catch (e: Exception) {
                //println("Saving deliveryNote failed with exception: ${e.localizedMessage}")
            }
        }
    }
}