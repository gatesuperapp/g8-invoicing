package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.document_date_emitting
import com.a4a.g8invoicing.shared.resources.document_due_date
import com.a4a.g8invoicing.shared.resources.document_footer
import com.a4a.g8invoicing.shared.resources.document_payment_bank_picker_title
import com.a4a.g8invoicing.shared.resources.document_payment_means_default_label
import com.a4a.g8invoicing.shared.resources.issuer_bank_identifier_generic
import com.a4a.g8invoicing.shared.resources.issuer_bank_identifier_iban
import com.a4a.g8invoicing.shared.resources.document_payment_means_picker_title
import com.a4a.g8invoicing.shared.resources.document_payment_terms
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import org.jetbrains.compose.resources.stringResource

@Composable
fun DocumentBottomSheetElementsAfterSlide(
    pageElement: ScreenElement?,
    parameters: Any?,
    onClickBack: () -> Unit,
    documentClientUiState: ClientOrIssuerState,
    documentIssuerUiState: ClientOrIssuerState,
    taxRates: List<BigDecimal>,
    onSelectClientOrIssuer: (ClientOrIssuerState) -> Unit,
    onClickNewDocumentClientOrIssuer: (ClientOrIssuerType) -> Unit,
    // `openFormOnCompletion` = true means the caller wants the bottom-sheet edit
    // form to open once the version check settles (edit-link flow). false means
    // the caller only wants the check to run so the mismatch dialog fires, but
    // no form should appear afterwards (refresh-from-master icon flow).
    onClickEditDocumentClientOrIssuer: (ClientOrIssuerState, openFormOnCompletion: Boolean) -> Unit,
    onClickDeleteDocumentClientOrIssuer: (ClientOrIssuerType) -> Unit,
    currentClientId: Int? = null,
    currentIssuerId: Int? = null,
    bottomFormOnValueChange: (ScreenElement, Any, ClientOrIssuerType?) -> Unit,
    bottomFormPlaceCursor: (ScreenElement, ClientOrIssuerType?) -> Unit,
    onClickDoneForm: (DocumentBottomSheetTypeOfForm, syncToMaster: Boolean) -> Unit,
    onClickCancelForm: () -> Unit,
    onSelectTaxRate: (BigDecimal?) -> Unit,
    showDocumentForm: Boolean = false,
    onShowDocumentForm: (Boolean) -> Unit,
    onValueChange: (ScreenElement, Any) -> Unit,
    onClickDeleteAddress: (ClientOrIssuerType) -> Unit,
    onClickDeleteEmail: (ClientOrIssuerType, Int) -> Unit = { _, _ -> },
    onAddEmail: (ClientOrIssuerType, String) -> Unit = { _, _ -> },
    onPendingEmailValidationResult: (ClientOrIssuerType, Boolean) -> Unit = { _, _ -> },
    showProductType: Boolean = false,
    ) {
    var typeOfCreation: DocumentBottomSheetTypeOfForm by remember {
        mutableStateOf(
            DocumentBottomSheetTypeOfForm.ADD_EXISTING_PRODUCT
        )
    }

    // Store strings in composable scope for use in lambdas
    val documentDateEmittingString = stringResource(Res.string.document_date_emitting)
    val documentDueDateString = stringResource(Res.string.document_due_date)
    val documentFooterString = stringResource(Res.string.document_footer)
    val documentPaymentTermsString = stringResource(Res.string.document_payment_terms)

    if (pageElement == ScreenElement.DOCUMENT_CLIENT || pageElement == ScreenElement.DOCUMENT_ISSUER) {
        val pair = parameters as Pair<ClientOrIssuerState?, List<ClientOrIssuerState>>
        val snapshot = pair.first
        val master = pair.second.firstOrNull { it.id == snapshot?.originalClientOrIssuerId }
        val snapshotVersion = snapshot?.originalVersion
        val masterVersion = master?.version
        val hasMasterUpdate = snapshotVersion != null && masterVersion != null &&
            masterVersion > snapshotVersion
        ClientOrIssuerPickerBottomSheet(
            pageElement = pageElement,
            list = pair.second,
            currentSelected = snapshot,
            hasMasterUpdate = hasMasterUpdate,
            onSelect = { onSelectClientOrIssuer(it) },
            onClickEdit = {
                // Only set the local typeOfCreation. The NavGraph checks for a
                // master version mismatch first and decides itself whether to
                // open the form now (no mismatch) or wait until the user has
                // dismissed the version-mismatch dialog. Opening synchronously
                // here would race the async check and stack the form on top
                // of the dialog before the user can react.
                typeOfCreation = if (pageElement == ScreenElement.DOCUMENT_CLIENT) {
                    DocumentBottomSheetTypeOfForm.EDIT_CLIENT
                } else DocumentBottomSheetTypeOfForm.EDIT_ISSUER
                onClickEditDocumentClientOrIssuer(it, true)
            },
            onClickDeselect = {
                onClickDeleteDocumentClientOrIssuer(
                    if (pageElement == ScreenElement.DOCUMENT_CLIENT) ClientOrIssuerType.DOCUMENT_CLIENT
                    else ClientOrIssuerType.DOCUMENT_ISSUER
                )
            },
            onClickRefreshFromMaster = {
                // Fires the existing version-mismatch dialog (wired at the NavGraph
                // level via onClickDocumentClientOrIssuer / checkVersionMismatch).
                // No form opens after the dialog is dismissed — refresh is a
                // dedicated action, not a shortcut into editing.
                onClickEditDocumentClientOrIssuer(it, false)
            },
            onClickNew = {
                onClickNewDocumentClientOrIssuer(
                    if (pageElement == ScreenElement.DOCUMENT_CLIENT) ClientOrIssuerType.DOCUMENT_CLIENT
                    else ClientOrIssuerType.DOCUMENT_ISSUER
                )
                typeOfCreation = if (pageElement == ScreenElement.DOCUMENT_CLIENT) {
                    DocumentBottomSheetTypeOfForm.NEW_CLIENT
                } else DocumentBottomSheetTypeOfForm.NEW_ISSUER
                onShowDocumentForm(true)
            },
            onDismiss = onClickBack,
        )
    }

    if (pageElement == ScreenElement.DOCUMENT_DATE) {
        DocumentBottomSheetFormSimple(
            onClickCancel = onClickBack,
            bottomSheetTitle = documentDateEmittingString,
            content = {
                DocumentBottomSheetDatePicker(
                    initialDate = parameters.let { it as String },
                    onValueChange = {
                        onValueChange(ScreenElement.DOCUMENT_DATE, it)
                        onClickBack()
                    }
                )
            },
            isDatePicker = true,
            screenElement = ScreenElement.DOCUMENT_DATE
        )
    }

    if (pageElement == ScreenElement.DOCUMENT_DUE_DATE) {
        DocumentBottomSheetFormSimple(
            onClickCancel = onClickBack,
            bottomSheetTitle = documentDueDateString,
            content = {
                DocumentBottomSheetDatePicker(
                    initialDate = parameters.let { it as String },
                    onValueChange = {
                        onValueChange(ScreenElement.DOCUMENT_DUE_DATE, it)
                        onClickBack()
                    }
                )
            },
            isDatePicker = true,
            screenElement = ScreenElement.DOCUMENT_DUE_DATE
        )
    }

    if (pageElement == ScreenElement.DOCUMENT_FOOTER) {
        var footerText by remember { mutableStateOf(parameters as TextFieldValue) }
        var showBottomSheet by remember { mutableStateOf(true) }

        if (showBottomSheet)
            DocumentBottomSheetFormSimple(
                onClickCancel = {
                    onClickBack()
                    showBottomSheet = false
                },
                onClickDone = {
                    onValueChange(it, footerText)
                    onClickBack()
                    showBottomSheet = false
                },
                bottomSheetTitle = documentFooterString,
                content = {
                    DocumentBottomSheetLargeText(
                        text = footerText,
                        onValueChange = {
                            footerText = it
                        }
                    )
                },
                screenElement = ScreenElement.DOCUMENT_FOOTER
            )
    }

    if (pageElement == ScreenElement.DOCUMENT_PAYMENT_TERMS) {
        var termsText by remember { mutableStateOf(parameters as TextFieldValue) }
        var showBottomSheet by remember { mutableStateOf(true) }

        if (showBottomSheet)
            DocumentBottomSheetFormSimple(
                onClickCancel = {
                    onClickBack()
                    showBottomSheet = false
                },
                onClickDone = {
                    onValueChange(it, termsText)
                    onClickBack()
                    showBottomSheet = false
                },
                bottomSheetTitle = documentPaymentTermsString,
                content = {
                    DocumentBottomSheetLargeText(
                        text = termsText,
                        onValueChange = { termsText = it },
                    )
                },
                screenElement = ScreenElement.DOCUMENT_PAYMENT_TERMS,
            )
    }

    if (pageElement == ScreenElement.DOCUMENT_PAYMENT_MEANS) {
        val params = parameters as? PaymentPickerParams ?: PaymentPickerParams(
            segments = emptyList(),
            hidden = false,
            masterIssuerId = null,
            selectedIban = null,
        )
        val segments = params.segments
        val hidden = params.hidden
        val selectedChips = com.a4a.g8invoicing.data.models.chipIdsFromSegments(segments)
        val labelsByChip: Map<String, String> =
            com.a4a.g8invoicing.data.models.PaymentMeans.entries.associate {
                it.chipId to stringResource(it.labelRes)
            }

        // Load the issuer's bank accounts once the picker opens. Empty until
        // the fetch lands — the IBAN dropdown stays hidden until then, so no
        // flash of empty state on modal open.
        val clientOrIssuerDataSource: com.a4a.g8invoicing.data.ClientOrIssuerLocalDataSourceInterface =
            org.koin.compose.koinInject()
        var issuerBanks by remember(params.masterIssuerId) {
            mutableStateOf(emptyList<com.a4a.g8invoicing.ui.states.IssuerBankState>())
        }
        androidx.compose.runtime.LaunchedEffect(params.masterIssuerId) {
            issuerBanks = params.masterIssuerId?.let {
                clientOrIssuerDataSource.getIssuerBanks(it)
            } ?: emptyList()
        }

        var showBottomSheet by remember { mutableStateOf(true) }
        var showPrefixEditor by remember { mutableStateOf(false) }
        var showBankEditor by remember { mutableStateOf(false) }

        // Default segments seeded on the fly if the doc has none stored yet —
        // avoids the user hitting "Modifier le texte" and finding an empty
        // editor because nothing was saved for a legacy row.
        val effectiveBankSegments = params.bankSegments.ifEmpty {
            com.a4a.g8invoicing.data.models.defaultPaymentBankSegments()
        }
        val identifierIbanLabel = stringResource(Res.string.issuer_bank_identifier_iban)
        val identifierGenericLabel = stringResource(Res.string.issuer_bank_identifier_generic)
        val identifierLabelForToken = if (
            com.a4a.g8invoicing.data.models.CountryCodes.isIbanCountry(params.bankCountry)
            || params.bankCountry == null
        ) identifierIbanLabel else identifierGenericLabel

        if (showBottomSheet)
            PaymentMeansPickerBottomSheet(
                title = stringResource(Res.string.document_payment_means_picker_title),
                onDismiss = {
                    onClickBack()
                    showBottomSheet = false
                },
            ) {
                PaymentMeansMultiSelect(
                    selectedChips = selectedChips,
                    otherChecked = params.otherChecked,
                    onToggleChip = { chipId ->
                        val newSegments = if (chipId in selectedChips)
                            com.a4a.g8invoicing.data.models.removeTokenFromSegments(segments, chipId)
                        else
                            com.a4a.g8invoicing.data.models.addTokenToSegments(segments, chipId)
                        onValueChange(ScreenElement.DOCUMENT_PAYMENT_MEANS_LABEL, newSegments)
                    },
                    onToggleOther = {
                        onValueChange(ScreenElement.DOCUMENT_PAYMENT_MEANS_OTHER, !params.otherChecked)
                    },
                    hidden = hidden,
                    onToggleHidden = {
                        onValueChange(ScreenElement.DOCUMENT_PAYMENT_MEANS_HIDDEN, !hidden)
                    },
                    onClickEditPrefix = { showPrefixEditor = true },
                    issuerBanks = issuerBanks,
                    selectedBankIban = params.selectedIban,
                    onBankPicked = { bank ->
                        onValueChange(ScreenElement.DOCUMENT_ISSUER_BANK_PICKED, bank)
                    },
                    bankHidden = params.bankHidden,
                    onToggleBankHidden = {
                        onValueChange(ScreenElement.DOCUMENT_PAYMENT_BANK_HIDDEN, !params.bankHidden)
                    },
                    onClickEditBankPrefix = { showBankEditor = true },
                    hasIssuer = params.hasIssuer,
                )
            }

        if (showPrefixEditor)
            DocumentBottomSheetFormSimple(
                onClickCancel = { showPrefixEditor = false },
                onClickDone = {
                    // The editor already fired onValueChange for each accepted
                    // edit; Done just closes the sub-sheet. Cancel behaves the
                    // same way — auto-save owns the persistence, not this button.
                    showPrefixEditor = false
                },
                bottomSheetTitle = stringResource(Res.string.document_payment_means_picker_title),
                content = {
                    PaymentMeansTextEditor(
                        segments = segments,
                        labelsByChip = labelsByChip,
                        onSegmentsChange = { newSegments ->
                            onValueChange(ScreenElement.DOCUMENT_PAYMENT_MEANS_LABEL, newSegments)
                        },
                    )
                },
                screenElement = ScreenElement.DOCUMENT_PAYMENT_MEANS_LABEL,
            )

        if (showBankEditor)
            DocumentBottomSheetFormSimple(
                onClickCancel = { showBankEditor = false },
                onClickDone = { showBankEditor = false },
                bottomSheetTitle = stringResource(Res.string.document_payment_bank_picker_title),
                content = {
                    PaymentBankTextEditor(
                        segments = effectiveBankSegments,
                        identifierLabel = identifierLabelForToken,
                        iban = params.bankIban,
                        bic = params.bankBic,
                        onSegmentsChange = { newSegments ->
                            onValueChange(ScreenElement.DOCUMENT_PAYMENT_BANK_LABEL, newSegments)
                        },
                    )
                },
                screenElement = ScreenElement.DOCUMENT_PAYMENT_BANK_LABEL,
            )
    }

    if (showDocumentForm) {
        DocumentBottomSheetForm(
            typeOfCreation = typeOfCreation,
            documentClientUiState = documentClientUiState,
            documentIssuerUiState = documentIssuerUiState,
            taxRates = taxRates,
            bottomFormOnValueChange = bottomFormOnValueChange,
            bottomFormPlaceCursor = bottomFormPlaceCursor,
            onClickCancel = { // Re-initialize
                onClickCancelForm()
                onShowDocumentForm(false)
            },
            onClickDone = { syncToMaster ->
                onClickDoneForm(typeOfCreation, syncToMaster)
            },
            onSelectTaxRate = onSelectTaxRate,
            onClickDeleteAddress = onClickDeleteAddress,
            onClickDeleteEmail = onClickDeleteEmail,
            onAddEmail = onAddEmail,
            onPendingEmailValidationResult = onPendingEmailValidationResult,
            showProductType = showProductType,
        )
    }
}

