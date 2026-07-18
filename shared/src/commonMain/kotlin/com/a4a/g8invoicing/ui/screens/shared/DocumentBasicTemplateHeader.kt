package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.addressed_to
import com.a4a.g8invoicing.shared.resources.credit_note_number
import com.a4a.g8invoicing.shared.resources.delivery_note_number
import com.a4a.g8invoicing.shared.resources.document_date_label
import com.a4a.g8invoicing.shared.resources.invoice_number
import com.a4a.g8invoicing.shared.resources.quote_number
import com.a4a.g8invoicing.ui.shared.ImageStorage
import com.a4a.g8invoicing.ui.shared.InitImageContext
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.loadLogoBitmap
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.CreditNoteState
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.states.QuoteState
import com.a4a.g8invoicing.ui.states.DocumentState
import com.a4a.g8invoicing.ui.theme.subTitleForDocuments
import com.a4a.g8invoicing.ui.theme.textForDocumentsSecondary
import com.a4a.g8invoicing.ui.theme.titleForDocuments
import org.jetbrains.compose.resources.stringResource

@Composable
fun DocumentBasicTemplateHeader(
    document: DocumentState,
    onClickElement: (ScreenElement) -> Unit,
    selectedItem: ScreenElement?,
    labels: Map<String, String>? = null,
) {
    // Initialize image context and load logo
    InitImageContext()
    val imageStorage = remember { ImageStorage() }
    var logoBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val logoPath = document.documentIssuer?.logoPath

    LaunchedEffect(logoPath) {
        logoBitmap = if (logoPath != null && imageStorage.logoExists(logoPath)) {
            loadLogoBitmap(imageStorage.getAbsolutePath(logoPath))
        } else {
            null
        }
    }

    // Header with title/date (left) and logo (right)
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .customCombinedClickable(
                onClick = {
                    onClickElement(ScreenElement.DOCUMENT_HEADER)
                },
                onLongClick = {
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Title and date on the left
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                modifier = Modifier
                    .getBorder(ScreenElement.DOCUMENT_NUMBER, selectedItem)
                    .customCombinedClickable(
                        onClick = {
                            onClickElement(ScreenElement.DOCUMENT_NUMBER)
                        },
                        onLongClick = {
                        }
                    ),
                style = MaterialTheme.typography.titleForDocuments,
                text = when (document) {
                    is DeliveryNoteState -> documentLabel(labels, "delivery_note_number", Res.string.delivery_note_number)
                    is CreditNoteState -> documentLabel(labels, "credit_note_number", Res.string.credit_note_number)
                    is QuoteState -> documentLabel(labels, "quote_number", Res.string.quote_number)
                    else -> documentLabel(labels, "invoice_number", Res.string.invoice_number)
                } + " " + document.documentNumber.text
            )

            Spacer(
                modifier = Modifier
                    .padding(bottom = 6.dp)
            )

            Text(
                modifier = Modifier
                    .getBorder(ScreenElement.DOCUMENT_DATE, selectedItem)
                    .customCombinedClickable(
                        onClick = {
                            onClickElement(ScreenElement.DOCUMENT_DATE)
                        },
                        onLongClick = {
                        }
                    ),
                style = MaterialTheme.typography.subTitleForDocuments,
                text = documentLabel(labels, "document_date_label", Res.string.document_date_label) + document.documentDate.substringBefore(
                    " "
                )
            )
        }

        // Logo on the right (if present)
        if (logoBitmap != null) {
            Image(
                bitmap = logoBitmap!!,
                contentDescription = "Logo",
                contentScale = ContentScale.FillHeight,
                modifier = Modifier.height(40.dp)
            )
        }
    }
    val client = document.documentClient

    // ROW 1: NOTHING ------- CLIENT ADDRESS TITLE 1
    val numberOfAddresses = client?.addresses?.size ?: 0
    val defaultTitle = documentLabel(labels, "addressed_to", Res.string.addressed_to)
    Row(
        modifier = Modifier.padding(top = 12.dp)
    ) {
        Spacer(
            modifier = Modifier
                .fillMaxWidth(0.5f)
        )
        // Show "Adressé à" by default when there's only one address and no custom title
        val addressTitle = client?.addresses?.getOrNull(0)?.addressTitle?.text
        val displayTitle = if (numberOfAddresses == 1 && addressTitle.isNullOrEmpty()) {
            defaultTitle
        } else {
            addressTitle ?: ""
        }
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (displayTitle.isNotEmpty()) {
                Text(
                    modifier = Modifier
                        .padding(bottom = 1.dp),
                    style = MaterialTheme.typography.textForDocumentsSecondary,
                    color = Color.DarkGray,
                    text = displayTitle
                )
            }
        }
    }

    // ROW 2 : ISSUER --------- CLIENT ADDRESS 1
    Row() {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .getBorder(ScreenElement.DOCUMENT_ISSUER, selectedItem)
                .customCombinedClickable(
                    onClick = {
                        onClickElement(ScreenElement.DOCUMENT_ISSUER)
                    },
                    onLongClick = {
                    }
                )
        ) {
            document.documentIssuer?.let {
                DocumentBasicTemplateClientOrIssuer(it, labels = labels)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            document.documentClient?.let {
                DocumentClientRectangleAndContent(
                    it,
                    onClickElement,
                    selectedItem,
                    addressIndex = 0,
                    displayAllInfo = true,
                    labels = labels,
                )
            }
        }
    }

    document.documentClient?.addresses?.let { addresses ->
        if (addresses.size > 1) {
            // ROW 3:  CLIENT ADDRESS TITLE 2 ------- TITLE 3
            // Explicit top padding so the additional-address titles don't stick to
            // the bottom of the issuer block when the issuer is taller than the
            // primary client rectangle. The PDF renderer already spaces this via
            // dedicated iText spacer cells.
            Row(
                modifier = Modifier.padding(top = 7.dp)
            ) {
                // When there are only 2 addresses, put address 2 on the right (below address 1)
                if (addresses.size == 2) {
                    Spacer(modifier = Modifier.fillMaxWidth(0.5f))
                }
                for (i in 1..<addresses.size) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(if (i == 1 && addresses.size > 2) 0.5f else 1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(bottom = 2.dp),
                            style = MaterialTheme.typography.textForDocumentsSecondary,
                            text = addresses.getOrNull(i)?.addressTitle?.text ?: ""
                        )
                    }
                }
            }

            // ROW 4:  CLIENT ADDRESS 2 ----------ADDRESS 3
            Row {
                // When there are only 2 addresses, put address 2 on the right (below address 1)
                if (addresses.size == 2) {
                    Spacer(modifier = Modifier.fillMaxWidth(0.5f))
                }
                for (i in 1..<addresses.size) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(if (i == 1 && addresses.size > 2) 0.5f else 1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        document.documentClient?.let {
                            DocumentClientRectangleAndContent(
                                it,
                                onClickElement,
                                selectedItem,
                                addressIndex = i,
                                displayAllInfo = false,
                                labels = labels,
                            )
                        }
                    }
                    if (i == 1 && addresses.size > 2) Spacer(modifier = Modifier.width(2.dp))
                }
            }
        }
    }
}

