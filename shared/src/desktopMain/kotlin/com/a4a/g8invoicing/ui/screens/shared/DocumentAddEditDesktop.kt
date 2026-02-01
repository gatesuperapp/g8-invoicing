package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.a4a.g8invoicing.data.models.ClientOrIssuerType
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.appbar_invoices
import com.a4a.g8invoicing.shared.resources.appbar_delivery_notes
import com.a4a.g8invoicing.shared.resources.appbar_credit_notes
import com.a4a.g8invoicing.shared.resources.document_client
import com.a4a.g8invoicing.shared.resources.document_date
import com.a4a.g8invoicing.shared.resources.document_due_date
import com.a4a.g8invoicing.shared.resources.document_footer
import com.a4a.g8invoicing.shared.resources.document_free_field
import com.a4a.g8invoicing.shared.resources.document_issuer
import com.a4a.g8invoicing.shared.resources.document_number_short
import com.a4a.g8invoicing.shared.resources.document_reference
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.CreditNoteState
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.states.InvoiceState
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.ui.theme.ColorBackgroundGrey
import com.a4a.g8invoicing.ui.theme.ColorVioletLight
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import org.jetbrains.compose.resources.stringResource

@Composable
fun DocumentAddEditDesktop(
    document: DocumentState,
    clientList: MutableList<ClientOrIssuerState>,
    issuerList: MutableList<ClientOrIssuerState>,
    documentClientUiState: ClientOrIssuerState,
    documentIssuerUiState: ClientOrIssuerState,
    documentProductUiState: DocumentProductState,
    taxRates: List<BigDecimal>,
    products: MutableList<ProductState>,
    onClickBack: () -> Unit,
    onValueChange: (ScreenElement, Any) -> Unit,
    onSelectProduct: (ProductState, Int?) -> Unit,
    onClickNewDocumentProduct: () -> Unit,
    onSelectClientOrIssuer: (ClientOrIssuerState) -> Unit,
    onClickEditDocumentProduct: (DocumentProductState) -> Unit,
    onClickNewDocumentClientOrIssuer: (ClientOrIssuerType) -> Unit,
    onClickDocumentClientOrIssuer: (ClientOrIssuerState) -> Unit,
    onClickDeleteDocumentProduct: (Int) -> Unit,
    onClickDeleteDocumentClientOrIssuer: (ClientOrIssuerType) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    bottomFormOnValueChange: (ScreenElement, Any, ClientOrIssuerType?) -> Unit,
    bottomFormPlaceCursor: (ScreenElement, ClientOrIssuerType?) -> Unit,
    onClickDoneForm: (DocumentBottomSheetTypeOfForm) -> Unit,
    onClickCancelForm: () -> Unit,
    onSelectTaxRate: (BigDecimal?) -> Unit,
    showDocumentForm: Boolean,
    onShowDocumentForm: (Boolean) -> Unit,
    onClickDeleteAddress: (ClientOrIssuerType) -> Unit,
    onOrderChange: (List<DocumentProductState>) -> Unit,
    exportPdfContent: @Composable (DocumentState, () -> Unit) -> Unit,
) {
    val localFocusManager = LocalFocusManager.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showExportPopup by remember { mutableStateOf(false) }

    // Zoom fluide (comme sur mobile)
    var zoom by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // Determine document type for breadcrumb
    val documentTypeName = when (document) {
        is InvoiceState -> stringResource(Res.string.appbar_invoices)
        is DeliveryNoteState -> stringResource(Res.string.appbar_delivery_notes)
        is CreditNoteState -> stringResource(Res.string.appbar_credit_notes)
        else -> "Document"
    }

    // Export popup
    if (showExportPopup) {
        Dialog(
            onDismissRequest = {},
            DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(10F),
                contentAlignment = Alignment.Center
            ) {
                exportPdfContent(document) { showExportPopup = false }
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackgroundGrey)
    ) {
        // Left panel: Document preview
        Column(
            modifier = Modifier
                .weight(0.65f)
                .fillMaxHeight()
        ) {
            // Breadcrumb + indicateur zoom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Breadcrumb (gauche)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = documentTypeName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.clickable { onClickBack() }
                    )
                    Text(
                        text = " › ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Text(
                        text = document.documentNumber.text.ifEmpty { "..." },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray
                    )
                }

                // Indicateur zoom (droite) - clic pour reset si zoomé
                Surface(
                    modifier = Modifier
                        .clickable(enabled = zoom > 1f) {
                            zoom = 1f
                            offsetX = 0f
                            offsetY = 0f
                        }
                        .padding(4.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = if (zoom > 1f) ColorVioletLight.copy(alpha = 0.1f) else Color.Transparent
                ) {
                    Text(
                        text = if (zoom > 1f) "${(zoom * 100).toInt()}% ✕" else "${(zoom * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (zoom > 1f) ColorVioletLight else Color.Gray,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Document preview area avec zoom molette et drag
            @OptIn(ExperimentalComposeUiApi::class)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 12.dp, bottom = 24.dp)
                    .onPointerEvent(PointerEventType.Scroll) { event ->
                        val scrollDelta = event.changes.first().scrollDelta.y
                        val zoomFactor = if (scrollDelta > 0) 0.9f else 1.1f
                        zoom = (zoom * zoomFactor).coerceIn(1f, 3f)
                        // Reset offset si zoom revient à 1
                        if (zoom == 1f) {
                            offsetX = 0f
                            offsetY = 0f
                        }
                    }
                    // Double-clic pour reset le zoom
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                zoom = 1f
                                offsetX = 0f
                                offsetY = 0f
                            }
                        )
                    }
                    // Drag pour naviguer quand zoomé
                    .pointerInput(zoom) {
                        if (zoom > 1f) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val maxX = (size.width * (zoom - 1) / 2f)
                                val maxY = (size.height * (zoom - 1) / 2f)
                                offsetX = (offsetX + dragAmount.x).coerceIn(-maxX, maxX)
                                offsetY = (offsetY + dragAmount.y).coerceIn(-maxY, maxY)
                            }
                        }
                    },
                contentAlignment = Alignment.TopCenter
            ) {
                Surface(
                    modifier = Modifier
                        .width(595.dp)
                        .padding(vertical = 16.dp)
                        .graphicsLayer {
                            scaleX = zoom
                            scaleY = zoom
                            translationX = offsetX
                            translationY = offsetY
                        },
                    shape = RoundedCornerShape(4.dp),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    DocumentBasicTemplate(
                        uiState = document,
                        onClickElement = { element ->
                            // Switch to appropriate tab when clicking on document
                            selectedTab = when (element) {
                                ScreenElement.DOCUMENT_PRODUCT -> 1
                                else -> 0
                            }
                        },
                        onClickRestOfThePage = {
                            localFocusManager.clearFocus()
                        }
                    )
                }
            }
        }

        // Right panel: Edit panel
        Surface(
            modifier = Modifier
                .weight(0.35f)
                .fillMaxHeight(),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Export PDF button
                Button(
                    onClick = { showExportPopup = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorVioletLight,
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Exporter PDF",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Tabs
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = ColorVioletLight,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = ColorVioletLight
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "Texte",
                                color = if (selectedTab == 0) ColorVioletLight else Color.Gray
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "Produits",
                                color = if (selectedTab == 1) ColorVioletLight else Color.Gray
                            )
                        }
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Tab content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    val contentScrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(contentScrollState)
                    ) {
                        when (selectedTab) {
                            0 -> DocumentDesktopTextTab(
                                document = document,
                                onValueChange = onValueChange,
                                clientList = clientList,
                                issuerList = issuerList,
                                documentClientUiState = documentClientUiState,
                                documentIssuerUiState = documentIssuerUiState,
                                taxRates = taxRates,
                                onSelectClientOrIssuer = onSelectClientOrIssuer,
                                onClickNewDocumentClientOrIssuer = onClickNewDocumentClientOrIssuer,
                                onClickDocumentClientOrIssuer = onClickDocumentClientOrIssuer,
                                onClickDeleteDocumentClientOrIssuer = onClickDeleteDocumentClientOrIssuer,
                                placeCursorAtTheEndOfText = placeCursorAtTheEndOfText,
                                bottomFormOnValueChange = bottomFormOnValueChange,
                                bottomFormPlaceCursor = bottomFormPlaceCursor,
                                onClickDoneForm = onClickDoneForm,
                                onClickCancelForm = onClickCancelForm,
                                onSelectTaxRate = onSelectTaxRate,
                                showDocumentForm = showDocumentForm,
                                onShowDocumentForm = onShowDocumentForm,
                                onClickDeleteAddress = onClickDeleteAddress
                            )

                            1 -> DocumentDesktopProductsTab(
                                document = document,
                                documentProductUiState = documentProductUiState,
                                products = products,
                                taxRates = taxRates,
                                onSelectProduct = { product ->
                                    onSelectProduct(product, document.documentClient?.originalClientId)
                                },
                                onClickNewProduct = onClickNewDocumentProduct,
                                onClickDocumentProduct = onClickEditDocumentProduct,
                                onClickDeleteDocumentProduct = onClickDeleteDocumentProduct,
                                bottomFormOnValueChange = bottomFormOnValueChange,
                                bottomFormPlaceCursor = bottomFormPlaceCursor,
                                onClickDoneForm = onClickDoneForm,
                                onClickCancelForm = onClickCancelForm,
                                onSelectTaxRate = onSelectTaxRate,
                                showDocumentForm = showDocumentForm,
                                onShowDocumentForm = onShowDocumentForm,
                                onOrderChange = onOrderChange
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentDesktopTextTab(
    document: DocumentState,
    onValueChange: (ScreenElement, Any) -> Unit,
    clientList: MutableList<ClientOrIssuerState>,
    issuerList: MutableList<ClientOrIssuerState>,
    documentClientUiState: ClientOrIssuerState,
    documentIssuerUiState: ClientOrIssuerState,
    taxRates: List<BigDecimal>,
    onSelectClientOrIssuer: (ClientOrIssuerState) -> Unit,
    onClickNewDocumentClientOrIssuer: (ClientOrIssuerType) -> Unit,
    onClickDocumentClientOrIssuer: (ClientOrIssuerState) -> Unit,
    onClickDeleteDocumentClientOrIssuer: (ClientOrIssuerType) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    bottomFormOnValueChange: (ScreenElement, Any, ClientOrIssuerType?) -> Unit,
    bottomFormPlaceCursor: (ScreenElement, ClientOrIssuerType?) -> Unit,
    onClickDoneForm: (DocumentBottomSheetTypeOfForm) -> Unit,
    onClickCancelForm: () -> Unit,
    onSelectTaxRate: (BigDecimal?) -> Unit,
    showDocumentForm: Boolean,
    onShowDocumentForm: (Boolean) -> Unit,
    onClickDeleteAddress: (ClientOrIssuerType) -> Unit,
) {
    val numberLabel = stringResource(Res.string.document_number_short)
    val dateLabel = stringResource(Res.string.document_date)
    val referenceLabel = stringResource(Res.string.document_reference)
    val freeFieldLabel = stringResource(Res.string.document_free_field)
    val issuerLabel = stringResource(Res.string.document_issuer)
    val clientLabel = stringResource(Res.string.document_client)
    val dueDateLabel = stringResource(Res.string.document_due_date)
    val footerLabel = stringResource(Res.string.document_footer)

    // State for dialogs/pickers
    var showDatePicker by remember { mutableStateOf(false) }
    var showDueDatePicker by remember { mutableStateOf(false) }
    var showIssuerPicker by remember { mutableStateOf(false) }
    var showClientPicker by remember { mutableStateOf(false) }
    var showFooterEditor by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Numéro
        DocumentDesktopTextField(
            label = numberLabel,
            value = document.documentNumber,
            onValueChange = { onValueChange(ScreenElement.DOCUMENT_NUMBER, it) }
        )

        // Date
        DocumentDesktopClickableField(
            label = dateLabel,
            value = document.documentDate.substringBefore(" "),
            onClick = { showDatePicker = true },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Outlined.DateRange,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        )

        // Référence
        DocumentDesktopTextField(
            label = referenceLabel,
            value = document.reference,
            onValueChange = { onValueChange(ScreenElement.DOCUMENT_REFERENCE, it) }
        )

        // Champ libre
        DocumentDesktopTextField(
            label = freeFieldLabel,
            value = document.freeField,
            onValueChange = { onValueChange(ScreenElement.DOCUMENT_FREE_FIELD, it) },
            singleLine = false,
            minLines = 3
        )

        // Émetteur
        DocumentDesktopClickableField(
            label = issuerLabel,
            value = document.documentIssuer?.let {
                (it.firstName?.text?.let { fn -> "$fn " } ?: "") + it.name.text
            } ?: "",
            onClick = { showIssuerPicker = true }
        )

        // Client
        DocumentDesktopClickableField(
            label = clientLabel,
            value = document.documentClient?.let {
                (it.firstName?.text?.let { fn -> "$fn " } ?: "") + it.name.text
            } ?: "",
            onClick = { showClientPicker = true }
        )

        // Échéance (only for invoices)
        if (document is InvoiceState) {
            DocumentDesktopClickableField(
                label = dueDateLabel,
                value = document.dueDate.substringBefore(" "),
                onClick = { showDueDatePicker = true },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.DateRange,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }

        // Pied de page
        DocumentDesktopClickableField(
            label = footerLabel,
            value = document.footerText.text.take(50) + if (document.footerText.text.length > 50) "..." else "",
            onClick = { showFooterEditor = true }
        )
    }

    // Date picker dialog
    if (showDatePicker) {
        DocumentDesktopDatePickerDialog(
            initialDate = document.documentDate,
            onDismiss = { showDatePicker = false },
            onDateSelected = { date ->
                onValueChange(ScreenElement.DOCUMENT_DATE, date)
                showDatePicker = false
            }
        )
    }

    // Due date picker dialog (for invoices)
    if (showDueDatePicker && document is InvoiceState) {
        DocumentDesktopDatePickerDialog(
            initialDate = document.dueDate,
            onDismiss = { showDueDatePicker = false },
            onDateSelected = { date ->
                onValueChange(ScreenElement.DOCUMENT_DUE_DATE, date)
                showDueDatePicker = false
            }
        )
    }

    // Issuer picker dialog
    if (showIssuerPicker) {
        DocumentDesktopPickerDialog(
            title = issuerLabel,
            items = issuerList,
            currentItemId = document.documentIssuer?.id,
            onDismiss = { showIssuerPicker = false },
            onSelect = { issuer ->
                onSelectClientOrIssuer(issuer)
                showIssuerPicker = false
            },
            onClickNew = {
                onClickNewDocumentClientOrIssuer(ClientOrIssuerType.ISSUER)
                showIssuerPicker = false
            },
            onClickEdit = { issuer ->
                onClickDocumentClientOrIssuer(issuer)
                showIssuerPicker = false
            }
        )
    }

    // Client picker dialog
    if (showClientPicker) {
        DocumentDesktopPickerDialog(
            title = clientLabel,
            items = clientList,
            currentItemId = document.documentClient?.id,
            onDismiss = { showClientPicker = false },
            onSelect = { client ->
                onSelectClientOrIssuer(client)
                showClientPicker = false
            },
            onClickNew = {
                onClickNewDocumentClientOrIssuer(ClientOrIssuerType.CLIENT)
                showClientPicker = false
            },
            onClickEdit = { client ->
                onClickDocumentClientOrIssuer(client)
                showClientPicker = false
            }
        )
    }

    // Footer editor dialog
    if (showFooterEditor) {
        DocumentDesktopFooterDialog(
            currentText = document.footerText,
            onDismiss = { showFooterEditor = false },
            onSave = { text ->
                onValueChange(ScreenElement.DOCUMENT_FOOTER, text)
                showFooterEditor = false
            }
        )
    }
}

@Composable
private fun DocumentDesktopProductsTab(
    document: DocumentState,
    documentProductUiState: DocumentProductState,
    products: MutableList<ProductState>,
    taxRates: List<BigDecimal>,
    onSelectProduct: (ProductState) -> Unit,
    onClickNewProduct: () -> Unit,
    onClickDocumentProduct: (DocumentProductState) -> Unit,
    onClickDeleteDocumentProduct: (Int) -> Unit,
    bottomFormOnValueChange: (ScreenElement, Any, ClientOrIssuerType?) -> Unit,
    bottomFormPlaceCursor: (ScreenElement, ClientOrIssuerType?) -> Unit,
    onClickDoneForm: (DocumentBottomSheetTypeOfForm) -> Unit,
    onClickCancelForm: () -> Unit,
    onSelectTaxRate: (BigDecimal?) -> Unit,
    showDocumentForm: Boolean,
    onShowDocumentForm: (Boolean) -> Unit,
    onOrderChange: (List<DocumentProductState>) -> Unit,
) {
    var showProductPicker by remember { mutableStateOf(false) }
    var typeOfCreation by remember { mutableStateOf(DocumentBottomSheetTypeOfForm.ADD_EXISTING_PRODUCT) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Add product buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    typeOfCreation = DocumentBottomSheetTypeOfForm.NEW_PRODUCT
                    onShowDocumentForm(true)
                    onClickNewProduct()
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE8E8E8),
                    contentColor = Color.DarkGray
                )
            ) {
                Text("+ Nouveau", fontSize = 13.sp)
            }

            Button(
                onClick = { showProductPicker = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE8E8E8),
                    contentColor = Color.DarkGray
                )
            ) {
                Text("Choisir", fontSize = 13.sp)
            }
        }

        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

        // List of document products
        document.documentProducts?.forEach { product ->
            DocumentDesktopProductItem(
                product = product,
                onClick = {
                    onClickDocumentProduct(product)
                    typeOfCreation = DocumentBottomSheetTypeOfForm.EDIT_PRODUCT
                    onShowDocumentForm(true)
                },
                onDelete = { product.id?.let { onClickDeleteDocumentProduct(it) } }
            )
        }

        if (document.documentProducts.isNullOrEmpty()) {
            Text(
                text = "Aucun produit ajouté",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }

    // Product picker dialog
    if (showProductPicker) {
        DocumentDesktopProductPickerDialog(
            products = products,
            onDismiss = { showProductPicker = false },
            onSelect = { product ->
                onSelectProduct(product)
                typeOfCreation = DocumentBottomSheetTypeOfForm.ADD_EXISTING_PRODUCT
                showProductPicker = false
                onShowDocumentForm(true)
            }
        )
    }

    // Product form dialog
    if (showDocumentForm) {
        DocumentDesktopProductFormDialog(
            typeOfCreation = typeOfCreation,
            documentProduct = documentProductUiState,
            taxRates = taxRates,
            bottomFormOnValueChange = bottomFormOnValueChange,
            onSelectTaxRate = onSelectTaxRate,
            onClickCancel = {
                onClickCancelForm()
                onShowDocumentForm(false)
            },
            onClickDone = {
                onClickDoneForm(typeOfCreation)
                onShowDocumentForm(false)
            }
        )
    }
}

