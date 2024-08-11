package com.a4a.g8invoicing.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.shared.BottomBar
import com.a4a.g8invoicing.ui.theme.ColorBlueLink

@Composable
fun About(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickBack: () -> Unit,
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            com.a4a.g8invoicing.ui.navigation.TopBar(
                title = R.string.appbar_about,
                navController = navController,
                onClickBackArrow = onClickBack
            )
        },
        //   private val _uiState = MutableStateFlow(ClientsUiState())
        // val uiState: StateFlow<ClientsUiState> = _uiState.asStateFlow()
        bottomBar = {
            BottomBar(
                isButtonNewDisplayed = false,
                navController = navController,
                onClickCategory = onClickCategory
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(
                    top = 40.dp,
                    start = 40.dp,
                    end = 40.dp,
                    bottom = 20.dp
                ),
                text = stringResource(id = R.string.about)
            )

            Button(elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 6.dp
            ), onClick = {
                composeEmail(
                    context = context,
                    address = Strings.get(R.string.about_contact_email)
                )
            }) {
                Text(stringResource(id = R.string.about_button_contact))
            }

            TermsOfService()

        }
    }
}

@Composable
fun TermsOfService() {
    val annotatedString = buildAnnotatedString {
        append(Strings.get(R.string.about_terms_of_service_header) + " ")

        pushStringAnnotation(tag = "terms", annotation = "https://the-gate.fr/conditions-generales")
        withStyle(style = SpanStyle(color = ColorBlueLink)) {
            append(Strings.get(R.string.about_terms_of_service) + " ")
        }
        pop()

        append(Strings.get(R.string.about_terms_of_service_header_2) + " ")

        pushStringAnnotation(
            tag = "policy",
            annotation = "https://the-gate.fr/politique-confidentialite"
        )
        withStyle(style = SpanStyle(color = ColorBlueLink)) {
            append(Strings.get(R.string.about_privacy))
        }

        pop()
    }

    ClickableText(
        modifier = Modifier.padding(top = 40.dp, start = 40.dp, end = 40.dp, bottom = 20.dp),
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "policy", start = offset, end = offset)
                .firstOrNull()?.let {
                Log.d("policy URL", it.item)
            }

            annotatedString.getStringAnnotations(tag = "terms", start = offset, end = offset)
                .firstOrNull()?.let {
                Log.d("terms URL", it.item)
            }
        })
}

