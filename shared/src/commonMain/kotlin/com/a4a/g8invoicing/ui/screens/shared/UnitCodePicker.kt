package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.a4a.g8invoicing.data.ProductLocalDataSourceInterface
import com.a4a.g8invoicing.data.models.UnitCodes
import org.koin.compose.koinInject
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

/**
 * Modal bottom sheet listing UNECE unit codes accepted by Factur-X. Search field at top
 * filters both code and French label. Last-5 codes used in Product creation surface at
 * the top as a "Récentes" section (auto-fetched via Koin from
 * `ProductLocalDataSourceInterface.fetchLast5UnitCodes()` — no plumbing needed by
 * callers). Below, entries are grouped by category (comptage, longueur, surface,
 * volume, poids, temps, énergie, service, emballage).
 *
 * When the user types a query, categories collapse into a single flat filtered list.
 * Selecting an entry calls [onSelect] with the UNECE code; the caller handles closing.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitCodePicker(
    currentCode: String?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val dataSource: ProductLocalDataSourceInterface = koinInject()
    var recentCodes by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(Unit) {
        recentCodes = dataSource.fetchLast5UnitCodes()
    }

    var query by remember { mutableStateOf(TextFieldValue("")) }

    val trimmedQuery = query.text.trim()
    val hasQuery = trimmedQuery.isNotEmpty()

    val filtered: List<UnitCodes.UnitCode> = remember(trimmedQuery) {
        if (!hasQuery) UnitCodes.ALL
        else UnitCodes.ALL.filter {
            it.code.contains(trimmedQuery, ignoreCase = true) ||
                it.labelFr.contains(trimmedQuery, ignoreCase = true) ||
                it.labelEn.contains(trimmedQuery, ignoreCase = true)
        }
    }

    val recent: List<UnitCodes.UnitCode> = remember(recentCodes) {
        recentCodes.mapNotNull { UnitCodes.findByCode(it) }
    }

    // Open fully expanded so the top of the list ("Récentes" header + recent codes)
    // is visible without the user having to drag the sheet up first.
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp)
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

            if (filtered.isEmpty()) {
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
                            UnitRow(entry = entry, isCurrent = entry.code == currentCode, onClick = { onSelect(entry.code) })
                        }
                    }
                    if (hasQuery) {
                        items(filtered, key = { "flat-${it.code}" }) { entry ->
                            UnitRow(entry = entry, isCurrent = entry.code == currentCode, onClick = { onSelect(entry.code) })
                        }
                    } else {
                        UnitCodes.Category.entries.forEach { category ->
                            val codes = filtered.filter { it.category == category }
                            if (codes.isEmpty()) return@forEach
                            item("header-$category") { SectionHeader(category.labelFr()) }
                            items(codes, key = { "cat-${it.code}" }) { entry ->
                                UnitRow(entry = entry, isCurrent = entry.code == currentCode, onClick = { onSelect(entry.code) })
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
        text = label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun UnitRow(
    entry: UnitCodes.UnitCode,
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
            text = "${entry.labelFr} — ${entry.code}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}

@Composable
private fun UnitCodes.Category.labelFr(): String = when (this) {
    UnitCodes.Category.COUNT -> stringResource(Res.string.unit_picker_category_count)
    UnitCodes.Category.LENGTH -> stringResource(Res.string.unit_picker_category_length)
    UnitCodes.Category.AREA -> stringResource(Res.string.unit_picker_category_area)
    UnitCodes.Category.VOLUME -> stringResource(Res.string.unit_picker_category_volume)
    UnitCodes.Category.WEIGHT -> stringResource(Res.string.unit_picker_category_weight)
    UnitCodes.Category.TIME -> stringResource(Res.string.unit_picker_category_time)
    UnitCodes.Category.ENERGY -> stringResource(Res.string.unit_picker_category_energy)
    UnitCodes.Category.SERVICE -> stringResource(Res.string.unit_picker_category_service)
    UnitCodes.Category.PACKAGING -> stringResource(Res.string.unit_picker_category_packaging)
}
