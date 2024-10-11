package com.a4a.g8invoicing.ui.screens

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.states.ClientOrIssuerState
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.viewmodels.ClientOrIssuerType


@Composable
fun ClientAddEdit(
    navController: NavController,
    clientOrIssuer: ClientOrIssuerState,
    onValueChange: (ScreenElement, Any) -> Unit,
    placeCursorAtTheEndOfText: (ScreenElement) -> Unit,
    onClickDone: () -> Unit,
    onClickBack: () -> Unit,
    onClickDeleteAddress: () -> Unit,
) {
    val requiredFieldsAreFilled: Boolean =
        if (clientOrIssuer.type?.name == ClientOrIssuerType.CLIENT.name
            && clientOrIssuer.name.text.isNotEmpty()
        ) {
            true
        } else if (clientOrIssuer.type?.name == ClientOrIssuerType.ISSUER.name
            && clientOrIssuer.name.text.isNotEmpty()
        ) {
            true
        } else false


    Scaffold(
        topBar = {
            ClientAddEditTopBar(
                navController = navController,
                onClickDone = onClickDone,
                onClickBackArrow = onClickBack,
                isValidateCtaDisabled = requiredFieldsAreFilled
            )
        }
    ) {
        it

        ClientOrIssuerAddEditForm(
            clientOrIssuer,
            onValueChange,
            placeCursorAtTheEndOfText,
            onClickDeleteAddress = onClickDeleteAddress
        )

    }
}

@Composable
private fun ClientAddEditTopBar(
    navController: NavController,
    onClickDone: () -> Unit,
    onClickBackArrow: () -> Unit,
    isValidateCtaDisabled: Boolean,
) {
    TopBar(
        ctaText = Strings.get(R.string.document_modal_product_save),
        ctaTextDisabled = isValidateCtaDisabled,
        navController = navController,
        onClickBackArrow = onClickBackArrow,
        isCancelCtaDisplayed = true,
        onClickCtaValidate = onClickDone
    )
}
