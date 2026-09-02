package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.document_bottom_sheet_add_product
import com.a4a.g8invoicing.ui.shared.ButtonAddOrChoose
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.RetentionState
import org.jetbrains.compose.resources.stringResource

// Bottom sheet with the "Add a product" button (opens the picker with search + list)
// and the list of chosen products underneath.
@Composable
fun DocumentBottomSheetProductsChosen(
    list: List<DocumentProductState>,
    onClickChooseExisting: () -> Unit, // Opens the product picker bottom sheet
    onClickDocumentProduct: (DocumentProductState) -> Unit, // Edit an existing document product
    onClickDelete: (Int) -> Unit,
    onOrderChange: (List<DocumentProductState>) -> Unit,
    hideLinkedSourceHeaders: Boolean = false,
    onToggleHideLinkedSourceHeaders: (() -> Unit)? = null,
    // Retention lines are pinned at the bottom of the doc, no drag handle,
    // no delete. Tapping a row opens the retention edit sheet.
    retentions: List<RetentionState> = emptyList(),
    onClickRetention: (Int) -> Unit = {},
    onToggleRetentionHidden: (Int) -> Unit = {},
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            // No bottom padding here — let the inner LazyColumn's viewport
            // extend to the sheet's edge (which sits just above the system
            // nav bar thanks to windowInsetsPadding on the outer Column).
            // The LazyColumn's own contentPadding provides the visual
            // breathing room for the last row.
            .padding(start = 20.dp, end = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        ButtonAddOrChoose(
            onClickChooseExisting,
            hasBorder = false,
            isPickerButton = true,
            stringResource(Res.string.document_bottom_sheet_add_product)
        )
        // Display the list of chosen products + retentions inline. Retentions
        // are appended as trailing items in the same LazyColumn so they share
        // the products' contentPadding + vertical spacing. The 1-product
        // helper advice (a bat + tooltip) is passed as a trailing LazyColumn
        // item too — earlier it sat outside the LazyColumn as an unweighted
        // Column with fillMaxSize + verticalScroll, which grabbed ~130 dp of
        // the parent Column and pushed the last retention row below the
        // sheet fold. Inside the LazyColumn it participates in scroll like
        // the other rows, so the retentions stay reachable.
        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
            DocumentBottomSheetProductListChosenContent(
                documentProducts = list,
                onClickItem = onClickDocumentProduct,
                onClickDelete = onClickDelete,
                onOrderChange = onOrderChange,
                hideLinkedSourceHeaders = hideLinkedSourceHeaders,
                onToggleHideLinkedSourceHeaders = onToggleHideLinkedSourceHeaders,
                retentions = retentions,
                onClickRetention = onClickRetention,
                onToggleRetentionHidden = onToggleRetentionHidden,
                showBatHelperAdvice = list.size == 1,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}


