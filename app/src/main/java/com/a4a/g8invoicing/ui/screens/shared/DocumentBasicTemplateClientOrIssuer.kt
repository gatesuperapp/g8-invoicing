package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.states.DocumentClientOrIssuerState
import com.a4a.g8invoicing.ui.theme.textForDocuments
import com.a4a.g8invoicing.ui.theme.textForDocumentsBold

@Composable
fun DocumentBasicTemplateClientOrIssuer(clientOrIssuer: DocumentClientOrIssuerState?) {
    Text(
        textAlign = TextAlign.Center, // have to specify it (in addition to column alignment)
        // because without it, multilines text aren't aligned
        modifier = Modifier
            .padding(bottom = 3.dp)
            .wrapContentHeight(),
        style = MaterialTheme.typography.textForDocumentsBold,
        text = (clientOrIssuer?.firstName?.let {
            ((it.text))
        }) + (clientOrIssuer?.name?.text ?: "")
    )
    if (!clientOrIssuer?.address1?.text.isNullOrEmpty()) {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 3.dp)
                .wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = clientOrIssuer?.address1?.text ?: ""
        )
    }
    if (!clientOrIssuer?.address2?.text.isNullOrEmpty()) {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 3.dp)
                .wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = clientOrIssuer?.address2?.text ?: ""
        )
    }
    if (!clientOrIssuer?.zipCode?.text.isNullOrEmpty() ||
        !clientOrIssuer?.city?.text.isNullOrEmpty()
    ) {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 3.dp)
                .wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = clientOrIssuer?.zipCode?.text + " " + clientOrIssuer?.city?.text
        )
    }
    if (!clientOrIssuer?.phone?.text.isNullOrEmpty()) {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 3.dp)
                .wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = clientOrIssuer?.phone?.text ?: ""
        )
    }
    if (!clientOrIssuer?.email?.text.isNullOrEmpty()) {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 3.dp)
                .wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = clientOrIssuer?.email?.text ?: ""
        )
    }
    if (!clientOrIssuer?.companyId1Number?.text.isNullOrEmpty()) {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 3.dp)
                .wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = clientOrIssuer?.companyId1Label?.text + " : "
                    + clientOrIssuer?.companyId1Number?.text
        )
    }
    if (!clientOrIssuer?.companyId2Number?.text.isNullOrEmpty()) {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 3.dp)
                .wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = clientOrIssuer?.companyId2Label?.text + " : "
                    + clientOrIssuer?.companyId2Number?.text
        )
    }
}