package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.data.ClientOrIssuerState
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
    onClickBack: () -> Unit,
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
