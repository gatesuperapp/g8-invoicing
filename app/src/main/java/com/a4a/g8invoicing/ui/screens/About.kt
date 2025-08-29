package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.screens.shared.ScaffoldWithDimmedOverlay
import com.a4a.g8invoicing.ui.shared.GeneralBottomBar
import com.a4a.g8invoicing.ui.theme.ColorVioletLight
import com.a4a.g8invoicing.ui.theme.ColorVioletLink
import com.a4a.g8invoicing.ui.theme.textNormalBold
import com.a4a.g8invoicing.ui.theme.textTitle
import java.io.File

@Composable
fun About(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickBack: () -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    // Add background when bottom menu expanded
    val isDimActive = remember { mutableStateOf(false) }

    //Export db popup
    var exportedFile: File? = null
    var exportErrorMessage: String? = null
    var showExportErrorDialog by remember { mutableStateOf(false) }
    var showSendDatabaseByEmailDialog by remember { mutableStateOf(false) }

    ScaffoldWithDimmedOverlay(
        isDimmed = isDimActive.value,
        onDismissDim = { isDimActive.value = false },
        topBar = {
            TopBar(
                title = R.string.appbar_about,
                navController = navController,
                onClickBackArrow = onClickBack,
                isCancelCtaDisplayed = false
            )
        },
        bottomBar = {
            GeneralBottomBar(
                isButtonNewDisplayed = false,
                navController = navController,
                onClickCategory = onClickCategory,
                onChangeBackground = {
                    isDimActive.value = !isDimActive.value

                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(
                        top = 130.dp,
                        bottom = 100.dp,
                        start = 40.dp,
                        end = 40.dp,
                    )
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top
            ) {

                Text(
                    modifier = Modifier.padding(
                        bottom = 16.dp
                    ),
                    text = stringResource(id = R.string.about_title_tutorials),
                    style = MaterialTheme.typography.textTitle,
                )

                Text(
                    modifier = Modifier.padding(
                        bottom = 10.dp
                    ),
                    text = stringResource(id = R.string.about_tutorials)
                )

                Button(
                    modifier = Modifier.padding(
                        bottom = 40.dp
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 3.dp
                    ), onClick = {
                        uriHandler.openUri(Strings.get(R.string.account_donate_link))
                    }) {
                    Text(stringResource(id = R.string.about_title_tutorials_button))
                }



                Text(
                    modifier = Modifier.padding(
                        bottom = 16.dp
                    ),
                    text = stringResource(id = R.string.about_title_contact),
                    style = MaterialTheme.typography.textTitle,
                )


                Text(
                    modifier = Modifier.padding(
                        bottom = 10.dp
                    ),
                    text = stringResource(id = R.string.about2)
                )

                Button(
                    modifier = Modifier.padding(
                        bottom = 40.dp
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 3.dp
                    ), onClick = {
                        composeEmail(
                            context = context,
                            address = Strings.get(R.string.about_contact_email),
                            emailSubject = "",
                            emailMessage = ""
                        )
                    }) {
                    Text(stringResource(id = R.string.about_button_contact))
                }


                Text(
                    modifier = Modifier.padding(
                        bottom = 16.dp
                    ),
                    text = stringResource(id = R.string.about_title_about),
                    style = MaterialTheme.typography.textTitle,
                )


                Text(
                    modifier = Modifier.padding(
                        bottom = 10.dp
                    ),
                    text = stringResource(id = R.string.about)
                )

                TermsOfService(uriHandler)

                Spacer(modifier = Modifier.height(30.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(id = R.string.about_build_version),
                        style = MaterialTheme.typography.textNormalBold
                    )
                    Text(
                        text = "1.1",
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Lien "Télécharger la BDD"
                Text(
                    text = stringResource(id = R.string.about_download_database),
                    color = ColorVioletLink,
                    style = MaterialTheme.typography.textNormalBold,
                    modifier = Modifier
                        .padding(bottom = 20.dp)
                        .clickable {
                            exportedFile = try {
                                exportDatabaseToDownloads(context)
                            } catch (e: Exception) {
                                exportErrorMessage = context.getString(
                                    R.string.about_download_database_error,
                                    e.message ?: ""
                                )
                                showExportErrorDialog = true
                                null
                            }

                            if (exportedFile != null) showSendDatabaseByEmailDialog = true
                        }
                )
            }
        }
        if (showSendDatabaseByEmailDialog) {
            exportedFile?.let {
                DatabaseEmailDialog(
                    context = context,
                    onDismiss = {
                        showSendDatabaseByEmailDialog = false
                    },
                    file = it
                )
            }
        }

        if (showExportErrorDialog) {
            AlertDialog(
                onDismissRequest = { showExportErrorDialog = false },
                text = { Text(exportErrorMessage ?: "") },
                confirmButton = {
                    TextButton(onClick = {
                        showExportErrorDialog = false
                    }) {
                        Text(stringResource(id = R.string.ok), color = ColorVioletLink)
                    }
                }
            )
        }
    }
}

@Composable
fun TermsOfService(uriHandler: UriHandler) {
    val annotatedString = buildAnnotatedString {
        append(Strings.get(R.string.about_terms_of_service_header) + " ")
        pushStringAnnotation(
            tag = "terms",
            annotation = Strings.get(R.string.about_terms_of_service_url_1)
        )
        withStyle(style = SpanStyle(color = ColorVioletLight)) {
            append(Strings.get(R.string.about_terms_of_service) + " ")
        }
        pop()
        append(Strings.get(R.string.about_terms_of_service_header_2) + " ")
        pushStringAnnotation(
            tag = "policy",
            annotation = Strings.get(R.string.about_terms_of_service_url_2)
        )
        withStyle(style = SpanStyle(color = ColorVioletLight)) {
            append(Strings.get(R.string.about_privacy))
        }
        pop()
    }

    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "terms", start = offset, end = offset)
                .firstOrNull()?.let {
                    uriHandler.openUri(it.item)
                }
            annotatedString.getStringAnnotations(tag = "policy", start = offset, end = offset)
                .firstOrNull()?.let {
                    uriHandler.openUri(it.item)
                }
        })
}