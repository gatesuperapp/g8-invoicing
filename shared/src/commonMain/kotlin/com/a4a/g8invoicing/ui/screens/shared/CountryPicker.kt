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
import androidx.compose.runtime.Composable
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
import com.a4a.g8invoicing.data.models.CountryCodes
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.country_picker_empty
import com.a4a.g8invoicing.shared.resources.country_picker_search
import org.jetbrains.compose.resources.stringResource

/**
 * Modal bottom sheet listing the ISO country codes we support (see [CountryCodes.ALL]).
 * Search field at top filters both code and French display name. Selecting an entry
 * calls [onSelect] with the ISO 3166-1 alpha-2 code (already uppercased); the caller
 * is responsible for closing the sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryPicker(
    currentCode: String?,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val allEntries: List<CountryEntry> = remember {
        // Flat alphabetical list by French display name — g8 users distribute worldwide
        // so no upfront bias toward a curated shortlist. Search filter below covers both
        // ISO code and name.
        CountryCodes.ALL.entries
            .map { CountryEntry(code = it.key, name = it.value) }
            .sortedBy { it.name }
    }

    var query by remember { mutableStateOf(TextFieldValue("")) }

    val filtered = remember(query.text, allEntries) {
        val q = query.text.trim()
        if (q.isEmpty()) allEntries
        else allEntries.filter {
            it.code.contains(q, ignoreCase = true) || it.name.contains(q, ignoreCase = true)
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
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
                    items(filtered, key = { it.code }) { entry ->
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
