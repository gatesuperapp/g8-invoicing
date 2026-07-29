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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.data.AppLanguage
import com.a4a.g8invoicing.data.LocaleManager
import com.a4a.g8invoicing.data.availableCurrencyCodes
import com.a4a.g8invoicing.data.currencyDisplayName
import com.a4a.g8invoicing.data.currencySymbol
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.currency_picker_all
import com.a4a.g8invoicing.shared.resources.currency_picker_empty
import com.a4a.g8invoicing.shared.resources.currency_picker_recent
import com.a4a.g8invoicing.shared.resources.currency_picker_search
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyPicker(
    currentCode: String,
    recentCodes: List<String>,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
    localeManager: LocaleManager = koinInject(),
) {
    val uiLanguageCode = when (localeManager.currentLanguage) {
        AppLanguage.SYSTEM -> null
        else -> localeManager.currentLanguage.code
    }

    var entries by remember { mutableStateOf<List<CurrencyEntry>>(emptyList()) }
    LaunchedEffect(uiLanguageCode) {
        // First open does a one-time scan of all JVM locales to resolve native
        // currency symbols. Run it off the main thread so the sheet doesn't
        // jank while populating; subsequent opens hit the warmed cache and are
        // instant.
        val built = withContext(Dispatchers.Default) {
            availableCurrencyCodes().map { code ->
                CurrencyEntry(
                    code = code,
                    name = currencyDisplayName(code, uiLanguageCode),
                    symbol = currencySymbol(code),
                )
            }.sortedBy { it.code }
        }
        entries = built
    }

    var query by remember { mutableStateOf(TextFieldValue("")) }

    val filtered = remember(query.text, entries) {
        val q = query.text.trim()
        if (q.isEmpty()) entries
        else entries.filter {
            it.code.contains(q, ignoreCase = true) ||
                it.name.contains(q, ignoreCase = true) ||
                it.symbol.contains(q, ignoreCase = true)
        }
    }

    val recentEntries = remember(recentCodes, entries) {
        recentCodes.mapNotNull { code -> entries.firstOrNull { it.code == code } }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                placeholder = { Text(stringResource(Res.string.currency_picker_search)) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Black,
                    cursorColor = Color.Black,
                ),
            )

            if (filtered.isEmpty()) {
                Text(
                    text = stringResource(Res.string.currency_picker_empty),
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
                        item {
                            SectionHeader(stringResource(Res.string.currency_picker_recent))
                        }
                        items(recentEntries, key = { "recent-" + it.code }) { entry ->
                            CurrencyRow(
                                entry = entry,
                                isCurrent = entry.code == currentCode,
                                onClick = { onSelect(entry.code) },
                            )
                        }
                        item {
                            SectionHeader(stringResource(Res.string.currency_picker_all))
                        }
                    }
                    items(filtered, key = { "all-" + it.code }) { entry ->
                        CurrencyRow(
                            entry = entry,
                            isCurrent = entry.code == currentCode,
                            onClick = { onSelect(entry.code) },
                        )
                        // Solidarity message pinned just under ILS: the Palestinian
                        // territories don't issue their own currency and Palestinians
                        // use ILS by force of occupation. Non-clickable, no i18n key
                        // (statement, not UI copy).
                        if (entry.code == "ILS") {
                            FreePalestineRow()
                        }
                    }
                }
            }
        }
    }
}

private data class CurrencyEntry(
    val code: String,
    val name: String,
    val symbol: String,
)

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun FreePalestineRow() {
    // Palestinian flag colours cycled through a repeating linear gradient. The
    // gradient's origin is animated so the colours appear to glide across the
    // text. The watermelon is kept in its native colour by rendering it as a
    // separate Text — applying the brush over the emoji would flatten its
    // colour glyph to a single-colour silhouette.
    val flagGreen = Color(0xFF009639)
    val flagRed = Color(0xFFEE2A35)
    val flagBlack = Color(0xFF000000)
    val cycleWidth = 220f
    val transition = rememberInfiniteTransition(label = "palestine")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = cycleWidth,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "palestine-shift",
    )
    val brush = Brush.linearGradient(
        colors = listOf(flagGreen, flagRed, flagBlack, flagGreen),
        start = Offset(shift, 0f),
        end = Offset(shift + cycleWidth, 0f),
        tileMode = TileMode.Repeated,
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "🍉 ", style = MaterialTheme.typography.bodyMedium)
        Text(
            text = "Free Palestine",
            style = MaterialTheme.typography.bodyMedium.copy(
                brush = brush,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

@Composable
private fun CurrencyRow(
    entry: CurrencyEntry,
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
            text = "${entry.code} — ${entry.name} (${entry.symbol})",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}
