package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.data.ClientOrIssuerLocalDataSourceInterface
import com.a4a.g8invoicing.data.CreditNoteLocalDataSourceInterface
import com.a4a.g8invoicing.data.DeliveryNoteLocalDataSourceInterface
import com.a4a.g8invoicing.data.InvoiceLocalDataSourceInterface
import com.a4a.g8invoicing.data.OrphanDocType
import com.a4a.g8invoicing.data.QuoteLocalDataSourceInterface
import com.a4a.g8invoicing.data.models.PersonType
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.orphan_rescue_card_assign_to
import com.a4a.g8invoicing.shared.resources.orphan_rescue_card_client
import com.a4a.g8invoicing.shared.resources.orphan_rescue_card_cta
import com.a4a.g8invoicing.shared.resources.orphan_rescue_card_products
import com.a4a.g8invoicing.shared.resources.orphan_rescue_doc_type_credit_note
import com.a4a.g8invoicing.shared.resources.orphan_rescue_doc_type_delivery_note
import com.a4a.g8invoicing.shared.resources.orphan_rescue_doc_type_invoice
import com.a4a.g8invoicing.shared.resources.orphan_rescue_doc_type_quote
import com.a4a.g8invoicing.shared.resources.orphan_rescue_message
import com.a4a.g8invoicing.shared.resources.orphan_rescue_title
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.states.DocumentState
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.stringResource

/**
 * Self-contained preview payload the dialog needs to render a single
 * orphan doc card. Built on the caller side from the full DocumentState
 * fetched via the doc-type-specific data source, so the dialog stays
 * decoupled from doc-type internals.
 */
data class OrphanRescueItem(
    val type: OrphanDocType,
    val id: Long,
    val number: String,        // "F001"
    val date: String,          // "05/09/2026"
    val clientLine: String,    // "Léa Martin — 12 rue des Fleurs, 75001 Paris"
    val productLines: List<String>,  // ["Consultation × 2h — 240 €", ...]
)

/**
 * Launch-time rescue for docs whose original_company_id points at a
 * ClientOrIssuer that no longer exists (1.9 migration bug: a company
 * deleted in the cleanup wizard used to leave its docs unreachable).
 * Non-dismissable while items remain so users can't accidentally close
 * and forget; auto-closes via [onAllResolved] once every card has been
 * assigned to a target company.
 */
@Composable
fun OrphanRescueDialog(
    initialItems: List<OrphanRescueItem>,
    candidates: List<ClientOrIssuerState>,
    onAssign: suspend (item: OrphanRescueItem, companyId: Long) -> Unit,
    onAllResolved: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var remaining by remember { mutableStateOf(initialItems) }
    if (remaining.isEmpty()) {
        onAllResolved()
        return
    }
    // Single-issuer shortcut: no per-doc picker (only one target
    // possible), single "Rattacher" CTA at the bottom batches every
    // orphan onto that issuer in one go.
    val singleCandidateId = candidates.singleOrNull()?.id?.toLong()
    AlertDialog(
        onDismissRequest = { /* Non-dismissable — see class KDoc. */ },
        title = { Text(stringResource(Res.string.orphan_rescue_title)) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = stringResource(Res.string.orphan_rescue_message, remaining.size),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(16.dp))
                remaining.forEach { item ->
                    OrphanCard(
                        item = item,
                        candidates = candidates,
                        showPicker = singleCandidateId == null,
                        onAssign = { companyId ->
                            // Optimistic drop: card disappears immediately, DB
                            // write runs in the background. If the update
                            // fails the next boot's orphan probe re-adds the
                            // doc — self-healing.
                            remaining = remaining.filterNot { it.id == item.id && it.type == item.type }
                            scope.launch { onAssign(item, companyId) }
                        },
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        },
        confirmButton = {
            if (singleCandidateId != null) {
                Button(
                    onClick = {
                        val toAssign = remaining
                        remaining = emptyList()
                        scope.launch {
                            toAssign.forEach { onAssign(it, singleCandidateId) }
                        }
                    },
                ) {
                    Text(stringResource(Res.string.orphan_rescue_card_cta))
                }
            } else {
                // Empty slot for the multi-issuer branch — per-card CTAs
                // carry the actions. Material3 AlertDialog still needs the
                // slot present.
                Box(Modifier.width(0.dp).height(0.dp))
            }
        },
    )
}

@Composable
private fun OrphanCard(
    item: OrphanRescueItem,
    candidates: List<ClientOrIssuerState>,
    // False = single-issuer flow: card shows preview only, batching runs
    // from the dialog-level CTA instead of per-card.
    showPicker: Boolean,
    onAssign: (companyId: Long) -> Unit,
) {
    var selectedId by remember(item.id, item.type) {
        mutableStateOf(candidates.firstOrNull()?.id?.toLong())
    }
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F2F8))
            .padding(12.dp),
    ) {
        val typeLabel = when (item.type) {
            OrphanDocType.INVOICE -> stringResource(Res.string.orphan_rescue_doc_type_invoice)
            OrphanDocType.DELIVERY_NOTE -> stringResource(Res.string.orphan_rescue_doc_type_delivery_note)
            OrphanDocType.CREDIT_NOTE -> stringResource(Res.string.orphan_rescue_doc_type_credit_note)
            OrphanDocType.QUOTE -> stringResource(Res.string.orphan_rescue_doc_type_quote)
        }
        Text(
            text = "$typeLabel ${item.number} — ${item.date}",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(Res.string.orphan_rescue_card_client) + " " + item.clientLine,
            style = MaterialTheme.typography.bodySmall,
        )
        if (item.productLines.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(Res.string.orphan_rescue_card_products),
                style = MaterialTheme.typography.bodySmall,
            )
            item.productLines.forEach { line ->
                Text(
                    text = "  • $line",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        if (!showPicker) return@Column
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.orphan_rescue_card_assign_to),
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(2.dp))
                val selectedName = candidates.firstOrNull { it.id?.toLong() == selectedId }
                    ?.name?.text?.ifBlank { "—" } ?: "—"
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White)
                        .clickable { expanded = true }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(selectedName, style = MaterialTheme.typography.bodyMedium)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    candidates.forEach { c ->
                        val id = c.id?.toLong() ?: return@forEach
                        DropdownMenuItem(
                            text = { Text(c.name.text.ifBlank { "—" }) },
                            onClick = { selectedId = id; expanded = false },
                        )
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = { selectedId?.let(onAssign) },
                enabled = selectedId != null,
            ) {
                Text(stringResource(Res.string.orphan_rescue_card_cta))
            }
        }
    }
}

