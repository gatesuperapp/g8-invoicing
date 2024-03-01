package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.data.ClientOrIssuerEditable
import com.a4a.g8invoicing.ui.states.CompanyDataState
import com.a4a.g8invoicing.ui.states.DeliveryNoteState
import com.a4a.g8invoicing.ui.states.DocumentProductState
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.theme.ColorGreenPaidCompl
import com.a4a.g8invoicing.ui.theme.textForDocuments
import com.a4a.g8invoicing.ui.theme.textForDocumentsImportant
import com.a4a.g8invoicing.ui.theme.textForDocumentsSecondary
import java.math.BigDecimal

@Composable
fun DeliveryNoteBasicTemplateContent(
    uiState: DeliveryNoteState,
    onClickDeliveryNoteNumber: () -> Unit,
    onClickDate: () -> Unit,
    onClickIssuer: () -> Unit,
    onClickClient: () -> Unit,
    onClickOrderNumber: () -> Unit,
    onClickDocumentProducts: () -> Unit,
    selectedItem: ScreenElement?,
) {
    Column(
        modifier = Modifier
            .background(Color.White)
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    modifier = Modifier
                        .getBorder(ScreenElement.DOCUMENT_NUMBER, selectedItem)
                        .customCombinedClickable(
                            onClick = {
                                onClickDeliveryNoteNumber()
                            },
                            onLongClick = {
                            }
                        ),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.textForDocuments,
                    text = stringResource(id = R.string.delivery_note_number) + " " + (uiState.number?.text
                        ?: stringResource(id = R.string.delivery_note_default_number))
                )

                Spacer(
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                )

                Text(
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .getBorder(ScreenElement.DOCUMENT_DATE, selectedItem)
                        .customCombinedClickable(
                            onClick = {
                                onClickDate()
                            },
                            onLongClick = {
                            }
                        ),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.textForDocuments,
                    text = stringResource(id = R.string.delivery_note_date) + " " + (uiState.deliveryDate
                        ?: stringResource(id = R.string.delivery_note_default_date))
                )
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .getBorder(ScreenElement.DOCUMENT_ISSUER, selectedItem)
                    .customCombinedClickable(
                        onClick = {
                            onClickIssuer()
                        },
                        onLongClick = {
                        }
                    )
                    .padding(top = 10.dp)
                    .weight(1f)
                    .fillMaxWidth(0.3f)
            ) {
                BuildClientOrIssuerInTemplate(uiState.issuer ?: fakeIssuer())
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.5f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .padding(bottom = 2.dp),
                    style = MaterialTheme.typography.textForDocumentsSecondary,
                    text = stringResource(id = R.string.delivery_note_recipient)
                )

                Column(
                    modifier = Modifier
                        .getBorder(ScreenElement.DOCUMENT_CLIENT, selectedItem)
                        .customCombinedClickable(
                            onClick = {
                                onClickClient()
                            },
                            onLongClick = {
                            }
                        )
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            SolidColor(Color.LightGray),
                            shape = RoundedCornerShape(15.dp)
                        )
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BuildClientOrIssuerInTemplate(uiState.client ?: fakeClient())
                }
            }
        }
        Spacer(
            modifier = Modifier
                .padding(top = 20.dp)
        )
        Row(
            Modifier
                .getBorder(ScreenElement.DOCUMENT_ORDER_NUMBER, selectedItem)
                .customCombinedClickable(
                    onClick = {
                        onClickOrderNumber()
                    },
                    onLongClick = {
                    }
                )

        ) {
            Text(
                style = MaterialTheme.typography.textForDocumentsImportant,
                text = stringResource(id = R.string.delivery_note_order_number) + " : "
            )
            Text(
                style = MaterialTheme.typography.textForDocumentsImportant,
                text = uiState.orderNumber?.text   ?: stringResource(id = R.string.delivery_note_default_order_number)
            )
        }

        Spacer(
            modifier = Modifier
                .padding(bottom = 6.dp)
        )

        Column(
            Modifier
                .getBorder(ScreenElement.DOCUMENT_PRODUCTS, selectedItem)
                .customCombinedClickable(
                    onClick = {
                        onClickDocumentProducts()
                    },
                    onLongClick = {
                    }
                )
                .fillMaxWidth()
        ) {
            // The table with all line items
            DataTable(uiState.documentProducts ?: fakeDocumentProducts())
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        ) {
            Spacer(
                modifier = Modifier
                    .weight(1f)
            )
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(
                    end = 8.dp
                )
            ) {
                // Will display the following:
                // Total HT
                // TVA 20% :
                // TVA 5% :
                // Total TTC :
                Text(
                    modifier = Modifier
                        .padding(bottom = 3.dp),
                    style = MaterialTheme.typography.textForDocuments,
                    text = stringResource(id = R.string.delivery_note_total_without_tax) + " "
                )
                uiState.documentPrices?.totalAmountsOfEachTax?.forEach() {
                    Text(
                        modifier = Modifier
                            .padding(bottom = 3.dp),
                        style = MaterialTheme.typography.textForDocuments,
                        text = stringResource(id = R.string.delivery_note_tax) + " " + it.first.toString() + "% : "
                    )
                }
                Text(
                    style = MaterialTheme.typography.textForDocumentsImportant,
                    text = stringResource(id = R.string.delivery_note_total_with_tax) + " "
                )
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Will display the values as follows:
                // Total HT : 12€
                // TVA 20% : 1€
                // TVA 5% : 1€
                // Total TTC : 14€
                Text(
                    modifier = Modifier
                        .padding(bottom = 3.dp, end = 3.dp),
                    style = MaterialTheme.typography.textForDocuments,
                    text = uiState.documentPrices?.totalPriceWithoutTax.toString() + stringResource(id = R.string.currency)
                )
                uiState.documentPrices?.totalAmountsOfEachTax?.forEach {
                    Text(
                        modifier = Modifier
                            .padding(bottom = 3.dp, end = 3.dp),
                        style = MaterialTheme.typography.textForDocuments,
                        text = it.second.toString() + stringResource(id = R.string.currency)
                    )
                }

                Text(
                    modifier = Modifier
                        .padding(end = 3.dp),
                    style = MaterialTheme.typography.textForDocumentsImportant,
                    text = uiState.documentPrices?.totalPriceWithTax.toString() + stringResource(id = R.string.currency)
                )
            }
        }
    }
}

