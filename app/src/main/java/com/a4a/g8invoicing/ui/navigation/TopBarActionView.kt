package com.a4a.g8invoicing.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.theme.callForActionsDisabled
import com.a4a.g8invoicing.ui.theme.callForActionsViolet

@Composable
fun TopBarActionView(
    appBarAction: AppBarAction?,
    iconSize: Dp,
) {
    appBarAction?.let {
        if (appBarAction.icon != null) {
            IconButton(
                onClick = appBarAction.onClick
            ) {
                Icon(
                    appBarAction.icon,
                    modifier = Modifier.size(iconSize),
                    tint = MaterialTheme.colorScheme.onBackground,
                    contentDescription = stringResource(id = appBarAction.description)
                )
            }
        }
    }
}

@Composable
fun TopBarCtaView(
    onClick: () -> Unit,
    ctaText: String?,
    ctaTextDisabled: Boolean? = true,
) {
    val requiredFieldsAreFilled: Boolean = ctaTextDisabled ?: false

    var customModifier = Modifier
        .padding(end = 20.dp)
    customModifier = if (requiredFieldsAreFilled)
        customModifier.then(
            Modifier
                .clickable { onClick() }
        ) else customModifier

    Text(
        style = if (requiredFieldsAreFilled) MaterialTheme.typography.callForActionsViolet
        else MaterialTheme.typography.callForActionsDisabled,
        modifier = customModifier,
        text = ctaText ?: ""
    )


}
