package com.a4a.g8invoicing.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.document_modal_product_cancel
import com.a4a.g8invoicing.ui.theme.ColorVioletLight
import com.a4a.g8invoicing.ui.theme.callForActionsViolet
import org.jetbrains.compose.resources.stringResource

//Provides back arrow navigation and eventually screen titles.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String? = null,
    appBarAction: AppBarAction? = null,
    appBarActions: Array<AppBarAction>? = null, // Multiple actions for desktop
    ctaText: String? = null,
    ctaTextDisabled: Boolean? = null,
    onClickCtaValidate: () -> Unit = {},
    iconSize: Dp = 24.dp,
    navController: NavController,
    isCancelCtaDisplayed: Boolean,
    onClickBackArrow: () -> Unit,
) {
    TopAppBar(
        title = {
            title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        actions = {
            // Multiple actions (for desktop)
            if (appBarActions != null && appBarActions.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    appBarActions.forEachIndexed { index, action ->
                        // First action (usually "New") gets a prominent outlined button
                        if (index == 0 && action.label != null) {
                            OutlinedButton(
                                onClick = action.onClick,
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = ColorVioletLight
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = ColorVioletLight
                                )
                            ) {
                                action.icon?.let {
                                    Icon(
                                        imageVector = it,
                                        contentDescription = action.description,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                }
                                Text(text = action.label)
                            }
                        } else {
                            // Other actions get text buttons
                            androidx.compose.material3.TextButton(
                                onClick = action.onClick,
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                action.icon?.let {
                                    Icon(
                                        imageVector = it,
                                        contentDescription = action.description,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (action.label != null) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                }
                                action.label?.let {
                                    Text(
                                        text = it,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
            // Single action (legacy)
            else if (appBarAction != null) {
                TopBarActionView(
                    appBarAction,
                    iconSize
                )
            } else {
                TopBarCtaView(
                    onClickCtaValidate,
                    ctaText,
                    ctaTextDisabled
                )
            }
        },
        navigationIcon = {
            if (isCancelCtaDisplayed) {
                Text(
                    style = MaterialTheme.typography.callForActionsViolet,
                    modifier = Modifier
                        .padding(start = 20.dp)
                        .clickable { onClickBackArrow() },
                    text = stringResource(Res.string.document_modal_product_cancel)
                )
            } else
                BackArrow(
                    navController,
                    onClickBackArrow
                )
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackArrow(
    navController: NavController,
    onClickBackArrow: () -> Unit,
) {
    val currentRoute = navController.currentDestination?.route
    // Main screens that should not display a back arrow
    val noBackArrowScreens = listOf(
        Screen.InvoiceList.name,
        Screen.DeliveryNoteList.name,
        Screen.CreditNoteList.name,
        Screen.ProductList.name,
        Screen.ClientOrIssuerList.name
    )

    // Show back arrow if:
    // - Current screen is ProductTaxRates, OR
    // - There's a previous screen AND current screen is NOT a main list screen
    val showBackButton =
        currentRoute == Screen.ProductTaxRates.name ||
        (navController.previousBackStackEntry != null &&
         currentRoute != null &&
         noBackArrowScreens.none { currentRoute.startsWith(it) })

    if (showBackButton) {
        IconButton(onClick = onClickBackArrow) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "back button"
            )
        }
    }
}
