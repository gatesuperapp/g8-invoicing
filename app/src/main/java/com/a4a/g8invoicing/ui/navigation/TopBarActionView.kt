package com.a4a.g8invoicing.ui.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun TopBarActionView(
    appBarAction: AppBarAction?,
) {
    appBarAction?.let {
        IconButton(
            onClick = appBarAction.onClick
        ) {
            Icon(
                appBarAction.icon,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onBackground,
                contentDescription = stringResource(id = appBarAction.description)
            )
        }
    }
}
