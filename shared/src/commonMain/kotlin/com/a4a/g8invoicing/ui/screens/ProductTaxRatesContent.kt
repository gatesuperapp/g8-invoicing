package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.data.stripTrailingZeros
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.tax_rate_add_rate
import com.a4a.g8invoicing.ui.shared.Separator
import com.a4a.g8invoicing.ui.theme.callForActions
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProductTaxRatesContent(
    taxRates: List<BigDecimal>?,
    currentTaxRate: BigDecimal?,
    onSelectTaxRate: (BigDecimal?) -> Unit,
    isDisplayedInBottomSheet: Boolean = false,
    onClickEditRates: (() -> Unit)? = null
) {
    var chosenOption by remember { mutableStateOf(currentTaxRate) }
    val scrollState = rememberScrollState()

    var modifier = Modifier.fillMaxWidth()

    modifier = if (isDisplayedInBottomSheet)
        modifier.then(Modifier
            .fillMaxHeight(0.5f)
            .background(Color.LightGray.copy(alpha = 0.4f))
            .padding(top = 30.dp, end = 60.dp, start = 60.dp, bottom = 30.dp))
    else
        modifier.then(Modifier
            .padding(12.dp)
        )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    )
    {
        Column(
            modifier = Modifier
                .background(color = Color.White, shape = RoundedCornerShape(6.dp))
                .fillMaxWidth()
        ) {
            // Adds the no tax ("-") choice to the list
            val taxRatesIncludingNoTax = taxRates?.toMutableList()
            taxRatesIncludingNoTax?.add(0, BigDecimal.ZERO)

            taxRatesIncludingNoTax?.forEachIndexed { index, taxRate ->
                val currentChosenOption = chosenOption
                val isCurrentTaxRate = if(currentTaxRate == null) {
                    taxRate == BigDecimal.ZERO
                } else currentChosenOption != null && taxRate.compareTo(currentChosenOption) == 0
                //We use compareTo() because if we use taxRate == chosenOption it compares 20 with 20.0 and returns false

                val isFirst = index == 0
                val isLast = taxRate == taxRatesIncludingNoTax.lastOrNull()

                // Column with background - Separator at top (except first) included in background
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isCurrentTaxRate) Color(0xFFF0F0F0) else Color.Transparent
                        )
                        .clickable {
                            chosenOption = taxRate
                            onSelectTaxRate(
                                if (taxRate == BigDecimal.ZERO) {
                                    null
                                } else taxRate
                            )
                        }
                ) {
                    // Separator at top of each item (except first)
                    if (!isFirst) {
                        Separator()
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = 20.dp,
                                end = 20.dp,
                                top = if (isFirst) 24.dp else 16.dp,
                                bottom = if (isLast) 24.dp else 16.dp
                            )
                    ) {
                        Text(
                            modifier = Modifier
                                .weight(1F)
                                .padding(
                                    start = 20.dp,
                                    end = 8.dp
                                ),
                            text = if (taxRate == BigDecimal.ZERO) {
                                "-"
                            } else {
                                "${taxRate.stripTrailingZeros().toPlainString().replace(".", ",")}%"
                            },
                            style = LocalTextStyle.current
                        )
                    }
                }
            }
        }

        // Settings icon below the block, aligned to the right
        if (onClickEditRates != null && !isDisplayedInBottomSheet) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFFf7f7f7),
                            shape = RoundedCornerShape(6.dp)
                        )
                ) {
                    IconButton(
                        onClick = onClickEditRates,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Settings,
                            contentDescription = "Modifier les taux",
                            modifier = Modifier.size(20.dp),
                            tint = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductTaxRatesEditContent(
    taxRates: List<Pair<Long?, BigDecimal>>,
    onUpdateRate: (Int, BigDecimal) -> Unit,
    onAddRate: () -> Unit,
    onDeleteRate: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(
            modifier = Modifier
                .background(color = Color.White, shape = RoundedCornerShape(6.dp))
                .fillMaxWidth()
                .padding(
                    top = 8.dp,
                    bottom = 8.dp
                )
        ) {
            taxRates.forEachIndexed { index, (id, rate) ->
                TaxRateEditRow(
                    rate = rate,
                    onUpdateRate = { newRate -> onUpdateRate(index, newRate) },
                    onDeleteRate = { onDeleteRate(index) },
                    canDelete = taxRates.size > 1
                )

                if (index < taxRates.size - 1) {
                    Separator()
                }
            }
        }

        // "Ajouter un taux" button with background
        Box(
            modifier = Modifier
                .padding(start = 4.dp, top = 4.dp)
                .background(
                    color = Color(0xFFf7f7f7),
                    shape = RoundedCornerShape(6.dp)
                )
                .clickable(enabled = true) {
                    onAddRate()
                }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                style = MaterialTheme.typography.callForActions,
                text = AnnotatedString(stringResource(Res.string.tax_rate_add_rate)),
            )
        }
    }
}

