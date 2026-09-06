package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.data.ClientOrIssuerLocalDataSourceInterface
import com.a4a.g8invoicing.data.models.CountryCodes
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.country_picker_all
import com.a4a.g8invoicing.shared.resources.country_picker_empty
import com.a4a.g8invoicing.shared.resources.country_picker_recent
import com.a4a.g8invoicing.shared.resources.country_picker_search
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

/**
 * Modal bottom sheet listing the ISO country codes we support (see [CountryCodes.ALL]).
 * Search field at top filters both code and French display name. A "Récents" section
 * at the top surfaces the last few codes used across any address, so returning users
 * skip the alphabetical scroll. Selecting an entry calls [onSelect] with the ISO
 * 3166-1 alpha-2 code (already uppercased); the caller is responsible for closing
 * the sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryPicker(
    currentCode: String?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    dataSource: ClientOrIssuerLocalDataSourceInterface = koinInject(),
) {
    // Recompute when the app language changes so the picker re-renders in
    // the new language. Was reading the frozen FR names from CountryCodes.ALL
    // directly — the display now delegates to CountryCodes.displayNameOf which
    // hits the platform's localised country-name table.
    val appLanguage = com.a4a.g8invoicing.data.AppLocaleHolder.languageCode
    val allEntries: List<CountryEntry> = remember(appLanguage) {
        CountryCodes.ALL.keys
            .map { code -> CountryEntry(code = code, name = CountryCodes.displayNameOf(code)) }
            .sortedBy { it.name.lowercase() }
    }

    var recentCodes by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(Unit) {
        recentCodes = dataSource.getRecentCountryCodes(3)
    }
    val recentEntries = remember(recentCodes, allEntries) {
        recentCodes.mapNotNull { code -> allEntries.firstOrNull { it.code == code } }
    }

    var query by remember { mutableStateOf(TextFieldValue("")) }

    val filtered = remember(query.text, allEntries) {
        val q = query.text.trim()
        if (q.isEmpty()) allEntries
        else allEntries.filter {
            it.code.contains(q, ignoreCase = true) || it.name.contains(q, ignoreCase = true)
        }
    }

    // Full-expand the sheet whenever the OS keyboard becomes visible so the
    // search TextField sits above it on any starting sheet state. Mirrors the
    // CurrencyPicker: search bar is always visible so we tie expand to actual
    // IME visibility rather than to a searchExpanded state.
    val density = LocalDensity.current
    val imeVisible = WindowInsets.ime.getBottom(density) > 0
    val sheetState = rememberModalBottomSheetState()
    LaunchedEffect(imeVisible) {
        if (imeVisible) sheetState.expand()
    }

    // contentWindowInsets = { WindowInsets(0) } + .imePadding() on the Column:
    // safe combo with Material3 ≥ 1.5.0-alpha19 that keeps the TextField above
    // the keyboard once the sheet is fully expanded.
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets(0) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.85f)
                .imePadding()
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                placeholder = { Text(stringResource(Res.string.country_picker_search)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Black,
                    cursorColor = Color.Black,
                ),
            )

            if (filtered.isEmpty()) {
                Text(
                    text = stringResource(Res.string.country_picker_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    val showRecent = query.text.isBlank() && recentEntries.isNotEmpty()
                    if (showRecent) {
                        item { SectionHeader(stringResource(Res.string.country_picker_recent)) }
                        items(recentEntries, key = { "recent-" + it.code }) { entry ->
                            CountryRow(
                                entry = entry,
                                isCurrent = entry.code == currentCode,
                                onClick = { onSelect(entry.code) },
                            )
                        }
                        item { SectionHeader(stringResource(Res.string.country_picker_all)) }
                    }
                    items(filtered, key = { "all-" + it.code }) { entry ->
                        CountryRow(
                            entry = entry,
                            isCurrent = entry.code == currentCode,
                            onClick = { onSelect(entry.code) },
                        )
                    }
                }
            }
        }
    }
}

private data class CountryEntry(val code: String, val name: String)

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun CountryRow(
    entry: CountryEntry,
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
            text = "${entry.code} — ${entry.name}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}
