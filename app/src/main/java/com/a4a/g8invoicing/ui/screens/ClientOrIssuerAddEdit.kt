package com.a4a.g8invoicing.ui.screens

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.navigation.actionDone
import com.a4a.g8invoicing.ui.shared.ScreenElement

@Composable
fun ClientOrIssuerAddEdit(
    navController: NavController,
    clientOrIssuer: ClientOrIssuerState,
    isNew: Boolean,
    onValueChange: (ScreenElement, Any) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    onClickDone: () -> Unit,
    onClickBack: () -> Unit
) {
    Scaffold(
        topBar = {
            ClientAddEditTopBar(
                isNewClient = isNew,
                navController = navController,
                onClickDone =  onClickDone,
                onClickBackArrow = onClickBack
            )
        }
    ) {
        it

        ClientOrIssuerAddEditForm(
            clientOrIssuer,
            onValueChange,
            placeCursorAtTheEndOfText
        )

    }
}

@Composable
private fun ClientAddEditTopBar(
    isNewClient: Boolean,
    navController: NavController,
    onClickDone: () -> Unit,
    onClickBackArrow: () -> Unit,
) {
    TopBar(
        title = if (isNewClient) {
            R.string.appbar_title_add_new
        } else {
            R.string.appbar_title_info
        },
        actionDone(
            onClick = onClickDone
        ),
        navController = navController,
        onClickBackArrow = onClickBackArrow
    )
}
