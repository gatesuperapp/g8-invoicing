package com.a4a.g8invoicing.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.product_categories_none
import com.a4a.g8invoicing.ui.screens.shared.resolveCategoryColor
import com.a4a.g8invoicing.ui.shared.drawScrollThumb
import com.a4a.g8invoicing.ui.states.ProductCategoryState
import com.a4a.g8invoicing.ui.theme.AppColors
import org.jetbrains.compose.resources.stringResource

/**
 * Bottom-bar "Marquer" action for the product list bulk-category flow.
 * Same shape as [ButtonWithDropdownMenu] — a tinted transparent button
 * anchored to a DropdownMenu — but the menu items are rendered from the
 * user's custom product categories (so each row carries its own colour
 * swatch). Dash row at the top clears the category on the selection.
 */
@Composable
fun ButtonWithCategoryDropdown(
    action: AppBarAction,
    categories: List<ProductCategoryState>,
    onPick: (ProductCategoryState?) -> Unit,
    onChangeBackground: () -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }

    Button(
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        onClick = {
            onChangeBackground()
            isExpanded = true
        },
    ) {
        AddIconAndLabelInColumn(action, 24.dp)

        // Dropdown anchored to the Marquer button. Material3 auto-flips it
        // upward when there's no room below — on the bottom bar it always
        // unfolds up, which matches the user's spec.
        //
        // Material3 DropdownMenu wraps content in its own verticalScroll but
        // doesn't expose the state, so we hoist scroll into an inner bounded
        // Column — same 320-dp cap as the TopBar filter dropdown, plus a
        // right-edge thumb so the user sees that more rows exist below.
        DropdownMenu(
            modifier = Modifier
                .widthIn(min = 200.dp, max = 280.dp)
                .background(AppColors.surface),
            expanded = isExpanded,
            onDismissRequest = {
                onChangeBackground()
                isExpanded = false
            },
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .heightIn(max = 320.dp)
                    .verticalScroll(scrollState)
                    .drawScrollThumb(scrollState),
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stringResource(Res.string.product_categories_none),
                            color = AppColors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    onClick = {
                        onChangeBackground()
                        isExpanded = false
                        onPick(null)
                    },
                )
                categories.forEach { category ->
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(resolveCategoryColor(category.color)),
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = category.name,
                                    color = AppColors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        },
                        onClick = {
                            onChangeBackground()
                            isExpanded = false
                            onPick(category)
                        },
                    )
                }
            }
        }
    }
}
