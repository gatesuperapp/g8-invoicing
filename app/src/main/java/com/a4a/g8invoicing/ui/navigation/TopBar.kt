package com.a4a.g8invoicing.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.theme.callForActionsViolet
import com.ninetyninepercent.funfactu.icons.IconArrowBack
import icons.IconMenu
import kotlinx.coroutines.launch

/**
 * [TopBar] provides back arrow navigation and eventually screen titles.
 */

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
    isCancelCtaDisplayed: Boolean = false,
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

    val currentBackStackEntry by navController.currentBackStackEntryAsState()

    val showBackButton by remember(currentBackStackEntry) {
        derivedStateOf {
            navController.previousBackStackEntry != null
        }
    }

    if (showBackButton) {
        IconButton(onClick = onClickBackArrow) {
            Icon(
                imageVector = IconArrowBack,
                contentDescription = "back button"
            )
        }
    } else {
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DrawerIcon(drawerState: DrawerState) {
    val coroutineScope = rememberCoroutineScope()
    IconButton(onClick = {
        coroutineScope.launch {
            drawerState.open()
        }
    }) {
        Icon(
            IconMenu,
            tint = MaterialTheme.colorScheme.onBackground,
            contentDescription = stringResource(id = R.string.drawer_menu_description)
        )
    }
}




