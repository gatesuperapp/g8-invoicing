package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a4a.g8invoicing.data.CurrencyManager
import com.a4a.g8invoicing.data.ProductLocalDataSourceInterface
import com.a4a.g8invoicing.data.formatAmount
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.currency_picker_search
import com.a4a.g8invoicing.shared.resources.document_bottom_sheet_picker_empty_product
import com.a4a.g8invoicing.shared.resources.document_bottom_sheet_picker_recent
import com.a4a.g8invoicing.shared.resources.document_bottom_sheet_picker_title_product
import com.a4a.g8invoicing.ui.states.ProductState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

private val RowGrey = Color(0xFFF0F0F0)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProductPickerBottomSheet(
    products: List<ProductState>,
    clientId: Int?,
    onSelect: (ProductState) -> Unit,
    onClickNew: () -> Unit,
    onDismiss: () -> Unit,
) {
    val currencyManager: CurrencyManager = koinInject()
    val currencyCode = currencyManager.currentCurrency

    var query by remember { mutableStateOf(TextFieldValue("")) }
    var searchExpanded by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val sheetState = rememberModalBottomSheetState()

    // Preemptively expand the sheet BEFORE the IME animates in — see the
    // matching note in ClientOrIssuerPickerBottomSheet.
    LaunchedEffect(searchExpanded) {
        if (searchExpanded) {
            sheetState.expand()
            focusRequester.requestFocus()
        }
    }

    val nonNullProducts = remember(products) { products.filter { it.id != null } }

    // Only surface "Récents" when the list is long enough that jumping to a recent
    // entry actually saves scrolling.
    val showRecentsSection = nonNullProducts.size > 5

    val productDataSource: ProductLocalDataSourceInterface = koinInject()
    var recentIds by remember { mutableStateOf<List<Long>>(emptyList()) }
    LaunchedEffect(showRecentsSection) {
        recentIds = if (showRecentsSection) productDataSource.fetchLast3RecentProductIds()
        else emptyList()
    }

    val recentEntries = remember(recentIds, nonNullProducts) {
        recentIds.mapNotNull { rid -> nonNullProducts.firstOrNull { it.id?.toLong() == rid } }
    }

    val filteredAlphaSorted = remember(query.text, nonNullProducts) {
        val q = query.text.trim()
        val filtered = if (q.isEmpty()) nonNullProducts
        else nonNullProducts.filter {
            it.name.text.contains(q, ignoreCase = true) ||
                (it.description?.text?.contains(q, ignoreCase = true) == true)
        }
        filtered.sortedBy { it.name.text.trim().lowercase() }
    }

    val grouped = remember(filteredAlphaSorted) {
        filteredAlphaSorted.groupBy { it.name.text.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "#" }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets(0) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.85f)
                .imePadding()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 24.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AnimatedContent(
                    targetState = searchExpanded,
                    modifier = Modifier.weight(1f),
                    transitionSpec = {
                        if (targetState) {
                            (expandHorizontally(tween(180)) + fadeIn(tween(180))) togetherWith
                                (shrinkHorizontally(tween(180)) + fadeOut(tween(120)))
                        } else {
                            (fadeIn(tween(180))) togetherWith
                                (shrinkHorizontally(tween(180)) + fadeOut(tween(120)))
                        }
                    },
                    label = "search-header",
                ) { expanded ->
                    if (expanded) {
                        TextField(
                            value = query,
                            onValueChange = { query = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .focusRequester(focusRequester),
                            textStyle = TextStyle(fontSize = 14.sp),
                            placeholder = {
                                Text(
                                    text = stringResource(Res.string.currency_picker_search),
                                    fontSize = 14.sp,
                                    color = Color.Gray,
                                )
                            },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clickable {
                                            searchExpanded = false
                                            query = TextFieldValue("")
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Search,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = RowGrey,
                                unfocusedContainerColor = RowGrey,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                cursorColor = Color.Black,
                            ),
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(Res.string.document_bottom_sheet_picker_title_product),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(start = 12.dp),
                            )
                            Spacer(Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable { searchExpanded = true },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.width(2.dp))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onClickNew() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            val showEmpty = filteredAlphaSorted.isEmpty() && recentEntries.isEmpty()
            if (showEmpty) {
                var emptyHelpVisible by remember { mutableStateOf(false) }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = "( ´ཀ` )",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { emptyHelpVisible = !emptyHelpVisible },
                    )
                    AnimatedVisibility(
                        visible = emptyHelpVisible,
                        enter = fadeIn(tween(500)),
                        exit = fadeOut(tween(100)),
                    ) {
                        Text(
                            text = stringResource(Res.string.document_bottom_sheet_picker_empty_product),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 24.dp),
                    contentPadding = PaddingValues(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    val showRecent = query.text.isBlank() && recentEntries.isNotEmpty()
                    if (showRecent) {
                        stickyHeader(key = "header-recent") {
                            RecentProductsHeader(stringResource(Res.string.document_bottom_sheet_picker_recent))
                        }
                        items(recentEntries, key = { "recent-${it.id}" }) { product ->
                            ProductPickerRow(
                                product = product,
                                clientId = clientId,
                                currencyCode = currencyCode,
                                onClick = { onSelect(product) },
                            )
                        }
                    }
                    val showAlphabetHeaders = nonNullProducts.size >= 10
                    grouped.forEach { (letter, entries) ->
                        if (showAlphabetHeaders) {
                            stickyHeader(key = "header-$letter") {
                                Text(
                                    text = letter,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.Gray,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White)
                                        .padding(top = 4.dp, bottom = 4.dp, start = 12.dp),
                                )
                            }
                        }
                        items(entries, key = { "alpha-${it.id}" }) { product ->
                            ProductPickerRow(
                                product = product,
                                clientId = clientId,
                                currencyCode = currencyCode,
                                onClick = { onSelect(product) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentProductsHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = Color.Gray,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(top = 12.dp, bottom = 4.dp, start = 12.dp),
    )
}

@Composable
private fun ProductPickerRow(
    product: ProductState,
    clientId: Int?,
    currencyCode: String,
    onClick: () -> Unit,
) {
    val price = if (clientId != null) {
        product.additionalPrices?.firstOrNull { it.clients.any { c -> c.id == clientId } }
            ?.priceWithTax
            ?: product.defaultPriceWithTax
    } else {
        product.defaultPriceWithTax
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.name.text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            product.description?.text?.takeIf { it.isNotBlank() }?.let { desc ->
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Text(
            text = price?.let { formatAmount(it, currencyCode) }.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
