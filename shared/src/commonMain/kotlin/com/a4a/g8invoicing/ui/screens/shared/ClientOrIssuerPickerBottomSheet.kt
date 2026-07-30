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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.data.ClientOrIssuerLocalDataSourceInterface
import com.a4a.g8invoicing.data.models.PersonType
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.currency_picker_search
import com.a4a.g8invoicing.shared.resources.document_bottom_sheet_picker_empty_client
import com.a4a.g8invoicing.shared.resources.document_bottom_sheet_picker_empty_issuer
import com.a4a.g8invoicing.shared.resources.document_bottom_sheet_picker_edit_link
import com.a4a.g8invoicing.shared.resources.document_bottom_sheet_picker_recent
import com.a4a.g8invoicing.shared.resources.document_bottom_sheet_picker_title_client
import com.a4a.g8invoicing.shared.resources.document_bottom_sheet_picker_title_issuer
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.theme.AppColors
import com.a4a.g8invoicing.ui.theme.ColorVioletLight
import com.a4a.g8invoicing.ui.theme.textBodySmall
import com.a4a.g8invoicing.ui.theme.textCaption
import com.a4a.g8invoicing.ui.theme.textScreenTitle
import com.a4a.g8invoicing.ui.theme.textSection
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

private val RowGrey = Color(0xFFF0F0F0)
private val SelectedViolet = ColorVioletLight.copy(alpha = 0.08f)
private val SelectedVioletText = ColorVioletLight

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ClientOrIssuerPickerBottomSheet(
    pageElement: ScreenElement,
    list: List<ClientOrIssuerState>,
    currentSelected: ClientOrIssuerState?,
    hasMasterUpdate: Boolean,
    onSelect: (ClientOrIssuerState) -> Unit,
    onClickEdit: (ClientOrIssuerState) -> Unit,
    onClickDeselect: () -> Unit,
    onClickRefreshFromMaster: (ClientOrIssuerState) -> Unit,
    onClickNew: () -> Unit,
    onDismiss: () -> Unit,
) {
    val isIssuer = pageElement == ScreenElement.DOCUMENT_ISSUER
    val showSearch = !isIssuer
    var query by remember { mutableStateOf(TextFieldValue("")) }
    var searchExpanded by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val sheetState = rememberModalBottomSheetState()

    // Full-expand the sheet on search open, then request focus. Simpler and
    // more predictable than trying to keep the sheet at partial when the user
    // wants to browse search results. Safe with Material3 ≥ 1.5.0-alpha19 —
    // no more IME animation race against the sheet's internal imePadding.
    LaunchedEffect(searchExpanded) {
        if (searchExpanded) {
            sheetState.expand()
            focusRequester.requestFocus()
        }
    }

    // Only clients get a "Récents" section, and only when the list is long enough
    // that jumping to a recent entry actually saves scrolling.
    val showRecentsSection = !isIssuer && list.size > 5

    val dataSource: ClientOrIssuerLocalDataSourceInterface = koinInject()
    var recentIds by remember { mutableStateOf<List<Long>>(emptyList()) }
    LaunchedEffect(showRecentsSection) {
        recentIds = if (showRecentsSection) {
            dataSource.fetchLast3RecentClientOrIssuerIds(PersonType.CLIENT)
        } else emptyList()
    }

    val selectedMasterId = currentSelected?.originalClientOrIssuerId ?: currentSelected?.id

    val recentEntries = remember(recentIds, list, selectedMasterId) {
        recentIds.mapNotNull { rid ->
            list.firstOrNull { it.id?.toLong() == rid }
        }.filter { it.id != selectedMasterId }
    }

    val filteredAlphaSorted = remember(query.text, list, selectedMasterId) {
        val q = query.text.trim()
        val filtered = if (q.isEmpty()) list
        else list.filter {
            it.name.text.contains(q, ignoreCase = true) ||
                (it.firstName?.text?.contains(q, ignoreCase = true) == true) ||
                (it.emails?.any { e -> e.email.text.contains(q, ignoreCase = true) } == true)
        }
        val withoutSelected = if (selectedMasterId == null) filtered
        else filtered.filter { it.id != selectedMasterId }
        withoutSelected.sortedBy { it.name.text.trim().lowercase() }
    }

    val grouped = remember(filteredAlphaSorted) {
        filteredAlphaSorted.groupBy { it.name.text.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "#" }
    }

    // Zero out contentWindowInsets on the sheet + apply .imePadding() on the
    // inner Column. With Material3 ≥ 1.5.0-alpha19 this combo drives the IME
    // handling cleanly: the sheet is fully expanded (see LaunchedEffect above)
    // and .imePadding() below pushes the TextField above the keyboard without
    // any anchor animation race.
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
            if (currentSelected != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                ) {
                    ClientOrIssuerPickerRow(
                        entry = currentSelected,
                        isSelected = true,
                        hasMasterUpdate = hasMasterUpdate,
                        onClick = { onClickEdit(currentSelected) },
                        onClickDelete = { onClickDeselect() },
                        onClickRefreshFromMaster = { onClickRefreshFromMaster(currentSelected) },
                    )
                }
                return@Column
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 24.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AnimatedContent(
                    targetState = showSearch && searchExpanded,
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
                            textStyle = MaterialTheme.typography.textBodySmall,
                            placeholder = {
                                Text(
                                    text = stringResource(Res.string.currency_picker_search),
                                    style = MaterialTheme.typography.textBodySmall.copy(color = Color.Gray),
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
                                        tint = AppColors.iconSecondary,
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
                                text = stringResource(
                                    if (isIssuer) Res.string.document_bottom_sheet_picker_title_issuer
                                    else Res.string.document_bottom_sheet_picker_title_client
                                ),
                                style = MaterialTheme.typography.textScreenTitle,
                                modifier = Modifier.padding(start = 12.dp),
                            )
                            Spacer(Modifier.weight(1f))
                            if (showSearch) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clickable { searchExpanded = true },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Search,
                                        contentDescription = null,
                                        tint = AppColors.iconPrimary,
                                        modifier = Modifier.size(22.dp),
                                    )
                                }
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
                        tint = AppColors.iconPrimary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            val showEmpty = filteredAlphaSorted.isEmpty() && currentSelected == null
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
                            text = stringResource(
                                if (isIssuer) Res.string.document_bottom_sheet_picker_empty_issuer
                                else Res.string.document_bottom_sheet_picker_empty_client
                            ),
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
                            RecentlyChosenHeader(stringResource(Res.string.document_bottom_sheet_picker_recent))
                        }
                        items(recentEntries, key = { "recent-${it.id ?: it.hashCode()}" }) { entry ->
                            ClientOrIssuerPickerRow(
                                entry = entry,
                                isSelected = false,
                                hasMasterUpdate = false,
                                onClick = { onSelect(entry) },
                                onClickDelete = {},
                                onClickRefreshFromMaster = {},
                            )
                        }
                    }
                    val showAlphabetHeaders = !isIssuer && list.size >= 10
                    grouped.forEach { (letter, entries) ->
                        if (showAlphabetHeaders) {
                            stickyHeader(key = "header-$letter") {
                                Text(
                                    text = letter,
                                    style = MaterialTheme.typography.textCaption.copy(fontWeight = FontWeight.SemiBold),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(AppColors.surface)
                                        .padding(top = 4.dp, bottom = 4.dp, start = 12.dp),
                                )
                            }
                        }
                        items(entries, key = { it.id ?: it.hashCode() }) { entry ->
                            ClientOrIssuerPickerRow(
                                entry = entry,
                                isSelected = false,
                                hasMasterUpdate = false,
                                onClick = { onSelect(entry) },
                                onClickDelete = {},
                                onClickRefreshFromMaster = {},
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentlyChosenHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.textSection,
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColors.surface)
            .padding(top = 12.dp, bottom = 4.dp, start = 12.dp),
    )
}

