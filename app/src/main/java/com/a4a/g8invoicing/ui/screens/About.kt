package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.shared.GeneralBottomBar
import com.a4a.g8invoicing.ui.theme.ColorBlueLink
import com.a4a.g8invoicing.ui.theme.ColorVioletLight
import com.a4a.g8invoicing.ui.theme.textSmall
import com.a4a.g8invoicing.ui.theme.textTitle

@Composable
fun About(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickBack: () -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    // Add background when bottom menu expanded
    val transparent = Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
    val backgroundColor = remember { mutableStateOf(transparent) }

    Scaffold(
        topBar = {
            TopBar(
                title = R.string.appbar_about,
                navController = navController,
                onClickBackArrow = onClickBack
            )
        },
        bottomBar = {
            GeneralBottomBar(
                isButtonNewDisplayed = false,
                navController = navController,
                onClickCategory = onClickCategory,
                onChangeBackground = {
                    backgroundColor.value =
                        changeBackgroundWithVerticalGradient(backgroundColor.value)
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
                        top = 100.dp,
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

                Text(
                    modifier = Modifier.padding(
                        bottom = 30.dp
                    ),
                    text = stringResource(id = R.string.about) + "1.0"
                )
            }
            Column(
                // apply darker background when bottom menu is expanded
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor.value),
            ) {}
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
        modifier = Modifier.padding(
            bottom = 20.dp
        ),
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