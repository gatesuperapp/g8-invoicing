package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a4a.g8invoicing.data.stripTrailingZeros
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.appbar_product_add
import com.a4a.g8invoicing.shared.resources.appbar_product_edit
import com.a4a.g8invoicing.shared.resources.client_form_cancel
import com.a4a.g8invoicing.shared.resources.client_form_validate
import com.a4a.g8invoicing.shared.resources.product_add_price
import com.a4a.g8invoicing.shared.resources.product_delete_price
import com.a4a.g8invoicing.shared.resources.product_description
import com.a4a.g8invoicing.shared.resources.product_description_input
import com.a4a.g8invoicing.shared.resources.product_name
import com.a4a.g8invoicing.shared.resources.product_name_input
import com.a4a.g8invoicing.shared.resources.product_price
import com.a4a.g8invoicing.shared.resources.product_price_client
import com.a4a.g8invoicing.shared.resources.product_price_default
import com.a4a.g8invoicing.shared.resources.product_price_input
import com.a4a.g8invoicing.shared.resources.product_price_with_tax
import com.a4a.g8invoicing.shared.resources.product_price_without_tax
import com.a4a.g8invoicing.shared.resources.product_tax
import com.a4a.g8invoicing.shared.resources.product_unit
import com.a4a.g8invoicing.shared.resources.product_unit_input
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.ProductState
import com.a4a.g8invoicing.ui.theme.ColorBackgroundGrey
import com.a4a.g8invoicing.ui.theme.ColorDarkGray
import com.a4a.g8invoicing.ui.theme.ColorVioletLight
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProductAddEditFormDesktop(
    product: ProductState,
    onValueChange: (ScreenElement, Any, String?) -> Unit,
    onClickForward: (ScreenElement) -> Unit,
    onClickDeletePrice: (priceId: String) -> Unit,
    onClickAddPrice: () -> Unit,
    onClickSelectClients: (priceId: String) -> Unit,
    onRemoveClient: (priceId: String, clientId: Int) -> Unit,
    onClickDone: () -> Unit,
    onClickBack: () -> Unit,
    isNewProduct: Boolean = true,
) {
    val localFocusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val titleText = if (isNewProduct) stringResource(Res.string.appbar_product_add) else stringResource(Res.string.appbar_product_edit)
    val cancelText = stringResource(Res.string.client_form_cancel)
    val validateText = stringResource(Res.string.client_form_validate)

    val nameLabel = stringResource(Res.string.product_name)
    val namePlaceholder = stringResource(Res.string.product_name_input)
    val descriptionLabel = stringResource(Res.string.product_description)
    val descriptionPlaceholder = stringResource(Res.string.product_description_input)
    val unitLabel = stringResource(Res.string.product_unit)
    val unitPlaceholder = stringResource(Res.string.product_unit_input)
    val taxLabel = stringResource(Res.string.product_tax)
    val priceLabel = stringResource(Res.string.product_price)
    val priceDefaultLabel = stringResource(Res.string.product_price_default)
    val pricePlaceholder = stringResource(Res.string.product_price_input)
    val priceWithoutTaxLabel = stringResource(Res.string.product_price_without_tax)
    val priceWithTaxLabel = stringResource(Res.string.product_price_with_tax)
    val priceClientLabel = stringResource(Res.string.product_price_client)
    val addPriceText = stringResource(Res.string.product_add_price)
    val deletePriceText = stringResource(Res.string.product_delete_price)

    val hasAdditionalPrices = !product.additionalPrices.isNullOrEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackgroundGrey)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    localFocusManager.clearFocus()
                })
            }
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(vertical = 32.dp)
                    .padding(end = 12.dp)
                    .imePadding(),
                contentAlignment = Alignment.TopCenter
            ) {
                Surface(
                    modifier = Modifier
                        .widthIn(max = 700.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp)
                    ) {
                        // Header
                        Text(
                            text = titleText,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (product.name.text.isNotEmpty()) {
                            Text(
                                text = product.name.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 24.dp),
                            color = Color.LightGray.copy(alpha = 0.5f)
                        )

                        // PRODUIT Section
                        ProductSectionTitle("PRODUIT")
                        Spacer(Modifier.height(16.dp))

                        // Nom
                        ProductDesktopFormField(
                            label = nameLabel,
                            value = product.name,
                            placeholder = namePlaceholder,
                            onValueChange = { onValueChange(ScreenElement.PRODUCT_NAME, it, null) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(16.dp))

                        // Description
                        ProductDesktopFormField(
                            label = descriptionLabel,
                            value = product.description,
                            placeholder = descriptionPlaceholder,
                            onValueChange = { onValueChange(ScreenElement.PRODUCT_DESCRIPTION, it, null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = false,
                            minLines = 2
                        )

                        Spacer(Modifier.height(16.dp))

                        // Unité
                        ProductDesktopFormField(
                            label = unitLabel,
                            value = product.unit,
                            placeholder = unitPlaceholder,
                            onValueChange = { onValueChange(ScreenElement.PRODUCT_UNIT, it, null) },
                            modifier = Modifier.fillMaxWidth(0.5f)
                        )

                        Spacer(Modifier.height(32.dp))

                        // TARIFICATION Section
                        ProductSectionTitle("TARIFICATION")
                        Spacer(Modifier.height(16.dp))

                        // TVA (clickable field)
                        ProductDesktopClickableField(
                            label = taxLabel,
                            value = product.taxRate?.let {
                                "${it.stripTrailingZeros().toPlainString().replace(".", ",")}%"
                            } ?: "-",
                            onClick = { onClickForward(ScreenElement.PRODUCT_TAX_RATE) },
                            modifier = Modifier.fillMaxWidth(0.3f)
                        )

                        Spacer(Modifier.height(16.dp))

                        // Prix par défaut HT / TTC
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ProductDesktopDecimalField(
                                label = if (hasAdditionalPrices) "$priceDefaultLabel $priceWithoutTaxLabel" else "$priceLabel $priceWithoutTaxLabel",
                                value = product.defaultPriceWithoutTax?.toPlainString() ?: "",
                                placeholder = pricePlaceholder,
                                onValueChange = { onValueChange(ScreenElement.PRODUCT_DEFAULT_PRICE_WITHOUT_TAX, it, null) },
                                modifier = Modifier.weight(1f)
                            )
                            ProductDesktopDecimalField(
                                label = if (hasAdditionalPrices) "$priceDefaultLabel $priceWithTaxLabel" else "$priceLabel $priceWithTaxLabel",
                                value = product.defaultPriceWithTax?.toPlainString() ?: "",
                                placeholder = product.taxRate?.let {
                                    (BigDecimal.fromInt(3) + BigDecimal.fromInt(3) * it / BigDecimal.fromInt(100)).toPlainString()
                                } ?: "",
                                onValueChange = { onValueChange(ScreenElement.PRODUCT_DEFAULT_PRICE_WITH_TAX, it, null) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Prix additionnels
                        product.additionalPrices?.let { prices ->
                            prices.forEach { currentPrice ->
                                key(currentPrice.idStr) {
                                    Spacer(Modifier.height(24.dp))

                                    HorizontalDivider(
                                        color = Color.LightGray.copy(alpha = 0.3f)
                                    )

                                    Spacer(Modifier.height(16.dp))

                                    // Header avec bouton supprimer
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Prix spécifique",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.DarkGray
                                        )
                                        IconButton(
                                            onClick = { onClickDeletePrice(currentPrice.idStr) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Delete,
                                                tint = ColorDarkGray,
                                                contentDescription = deletePriceText
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    // Prix HT / TTC
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        ProductDesktopDecimalField(
                                            label = "$priceLabel $priceWithoutTaxLabel",
                                            value = currentPrice.priceWithoutTax?.toPlainString() ?: "",
                                            placeholder = pricePlaceholder,
                                            onValueChange = { onValueChange(ScreenElement.PRODUCT_OTHER_PRICE_WITHOUT_TAX, it, currentPrice.idStr) },
                                            modifier = Modifier.weight(1f)
                                        )
                                        ProductDesktopDecimalField(
                                            label = "$priceLabel $priceWithTaxLabel",
                                            value = currentPrice.priceWithTax?.toPlainString() ?: "",
                                            placeholder = "",
                                            onValueChange = { onValueChange(ScreenElement.PRODUCT_OTHER_PRICE_WITH_TAX, it, currentPrice.idStr) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Spacer(Modifier.height(12.dp))

                                    // Clients
                                    ProductDesktopClientsPicker(
                                        label = priceClientLabel,
                                        clients = currentPrice.clients,
                                        onClick = { onClickSelectClients(currentPrice.idStr) },
                                        onRemoveClient = { clientId -> onRemoveClient(currentPrice.idStr, clientId) }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(24.dp))

                        // Bouton ajouter un prix
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color(0xFFE8E8E8),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { onClickAddPrice() }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = addPriceText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }

            VerticalScrollbar(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(end = 4.dp, top = 4.dp, bottom = 4.dp),
                adapter = rememberScrollbarAdapter(scrollState),
                style = LocalScrollbarStyle.current.copy(
                    thickness = 8.dp,
                    hoverColor = ColorVioletLight.copy(alpha = 0.5f),
                    unhoverColor = Color.LightGray.copy(alpha = 0.5f)
                )
            )
        }

        // Footer fixe
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onClickBack,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color.Gray),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.DarkGray
                    )
                ) {
                    Text(
                        text = cancelText,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                Spacer(Modifier.padding(8.dp))

                Button(
                    onClick = onClickDone,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorVioletLight,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = validateText,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = ColorVioletLight,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp
    )
}

@Composable
private fun ProductDesktopFormField(
    label: String,
    value: TextFieldValue?,
    placeholder: String,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value ?: TextFieldValue(""),
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color.LightGray
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = ColorVioletLight,
                cursorColor = ColorVioletLight
            ),
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = keyboardType
            )
        )
    }
}

@Composable
private fun ProductDesktopDecimalField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = Color.LightGray
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = ColorVioletLight,
                cursorColor = ColorVioletLight
            ),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Decimal
            )
        )
    }
}

@Composable
private fun ProductDesktopClickableField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
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
            border = BorderStroke(1.dp, Color.LightGray),
            color = Color.White
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun ProductDesktopClientsPicker(
    label: String,
    clients: List<com.a4a.g8invoicing.ui.states.ClientRef>,
    onClick: () -> Unit,
    onRemoveClient: (Int) -> Unit,
    modifier: Modifier = Modifier
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
            border = BorderStroke(1.dp, Color.LightGray),
            color = Color.White
        ) {
            if (clients.isEmpty()) {
                Text(
                    text = "Sélectionner...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.LightGray,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    clients.forEach { client ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ColorVioletLight.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, ColorVioletLight.copy(alpha = 0.3f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
                            ) {
                                Text(
                                    text = client.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray
                                )
                                IconButton(
                                    onClick = { onRemoveClient(client.id) },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Text(
                                        text = "×",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