@Composable
private fun ClientOrIssuerPickerRow(
    entry: ClientOrIssuerState,
    isSelected: Boolean,
    hasMasterUpdate: Boolean,
    onClick: () -> Unit,
    onClickDelete: () -> Unit,
    onClickRefreshFromMaster: () -> Unit,
) {
    val displayName = entry.name.text +
        (entry.firstName?.text?.takeIf { it.isNotBlank() }?.let { " $it" } ?: "")
    val subtitle = entry.emails
        ?.mapNotNull { it.email.text.takeIf { s -> s.isNotBlank() } }
        ?.joinToString(", ")
        ?.takeIf { it.isNotBlank() }

    val rowModifier = if (isSelected) {
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(SelectedViolet)
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 12.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 12.dp)
    }

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.textBodySmall.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.textCaption,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (isSelected) {
                Text(
                    text = stringResource(Res.string.document_bottom_sheet_picker_edit_link),
                    style = MaterialTheme.typography.textCaption.copy(
                        color = SelectedVioletText,
                        textDecoration = TextDecoration.Underline,
                    ),
                    modifier = Modifier.padding(top = 7.dp),
                )
            }
        }
        if (isSelected) {
            if (hasMasterUpdate) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onClickRefreshFromMaster() },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Autorenew,
                        contentDescription = "Update from master",
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onClickDelete() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Remove",
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