/**
 * Aggregated payload returned by [probeOrphanRescue] when the orphan
 * probe finds work to do. Null return = nothing to rescue (either no
 * orphans, no issuer to reassign to, or DB error).
 */
data class OrphanRescuePayload(
    val items: List<OrphanRescueItem>,
    val candidates: List<ClientOrIssuerState>,
)

/**
 * Boot-time / post-wizard orphan probe. Runs the cheap count query
 * first; only fetches full previews if there's actually something to
 * rescue AND at least one issuer to reassign to. Both platform entry
 * points (App.kt shared, MainCompose.kt Android) call this so the
 * detection logic stays in a single place.
 */
suspend fun probeOrphanRescue(
    clientOrIssuerDataSource: ClientOrIssuerLocalDataSourceInterface,
    invoiceDataSource: InvoiceLocalDataSourceInterface,
    deliveryNoteDataSource: DeliveryNoteLocalDataSourceInterface,
    creditNoteDataSource: CreditNoteLocalDataSourceInterface,
    quoteDataSource: QuoteLocalDataSourceInterface,
): OrphanRescuePayload? {
    val count = clientOrIssuerDataSource.countOrphanDocs()
    if (count <= 0L) return null
    val issuers = clientOrIssuerDataSource.fetchAll(PersonType.ISSUER).first()
    if (issuers.isEmpty()) return null
    val ids = clientOrIssuerDataSource.getOrphanDocIds()
    val previews = mutableListOf<OrphanRescueItem>()
    ids.invoice.forEach { id ->
        invoiceDataSource.fetch(id)?.let { previews.add(it.toRescueItem(OrphanDocType.INVOICE, id)) }
    }
    ids.deliveryNote.forEach { id ->
        deliveryNoteDataSource.fetch(id)?.let { previews.add(it.toRescueItem(OrphanDocType.DELIVERY_NOTE, id)) }
    }
    ids.creditNote.forEach { id ->
        creditNoteDataSource.fetch(id)?.let { previews.add(it.toRescueItem(OrphanDocType.CREDIT_NOTE, id)) }
    }
    ids.quote.forEach { id ->
        quoteDataSource.fetch(id)?.let { previews.add(it.toRescueItem(OrphanDocType.QUOTE, id)) }
    }
    if (previews.isEmpty()) return null
    return OrphanRescuePayload(items = previews, candidates = issuers)
}

// Text preview extracted from a doc's frozen client + products snapshots.
// Minimal on purpose: name + first address + product name/quantity, one
// line each. Enough for the user to recognise the doc in the rescue
// dialog without opening a full PDF viewer.
internal fun DocumentState.toRescueItem(type: OrphanDocType, id: Long): OrphanRescueItem {
    val client = documentClient
    val clientName = buildString {
        val fn = client?.firstName?.text?.trim().orEmpty()
        val n = client?.name?.text?.trim().orEmpty()
        if (fn.isNotEmpty()) append(fn)
        if (fn.isNotEmpty() && n.isNotEmpty()) append(' ')
        if (n.isNotEmpty()) append(n)
    }.ifEmpty { "—" }
    val addr = client?.addresses?.firstOrNull()
    val addressPart = listOfNotNull(
        addr?.addressLine1?.text?.trim()?.ifEmpty { null },
        listOfNotNull(
            addr?.zipCode?.text?.trim()?.ifEmpty { null },
            addr?.city?.text?.trim()?.ifEmpty { null },
        ).joinToString(" ").ifEmpty { null },
    ).joinToString(", ")
    val clientLine = if (addressPart.isEmpty()) clientName else "$clientName — $addressPart"
    val productLines = documentProducts.orEmpty().map { p ->
        val name = p.name.text.trim().ifEmpty { "—" }
        val qty = p.quantity.toPlainString()
        "$name × $qty"
    }
    return OrphanRescueItem(
        type = type,
        id = id,
        number = documentNumber.text,
        date = documentDate,
        clientLine = clientLine,
        productLines = productLines,
    )
}
