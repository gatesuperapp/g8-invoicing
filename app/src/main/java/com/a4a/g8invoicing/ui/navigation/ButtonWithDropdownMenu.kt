package com.a4a.g8invoicing.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

/**
 * Menu displaying secondary icons, accessed after clicking "More" icon
 *
 */

@Composable
fun ButtonWithDropdownMenu(
    action: AppBarAction,
    secondaryItems: List<AppBarAction>,
) {
    var isExpanded by remember { mutableStateOf(false) }

    Button(
        contentPadding = PaddingValues(0.dp),
        onClick = {
            isExpanded = true
        },
    ) {
        AddIconAndLabelInColumn(action, 24.dp)

        DropdownMenu(
            modifier = Modifier
                .background(Color.White),
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            secondaryItems.forEach { icon ->
                DropdownMenuItem(
                    text = { Text(stringResource(icon.description)) },
                    onClick = {
                        isExpanded = false
                        action.onClick()
                    },
                    leadingIcon = {
                        Icon(
                            action.icon,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onBackground,
                            contentDescription = stringResource(id = action.description)
                        )
                    }
                )
            }
        }
    }
}