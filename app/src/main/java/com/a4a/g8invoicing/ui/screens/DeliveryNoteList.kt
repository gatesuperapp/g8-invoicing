package com.a4a.g8invoicing.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.shared.AlertDialogDeleteDocument
import com.a4a.g8invoicing.ui.shared.AnimationLottie
import com.a4a.g8invoicing.ui.shared.GeneralBottomBar
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.states.DeliveryNotesUiState

@Composable
fun DeliveryNoteList(
    navController: NavController,
    documentsUiState: DeliveryNotesUiState,
    onClickDelete: (List<DeliveryNoteState>) -> Unit,
    onClickDuplicate: (List<DeliveryNoteState>) -> Unit,
    onClickConvert: (List<DeliveryNoteState>) -> Unit,
    onClickNew: () -> Unit,
    onClickCategory: (Category) -> Unit,
    onClickListItem: (Int) -> Unit,
    onClickBack: () -> Unit,
) {
    // Main list to handle actions with selected items
    val selectedItems = remember { mutableStateListOf<DeliveryNoteState>() }

    // Will recompose the BottomBar (only) when an item is selected, or when all items are unselected
    val selectedMode = remember { mutableStateOf(false) }
    // Will recompose all the items when clicking "unselect all"
    val keyToResetCheckboxes = remember { mutableStateOf(false) }

    // Alert dialogs
    val openAlertDialog = remember { mutableStateOf(false) }
    val checkIfAutoSaveDialogMustBeOpened = remember { mutableStateOf(false) }

    // Add background when bottom menu expanded
    val transparent = Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
    val backgroundColor = remember { mutableStateOf(transparent) }

    Scaffold(
        topBar = {
            TopBar(
                title = R.string.appbar_delivery_notes,
                navController = navController,
                onClickBackArrow = onClickBack
            )
        },
        bottomBar = {
            GeneralBottomBar(
                navController = navController,
                numberOfItemsSelected = selectedItems.size,
                onClickDelete = {
                    backgroundColor.value =
                        changeBackgroundWithVerticalGradient(backgroundColor.value)
                    openAlertDialog.value = true
                },
                onClickDuplicate = {
                    onClickDuplicate(selectedItems.toList())
                    resetSelectedItems(selectedItems, selectedMode, keyToResetCheckboxes)
                },
                onClickConvert = {
                    onClickConvert(selectedItems.toList())
                    resetSelectedItems(selectedItems, selectedMode, keyToResetCheckboxes)
                },
                onClickUnselectAll = {
                    resetSelectedItems(selectedItems, selectedMode, keyToResetCheckboxes)
                },
                onClickNew = { onClickNew() },
                onClickCategory = onClickCategory,
                isConvertible = true,
                onChangeBackground = {
                    backgroundColor.value =
                        changeBackgroundWithVerticalGradient(backgroundColor.value)
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(
                    padding
                )
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column {
                    // No need to have pull to refresh because it's a flow,
                    // thus the list is updated when anything changes in db
                    DocumentListContent(
                        documents = documentsUiState.deliveryNoteStates,
                        onItemClick = onClickListItem,
                        addDocumentToSelectedList = {
                            selectedItems.add(it as DeliveryNoteState)
                            selectedMode.value = true
                        },
                        removeDocumentFromSelectedList = {
                            selectedItems.remove(it as DeliveryNoteState)
                            if (selectedItems.isEmpty()) {
                                selectedMode.value = false
                            }
                        },
                        keyToUnselectAll = keyToResetCheckboxes.value
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (documentsUiState.deliveryNoteStates.size == 1)
                        DisplayBatHelperAdvice()
                }

                Column(
                    // apply darker background when bottom menu is expanded
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor.value),
                ) {}
            }
        }

        when {
            openAlertDialog.value -> {
                AlertDialogDeleteDocument(
                    onDismissRequest = {
                        openAlertDialog.value = false
                        backgroundColor.value =
                            changeBackgroundWithVerticalGradient(backgroundColor.value)
                    },
                    onConfirmation = {
                        openAlertDialog.value = false
                        onClickDelete(selectedItems.toList())
                        selectedItems.clear()
                        selectedMode.value = false
                        backgroundColor.value =
                            changeBackgroundWithVerticalGradient(backgroundColor.value)
                    }
                )
            }
        }
    }


}

@Composable
private fun DisplayBatHelperAdvice() {
    var visibleText by remember { mutableIntStateOf(0) }
    val numberOfIterations = remember { mutableIntStateOf(1) }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(top = 40.dp)
            .padding(
                top = 20.dp,
                start = 40.dp,
                end = 40.dp,
                bottom = 20.dp
            )
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() } // This is mandatory
            ) {
                if (visibleText < 2) {
                    visibleText += 1
                } else visibleText = 0
                numberOfIterations.intValue += 1
            }
        ) {
            AnimationLottie(
                modifier = Modifier
                    .width(130.dp)
                    .height(80.dp)
                    .align(Alignment.Center),
                file = R.raw.bat_wavy_arms,
                numberOfIteration = numberOfIterations.intValue
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        AnimatedVisibility(
            visible = visibleText == 1,
            enter = fadeIn(tween(1000)),
            exit = fadeOut(tween(100)),
        ) {
            Text(
                text = Strings.get(R.string.delivery_note_advice_convert),
                textAlign = TextAlign.Center
            )
        }

        AnimatedVisibility(
            visible = visibleText == 2,
            enter = fadeIn(
                tween(
                    2000,
                    delayMillis = 100,
                    easing = LinearOutSlowInEasing
                )
            ),
            exit = fadeOut(tween(100)),
        ) {
            Text(
                text = Strings.get(R.string.delivery_note_advice_convert2),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun resetSelectedItems(
    selectedItems: MutableList<DeliveryNoteState>,
    selectedMode: MutableState<Boolean>,
    keyToResetCheckboxes: MutableState<Boolean>,
) {
    selectedItems.clear()
    selectedMode.value = false
    // Allow to "reset" the checkbox rememberValue to false inDeliveryNoteListItem when recomposing
    keyToResetCheckboxes.value = !keyToResetCheckboxes.value
}



