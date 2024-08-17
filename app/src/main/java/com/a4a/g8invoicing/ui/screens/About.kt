package com.a4a.g8invoicing.ui.screens

import android.util.Log
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.shared.BottomBar
import com.a4a.g8invoicing.ui.theme.ColorBlueLink
import com.a4a.g8invoicing.ui.theme.ColorCoral
import com.a4a.g8invoicing.ui.theme.ColorGoldGift
import com.a4a.g8invoicing.ui.theme.ColorGreen
import com.a4a.g8invoicing.ui.theme.ColorLightGreenTransp
import com.a4a.g8invoicing.ui.theme.ColorLightGreyo
import com.a4a.g8invoicing.ui.theme.ColorRedGift
import com.a4a.g8invoicing.ui.theme.ColorSand
import com.itextpdf.forms.util.DrawingUtil.drawCircle
import java.util.Collections.rotate

@Composable
fun About(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickBack: () -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current


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

        val infiniteTransition = rememberInfiniteTransition(label = "border")
        val targetOffset = with(LocalDensity.current) {
            1000.dp.toPx()
        }
        val offset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = targetOffset,
            animationSpec = infiniteRepeatable(
                animation = tween(10000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "offset"
        )
        val brushSize = 400f
        val brush = Brush.linearGradient(
            colors = listOf(ColorBlueLink, ColorLightGreenTransp),
            start = Offset(offset, offset),
            end = Offset(offset + brushSize, offset + brushSize),
            tileMode = TileMode.Mirror
        )

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

            Button(
                modifier = Modifier.border(
                    BorderStroke(
                        width = 4.dp,
                        brush = brush
                    ),shape = RoundedCornerShape(50)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                onClick = {
                    uriHandler.openUri(Strings.get(R.string.about_donate_link))
                },
            ) {
                Text(stringResource(id = R.string.about_donate))
            }

            Text(
                modifier = Modifier.padding(
                    top = 40.dp,
                    start = 40.dp,
                    end = 40.dp,
                    bottom = 20.dp
                ),
                text = stringResource(id = R.string.about2)
            )

            Button(elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 3.dp
            ), onClick = {
                composeEmail(
                    context = context,
                    address = Strings.get(R.string.about_contact_email)
                )
            }) {
                Text(stringResource(id = R.string.about_button_contact))
            }

            TermsOfService(uriHandler)
        }
    }
}

@Composable
fun TermsOfService(uriHandler: UriHandler) {
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
        modifier = Modifier.padding(top = 20.dp, start = 40.dp, end = 40.dp, bottom = 20.dp),
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "policy", start = offset, end = offset)
                .firstOrNull()?.let {
                    uriHandler.openUri(it.item)
                }

            annotatedString.getStringAnnotations(tag = "terms", start = offset, end = offset)
                .firstOrNull()?.let {
                    uriHandler.openUri(it.item)
                }
        })
}