/**
 * Modal wrapper for the payment-means picker. Mirrors the visual language of
 * [ClientOrIssuerPickerBottomSheet]: grey pill drag handle, title top-left in
 * [MaterialTheme.typography.titleMedium], no Cancel / Save actions (every
 * mutation is auto-persisted by the picker content itself).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PaymentMeansPickerBottomSheet(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets(0) },
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color(0xFFE0E0E0)) },
    ) {
        // fillMaxHeight(0.85f) mirrors the ClientOrIssuerPickerBottomSheet so
        // both modals rise to the same visual height on the screen.
        Column(modifier = Modifier.fillMaxHeight(0.85f)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 24.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
            content()
        }
    }
}

/**
 * Payload passed to the payment-means picker branch: segments + hidden flag,
 * plus the info needed to hydrate + wire the IBAN dropdown (master issuer id
 * to fetch banks, currently-frozen IBAN for pre-selection).
 */
data class PaymentPickerParams(
    val segments: List<com.a4a.g8invoicing.data.models.PaymentLabelSegment>,
    val hidden: Boolean,
    val masterIssuerId: Long?,
    val selectedIban: String?,
    val bankHidden: Boolean = false,
    // True when an issuer is set on the invoice (via the "Émetteur" picker).
    // Drives the empty-state hint on the IBAN dropdown.
    val hasIssuer: Boolean = false,
    // "Autre" chip state — separate from [segments] since OTHER never
    // contributes a Token (UI-only marker).
    val otherChecked: Boolean = false,
    // Bank-details editor payload: current segments + resolved token values.
    val bankSegments: List<com.a4a.g8invoicing.data.models.PaymentBankSegment> = emptyList(),
    val bankIban: String = "",
    val bankBic: String = "",
    // Country of the frozen bank (ISO 3166-1). Drives the IBAN vs "N° de compte"
    // identifier label baked into the token render.
    val bankCountry: String? = null,
)
