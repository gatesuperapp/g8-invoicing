package com.a4a.g8invoicing.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.theme.callForActionsViolet
import kotlinx.coroutines.launch

//Provides back arrow navigation and eventually screen titles.


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    @StringRes title: Int? = null,
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
                    text = stringResource(id = it),
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
                    text = Strings.get(R.string.document_modal_product_cancel)
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

    //Adding this check to avoid the back arrow flicker when opening a detail screen
    // from a list (The back arrows was showing in the top bar while the new screen is loaded)
    val previousRoute = navController.previousBackStackEntry?.destination?.route
    val currentRoute = navController.currentDestination?.route
    val noBackArrowScreens = listOf(Screen.ProductList.name, Screen.ClientOrIssuerList.name)

    val showBackButton =
        currentRoute == Screen.ProductTaxRates.name || (navController.previousBackStackEntry != null &&
                previousRoute != null &&
                noBackArrowScreens.none { previousRoute.startsWith(it) })

    if (showBackButton) {
        IconButton(onClick = onClickBackArrow) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "back button"
            )
        }
    }
}