@Composable
private fun DocumentDesktopTextField(
    label: String,
    value: TextFieldValue?,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value ?: TextFieldValue(""),
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = ColorVioletLight,
                cursorColor = ColorVioletLight
            ),
            singleLine = singleLine,
            minLines = minLines
        )
    }
}

@Composable
private fun DocumentDesktopClickableField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value.ifEmpty { "-" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (value.isEmpty()) Color.LightGray else Color.DarkGray,
                    modifier = Modifier.weight(1f)
                )
                trailingIcon?.invoke()
            }
        }
    }
}

@Composable
private fun DocumentDesktopProductItem(
    product: DocumentProductState,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF5F5F5)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name.text,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${product.quantity} x ${product.priceWithTax ?: product.priceWithoutTax ?: "-"} €",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Text("×", fontSize = 20.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun DocumentDesktopPickerDialog(
    title: String,
    items: List<ClientOrIssuerState>,
    currentItemId: Int?,
    onDismiss: () -> Unit,
    onSelect: (ClientOrIssuerState) -> Unit,
    onClickNew: () -> Unit,
    onClickEdit: (ClientOrIssuerState) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(12.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onClickNew,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorVioletLight
                    )
                ) {
                    Text("+ Nouveau")
                }

                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items.forEach { item ->
                        val isSelected = item.id == currentItemId
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(item) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) ColorVioletLight.copy(alpha = 0.1f) else Color(0xFFF5F5F5),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, ColorVioletLight) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = (item.firstName?.text?.let { "$it " } ?: "") + item.name.text,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray
                        )
                    ) {
                        Text("Fermer")
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentDesktopProductPickerDialog(
    products: List<ProductState>,
    onDismiss: () -> Unit,
    onSelect: (ProductState) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.7f),
            shape = RoundedCornerShape(12.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Choisir un produit",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    products.forEach { product ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(product) },
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF5F5F5)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = product.name.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    product.defaultPriceWithoutTax?.let {
                                        Text(
                                            text = "$it € HT",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (products.isEmpty()) {
                        Text(
                            text = "Aucun produit disponible",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray
                        )
                    ) {
                        Text("Fermer")
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentDesktopFooterDialog(
    currentText: TextFieldValue,
    onDismiss: () -> Unit,
    onSave: (TextFieldValue) -> Unit
) {
    var text by remember { mutableStateOf(currentText) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f),
            shape = RoundedCornerShape(12.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Pied de page",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = ColorVioletLight,
                        cursorColor = ColorVioletLight
                    )
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray
                        )
                    ) {
                        Text("Annuler")
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = { onSave(text) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorVioletLight
                        )
                    ) {
                        Text("Valider")
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentDesktopDatePickerDialog(
    initialDate: String,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(12.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                DocumentBottomSheetDatePicker(
                    initialDate = initialDate,
                    onValueChange = { date ->
                        onDateSelected(date)
                    }
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray
                        )
                    ) {
                        Text("Fermer")
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentDesktopProductFormDialog(
    typeOfCreation: DocumentBottomSheetTypeOfForm,
    documentProduct: DocumentProductState,
    taxRates: List<BigDecimal>,
    bottomFormOnValueChange: (ScreenElement, Any, ClientOrIssuerType?) -> Unit,
    onSelectTaxRate: (BigDecimal?) -> Unit,
    onClickCancel: () -> Unit,
    onClickDone: () -> Unit
) {
    var showTaxPicker by remember { mutableStateOf(false) }

    val title = when (typeOfCreation) {
        DocumentBottomSheetTypeOfForm.NEW_PRODUCT -> "Nouveau produit"
        DocumentBottomSheetTypeOfForm.ADD_EXISTING_PRODUCT -> "Ajouter produit"
        DocumentBottomSheetTypeOfForm.EDIT_PRODUCT -> "Modifier produit"
        else -> "Produit"
    }

    val isDoneEnabled = documentProduct.name.text.isNotEmpty() &&
            documentProduct.quantity != com.ionspin.kotlin.bignum.decimal.BigDecimal.ZERO

    Dialog(onDismissRequest = onClickCancel) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(12.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(16.dp))

                // Form content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Nom
                    DocumentDesktopTextField(
                        label = "Nom",
                        value = documentProduct.name,
                        onValueChange = { bottomFormOnValueChange(ScreenElement.DOCUMENT_PRODUCT_NAME, it, null) }
                    )

                    // Description
                    DocumentDesktopTextField(
                        label = "Description",
                        value = documentProduct.description,
                        onValueChange = { bottomFormOnValueChange(ScreenElement.DOCUMENT_PRODUCT_DESCRIPTION, it, null) },
                        singleLine = false,
                        minLines = 2
                    )

                    // Quantité
                    DocumentDesktopDecimalField(
                        label = "Quantité",
                        value = documentProduct.quantity.toPlainString(),
                        onValueChange = { bottomFormOnValueChange(ScreenElement.DOCUMENT_PRODUCT_QUANTITY, it, null) }
                    )

                    // Unité
                    DocumentDesktopTextField(
                        label = "Unité",
                        value = documentProduct.unit,
                        onValueChange = { bottomFormOnValueChange(ScreenElement.DOCUMENT_PRODUCT_UNIT, it, null) }
                    )

                    // TVA
                    DocumentDesktopClickableField(
                        label = "TVA",
                        value = documentProduct.taxRate?.let { "${it.toPlainString()}%" } ?: "-",
                        onClick = { showTaxPicker = true }
                    )

                    // Prix HT
                    DocumentDesktopDecimalField(
                        label = "Prix unitaire HT",
                        value = documentProduct.priceWithoutTax?.toPlainString() ?: "",
                        onValueChange = { bottomFormOnValueChange(ScreenElement.DOCUMENT_PRODUCT_PRICE_WITHOUT_TAX, it, null) }
                    )

                    // Prix TTC
                    DocumentDesktopDecimalField(
                        label = "Prix unitaire TTC",
                        value = documentProduct.priceWithTax?.toPlainString() ?: "",
                        onValueChange = { bottomFormOnValueChange(ScreenElement.DOCUMENT_PRODUCT_PRICE_WITH_TAX, it, null) }
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Footer buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onClickCancel,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray
                        )
                    ) {
                        Text("Annuler")
                    }

                    Spacer(Modifier.width(8.dp))

                    Button(
                        onClick = onClickDone,
                        enabled = isDoneEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ColorVioletLight
                        )
                    ) {
                        Text("Valider")
                    }
                }
            }
        }
    }

    // Tax picker dialog
    if (showTaxPicker) {
        DocumentDesktopTaxPickerDialog(
            taxRates = taxRates,
            currentTaxRate = documentProduct.taxRate,
            onDismiss = { showTaxPicker = false },
            onSelect = { rate ->
                onSelectTaxRate(rate)
                showTaxPicker = false
            }
        )
    }
}

@Composable
private fun DocumentDesktopDecimalField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = ColorVioletLight,
                cursorColor = ColorVioletLight
            ),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
            )
        )
    }
}

@Composable
private fun DocumentDesktopTaxPickerDialog(
    taxRates: List<BigDecimal>,
    currentTaxRate: BigDecimal?,
    onDismiss: () -> Unit,
    onSelect: (BigDecimal?) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.8f),
            shape = RoundedCornerShape(12.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Taux de TVA",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(Modifier.height(16.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    taxRates.forEach { rate ->
                        val isSelected = rate == currentTaxRate
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(rate) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) ColorVioletLight.copy(alpha = 0.1f) else Color(0xFFF5F5F5),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, ColorVioletLight) else null
                        ) {
                            Text(
                                text = "${rate.toPlainString()}%",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray
                        )
                    ) {
                        Text("Fermer")
                    }
                }
            }
        }
    }
}
