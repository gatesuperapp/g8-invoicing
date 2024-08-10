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
            .padding(bottom = 2.dp)
            .wrapContentHeight(),
        style = MaterialTheme.typography.textForDocumentsBold,
        text = (clientOrIssuer?.firstName.let {
            (it?.text ?: "") + " "
        }) + (clientOrIssuer?.name?.text ?: "")
    )
    clientOrIssuer?.address1?.let {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier.wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = it.text
        )
    }
    clientOrIssuer?.address2?.let {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier.wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = it.text
        )
    }
    clientOrIssuer?.zipCode?.let {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier.wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = it.text + " " + clientOrIssuer.city?.text
        )
    }
    clientOrIssuer?.phone?.let {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier.wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = it.text
        )
    }
    clientOrIssuer?.email?.let {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(bottom = 3.dp)
                .wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = it.text
        )
    }
    clientOrIssuer?.companyId1Number?.let {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier.wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = clientOrIssuer.companyId1Label?.text + " : " + it.text
        )
    }
    clientOrIssuer?.companyId2Number?.let {
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier.wrapContentHeight(),
            style = MaterialTheme.typography.textForDocuments,
            text = clientOrIssuer.companyId2Label?.text + " : " + it.text
        )
    }
}