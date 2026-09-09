package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.theme.textSection
import com.a4a.g8invoicing.data.AppLocaleHolder
import com.a4a.g8invoicing.data.ProductLocalDataSourceInterface
import com.a4a.g8invoicing.data.models.UnitCode
import com.a4a.g8invoicing.data.models.UnitCodeRepository
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.unit_picker_category_area
import com.a4a.g8invoicing.shared.resources.unit_picker_category_count
import com.a4a.g8invoicing.shared.resources.unit_picker_category_energy
import com.a4a.g8invoicing.shared.resources.unit_picker_category_length
import com.a4a.g8invoicing.shared.resources.unit_picker_category_packaging
import com.a4a.g8invoicing.shared.resources.unit_picker_category_service
import com.a4a.g8invoicing.shared.resources.unit_picker_category_time
import com.a4a.g8invoicing.shared.resources.unit_picker_category_volume
import com.a4a.g8invoicing.shared.resources.unit_picker_category_weight
import com.a4a.g8invoicing.shared.resources.unit_picker_empty
import com.a4a.g8invoicing.shared.resources.unit_picker_recent
import com.a4a.g8invoicing.shared.resources.unit_picker_search
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/**
 * Modal bottom sheet listing UNECE unit codes accepted by Factur-X. Search
 * queries go through [UnitCodeRepository.search] so localised keywords (from
 * strings.xml) and diacritic-insensitive matching are honoured.
 *
 * Last-5 unit codes used at product creation surface at the top as "Récentes"
 * (fetched via Koin from ProductLocalDataSourceInterface). Below, entries are
 * grouped by category (comptage, longueur, surface, volume, poids, temps,
 * énergie, service, emballage) when the search is empty; a non-empty query
 * collapses everything into a single ranked list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitCodePicker(
    currentCode: String?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val productDataSource: ProductLocalDataSourceInterface = koinInject()
    val unitCodeRepository: UnitCodeRepository = koinInject()

    // Nullable while the fetch is in flight so the list waits for the Récentes
    // section before rendering. Otherwise the LazyColumn paints the "Comptage"
    // category at index 0, then the Récentes header inserts at index 0 later
    // and the sheet stays scrolled past it.
    var recentCodes by remember { mutableStateOf<List<String>?>(null) }
    LaunchedEffect(Unit) {
        recentCodes = productDataSource.fetchLast5UnitCodes()
    }

    var query by remember { mutableStateOf(TextFieldValue("")) }
    val trimmedQuery = query.text.trim()
    val hasQuery = trimmedQuery.isNotEmpty()

    // Rebuild both the display-name map and search results whenever the query
    // OR the locale change. The locale key covers the (rare) case where the
    // user switches app language while the sheet is open.
    val locale = AppLocaleHolder.languageCode
    var namesByCode by remember { mutableStateOf<Map<UnitCode, String>>(emptyMap()) }
    LaunchedEffect(locale) {
        namesByCode = UnitCode.entries.associateWith { unitCodeRepository.resolveName(it) }
    }
    var results by remember { mutableStateOf<List<UnitCode>>(emptyList()) }
    LaunchedEffect(trimmedQuery, locale) {
        results = unitCodeRepository.search(trimmedQuery)
    }

    val recent: List<UnitCode> = remember(recentCodes) {
        recentCodes?.mapNotNull { UnitCode.findByCode(it) }?.take(3) ?: emptyList()
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets(0) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.85f)
                .imePadding()
                .padding(horizontal = 24.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                placeholder = { Text(stringResource(Res.string.unit_picker_search)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Black,
                    cursorColor = Color.Black,
                ),
            )

            if (recentCodes == null) {
                // Fetch still in flight — render nothing so the LazyColumn's
                // initial scroll position matches the final structure.
            } else if (results.isEmpty()) {
                Text(
                    text = stringResource(Res.string.unit_picker_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    if (!hasQuery && recent.isNotEmpty()) {
                        item("header-recent") {
                            SectionHeader(stringResource(Res.string.unit_picker_recent))
                        }
                        items(recent, key = { "recent-${it.code}" }) { entry ->
                            UnitRow(
                                entry = entry,
                                label = namesByCode[entry] ?: entry.code,
                                isCurrent = entry.code == currentCode,
                                onClick = { onSelect(entry.code) },
                            )
                        }
                    }
                    if (hasQuery) {
                        items(results, key = { "flat-${it.code}" }) { entry ->
                            UnitRow(
                                entry = entry,
                                label = namesByCode[entry] ?: entry.code,
                                isCurrent = entry.code == currentCode,
                                onClick = { onSelect(entry.code) },
                            )
                        }
                    } else {
                        UnitCode.Category.entries.forEach { category ->
                            val codes = results.filter { it.category == category }
                            if (codes.isEmpty()) return@forEach
                            item("header-$category") { SectionHeader(category.label()) }
                            items(codes, key = { "cat-${it.code}" }) { entry ->
                                UnitRow(
                                    entry = entry,
                                    label = namesByCode[entry] ?: entry.code,
                                    isCurrent = entry.code == currentCode,
                                    onClick = { onSelect(entry.code) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(label: String) {
    Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.textSection,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun UnitRow(
    entry: UnitCode,
    label: String,
    isCurrent: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "$label — ${entry.code}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun UnitCode.Category.label(): String = when (this) {
    UnitCode.Category.COUNT -> stringResource(Res.string.unit_picker_category_count)
    UnitCode.Category.LENGTH -> stringResource(Res.string.unit_picker_category_length)
    UnitCode.Category.AREA -> stringResource(Res.string.unit_picker_category_area)
    UnitCode.Category.VOLUME -> stringResource(Res.string.unit_picker_category_volume)
    UnitCode.Category.WEIGHT -> stringResource(Res.string.unit_picker_category_weight)
    UnitCode.Category.TIME -> stringResource(Res.string.unit_picker_category_time)
    UnitCode.Category.ENERGY -> stringResource(Res.string.unit_picker_category_energy)
    UnitCode.Category.SERVICE -> stringResource(Res.string.unit_picker_category_service)
    UnitCode.Category.PACKAGING -> stringResource(Res.string.unit_picker_category_packaging)
}