@Composable
fun DocumentClientRectangleAndContent(
    documentClient: ClientOrIssuerState,
    onClickElement: (ScreenElement) -> Unit,
    selectedItem: ScreenElement?,
    addressIndex: Int,
    displayAllInfo: Boolean,
    labels: Map<String, String>? = null,
) {

    Column(
        modifier = Modifier
            .padding(bottom = 6.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var customModifier2 = Modifier
            .getBorder(ScreenElement.DOCUMENT_CLIENT, selectedItem)
            .customCombinedClickable(
                onClick = {
                    onClickElement(ScreenElement.DOCUMENT_CLIENT)
                },
                onLongClick = {
                }
            )
            .border(
                1.dp,
                SolidColor(Color.LightGray),
                shape = RoundedCornerShape(15.dp)
            )
            .padding(top = 8.dp)
            .fillMaxWidth()
        customModifier2 = if (addressIndex == 0)
            customModifier2.then(
                Modifier
                    .padding(bottom = 8.dp)
            )
        else
            customModifier2.then(
                Modifier
                    .padding(bottom = 4.dp)
            )

        Column(
            modifier = customModifier2,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DocumentBasicTemplateClientOrIssuer(
                documentClient,
                addressIndex = addressIndex,
                displayAllInfo = displayAllInfo,
                labels = labels,
            )
        }
    }
}
