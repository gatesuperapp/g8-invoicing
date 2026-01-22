package com.a4a.g8invoicing.ui.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.document_modal_product_cancel
import com.a4a.g8invoicing.ui.theme.callForActionsViolet
import org.jetbrains.compose.resources.stringResource

//Provides back arrow navigation and eventually screen titles.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String? = null,
    appBarAction: AppBarAction? = null,
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
            if (appBarAction != null) {
                TopBarActionView(
                    appBarAction,
                    iconSize
                )
            } else TopBarCtaView(
                onClickCtaValidate,
                ctaText,
                ctaTextDisabled
            )
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