@Composable
private fun TaxRateEditRow(
    rate: BigDecimal,
    onUpdateRate: (BigDecimal) -> Unit,
    onDeleteRate: () -> Unit,
    canDelete: Boolean
) {
    var isEditing by remember { mutableStateOf(rate == BigDecimal.ZERO) }
    val initialText = if (rate == BigDecimal.ZERO) "" else rate.stripTrailingZeros().toPlainString().replace(".", ",")
    // Don't use rate as key - we manage the text state independently during editing
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(
            text = initialText,
            selection = TextRange(initialText.length)
        ))
    }
    val focusRequester = remember { FocusRequester() }

    // Update text when rate changes from external source (not during editing)
    LaunchedEffect(rate) {
        if (!isEditing) {
            val newText = if (rate == BigDecimal.ZERO) "" else rate.stripTrailingZeros().toPlainString().replace(".", ",")
            textFieldValue = TextFieldValue(
                text = newText,
                selection = TextRange(newText.length)
            )
        }
    }

    // Request focus when entering edit mode
    LaunchedEffect(isEditing) {
        if (isEditing) {
            focusRequester.requestFocus()
        }
    }

    // Function to save the current text as rate
    fun saveCurrentRate() {
        val normalizedValue = textFieldValue.text.replace(",", ".")
        if (normalizedValue.isNotEmpty() && normalizedValue != ".") {
            try {
                val newRate = BigDecimal.parseString(normalizedValue)
                onUpdateRate(newRate)
            } catch (e: Exception) {
                // Invalid number, ignore
            }
        }
        isEditing = false
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 20.dp,
                end = 8.dp,
                top = 12.dp,
                bottom = 12.dp
            )
    ) {
        if (isEditing) {
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    // Allow numbers, comma, and period - convert period to comma for display
                    val withComma = newValue.text.replace('.', ',')
                    val filtered = withComma.filter { it.isDigit() || it == ',' }
                    // Only allow one comma
                    val commaCount = filtered.count { it == ',' }
                    if (commaCount <= 1) {
                        // Keep cursor position relative to filtered text
                        val newCursorPos = minOf(newValue.selection.start, filtered.length)

                        textFieldValue = TextFieldValue(
                            text = filtered,
                            selection = TextRange(newCursorPos)
                        )
                        // Don't update rate here - only update when user clicks Done
                    }
                },
                modifier = Modifier
                    .weight(1F)
                    .padding(start = 20.dp, end = 8.dp)
                    .focusRequester(focusRequester),
                textStyle = LocalTextStyle.current,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        innerTextField()
                        Text("%", style = LocalTextStyle.current)
                    }
                }
            )
            IconButton(
                onClick = { saveCurrentRate() },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Done,
                    contentDescription = "Valider",
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Text(
                modifier = Modifier
                    .weight(1F)
                    .padding(start = 20.dp, end = 8.dp),
                text = "${rate.stripTrailingZeros().toPlainString().replace(".", ",")}%",
                style = LocalTextStyle.current
            )
            IconButton(
                onClick = { isEditing = true },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Modifier",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Gray
                )
            }
        }

        if (canDelete) {
            Spacer(modifier = Modifier.width(4.dp))
            IconButton(
                onClick = onDeleteRate,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Supprimer",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Gray
                )
            }
        }
    }
}