private fun Modifier.getBorder(item: ScreenElement, selectedItem: ScreenElement?) = then(
    border(
        if (item == selectedItem) {
            BorderStroke(1.dp, ColorGreenPaidCompl)
        } else {
            BorderStroke(0.dp, Color.Transparent)
        }
    )
)


// Remove the indicator on click
@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.customCombinedClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onLongClickLabel: String? = null,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) = composed(
    inspectorInfo = debugInspectorInfo {
        name = "combinedClickable"
        properties["enabled"] = enabled
        properties["onClickLabel"] = onClickLabel
        properties["role"] = role
        properties["onClick"] = onClick
        properties["onDoubleClick"] = onDoubleClick
        properties["onLongClick"] = onLongClick
        properties["onLongClickLabel"] = onLongClickLabel
    }
) {
    Modifier.combinedClickable(
        enabled = enabled,
        onClickLabel = onClickLabel,
        onLongClickLabel = onLongClickLabel,
        onLongClick = onLongClick,
        onDoubleClick = onDoubleClick,
        onClick = onClick,
        role = role,
        indication = null, // Removing the indicator on click
        interactionSource = remember { MutableInteractionSource() }
    )
}

@Composable
private fun fakeIssuer() =
    ClientOrIssuerEditable(
        // Fill with dummy values to show how it looks
        id = null,
        firstName = TextFieldValue(text =  stringResource(id = R.string.delivery_note_default_issuer_firstName)),
        name = TextFieldValue(text =  stringResource(id = R.string.delivery_note_default_issuer_name)),
        address1 = TextFieldValue(text =  stringResource(id = R.string.delivery_note_default_issuer_address1)),
        address2 = TextFieldValue(text =  stringResource(id = R.string.delivery_note_default_issuer_address2)),
        zipCode = TextFieldValue(text =  stringResource(id = R.string.delivery_note_default_issuer_zipCode)),
        city = TextFieldValue(text =  stringResource(id = R.string.delivery_note_default_issuer_city)),
        phone = TextFieldValue(text =  stringResource(id = R.string.delivery_note_default_issuer_phone)),
        email = TextFieldValue(text =  stringResource(id = R.string.delivery_note_default_issuer_email)),
        notes = TextFieldValue(text = stringResource(id = R.string.delivery_note_default_issuer_company_notes)),
        companyData = listOf(
            CompanyDataState(
                label = stringResource(id = R.string.delivery_note_default_issuer_company_label1),
                number = stringResource(id = R.string.delivery_note_default_issuer_company_number1)
            ),
            CompanyDataState(
                label = stringResource(id = R.string.delivery_note_default_issuer_company_label2),
                number = stringResource(id = R.string.delivery_note_default_issuer_company_number2)
            )
        )
    )

@Composable
private fun fakeClient() =
    ClientOrIssuerEditable(
        id = null,
        firstName = TextFieldValue(text =  stringResource(id = R.string.delivery_note_default_client_firstName)),
        name = TextFieldValue(text =  stringResource(id = R.string.delivery_note_default_client_name)),
        address1 = TextFieldValue(text =  stringResource(id = R.string.delivery_note_default_client_address1)),
        address2 = null,
        zipCode = TextFieldValue(text =  stringResource(id = R.string.delivery_note_default_client_zipCode)),
        city = TextFieldValue(text =  stringResource(id = R.string.delivery_note_default_client_city)),
        phone = TextFieldValue(text =  stringResource(id = R.string.delivery_note_default_client_phone)),
        email = TextFieldValue(text =  stringResource(id = R.string.delivery_note_default_client_email)),
        notes = TextFieldValue(text = stringResource(id = R.string.delivery_note_default_client_notes)),
        companyData = listOf(
            CompanyDataState(
                label = stringResource(id = R.string.delivery_note_default_client_company_label1),
                number = stringResource(id = R.string.delivery_note_default_client_company_number1)
            )
        )
    )

@Composable
private fun fakeDocumentProducts() =
    listOf(
        DocumentProductState(
            id = 1,
            name = TextFieldValue(stringResource(id = R.string.delivery_note_default_document_product_name)),
            description = TextFieldValue(stringResource(id = R.string.delivery_note_default_document_product_description)),
            finalPrice = BigDecimal(4),
            taxRate = BigDecimal(20),
            priceWithoutTax = BigDecimal(4.80),
            quantity = BigDecimal(2),
            unit = TextFieldValue(stringResource(id = R.string.delivery_note_default_document_product_unit)),
            productId = 1
        )
    )