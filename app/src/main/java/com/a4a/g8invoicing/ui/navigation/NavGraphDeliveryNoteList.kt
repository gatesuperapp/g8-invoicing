package com.a4a.g8invoicing.ui.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.screens.DeliveryNoteList
import com.a4a.g8invoicing.ui.shared.AlertDialogErrorOrInfo
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.viewmodels.AlertDialogViewModel
import com.a4a.g8invoicing.ui.viewmodels.DeliveryNoteListViewModel

fun NavGraphBuilder.deliveryNoteList(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickListItem: (Int) -> Unit,
    onClickNew: () -> Unit,
    onClickBack: () -> Unit,
) {
    composable(route = Screen.DeliveryNoteList.name) {
        val viewModel: DeliveryNoteListViewModel = hiltViewModel()
        val deliveryNotesUiState by viewModel.deliveryNotesUiState
            .collectAsStateWithLifecycle()

        val alertDialogViewModel: AlertDialogViewModel = hiltViewModel()
        val displayAutoSaveAlertDialog =
            alertDialogViewModel.fetchAlertDialogFromLocalDb(4) ?: false
        val displayConvertDeliveryNoteAlertDialog = remember {
            mutableStateOf(
                alertDialogViewModel.fetchAlertDialogFromLocalDb(5) ?: false
            )
        }

        DeliveryNoteList(
            displayAutoSaveAlertDialog = displayAutoSaveAlertDialog,
            onDisplayAutoSaveAlertDialogClose = {
                alertDialogViewModel.updateAlertDialogInLocalDb(4)
            },
            openCreateNewScreen = { onClickNew() },
            navController = navController,
            deliveryNotesUiState = deliveryNotesUiState,
            onClickDelete = viewModel::deleteDeliveryNotes,
            onClickDuplicate = viewModel::duplicateDeliveryNotes,
            onClickConvert = viewModel::convertDeliveryNotes,
            onClickNew = { onClickNew() },
            onClickCategory = onClickCategory,
            onClickListItem = onClickListItem,
            onClickBack = { onClickBack() })

        when {
            (displayConvertDeliveryNoteAlertDialog.value
                    && deliveryNotesUiState.deliveryNoteStates.isNotEmpty()) -> {
                alertDialogViewModel.updateAlertDialogInLocalDb(5)
                AlertDialogErrorOrInfo(
                    onDismissRequest = {
                        displayConvertDeliveryNoteAlertDialog.value = false

                    },
                    onConfirmation = {
                        displayConvertDeliveryNoteAlertDialog.value = false
                    },
                    message = Strings.get(R.string.alert_convert_notes_dialog_info),
                    confirmationText = stringResource(id = R.string.alert_convert_notes_info_confirm)
                )
            }
        }
    }


}