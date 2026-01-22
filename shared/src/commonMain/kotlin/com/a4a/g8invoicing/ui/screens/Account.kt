package com.a4a.g8invoicing.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.account_bat_love
import com.a4a.g8invoicing.shared.resources.account_donate
import com.a4a.g8invoicing.shared.resources.account_donate_link
import com.a4a.g8invoicing.shared.resources.account_share_content
import com.a4a.g8invoicing.shared.resources.account_subscribe
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.shared.GeneralBottomBar
import com.a4a.g8invoicing.ui.shared.animations.BatKiss
import com.a4a.g8invoicing.ui.theme.ColorHotPink
import com.a4a.g8invoicing.ui.theme.ColorVioletLight
import org.jetbrains.compose.resources.stringResource

@Composable
fun Account(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickBack: () -> Unit,
    onShareContent: (String) -> Unit = {},
) {
    val uriHandler = LocalUriHandler.current

    // Add background when bottom menu expanded
    val isDimActive = remember { mutableStateOf(false) }

    // Animation around button
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
        colors = listOf(ColorVioletLight, ColorHotPink),
        start = Offset(offset, offset),
        end = Offset(offset + brushSize, offset + brushSize),
        tileMode = TileMode.Mirror
    )

    Scaffold(
        topBar = {
            com.a4a.g8invoicing.ui.navigation.TopBar(
              //  title = R.string.appbar_account,
                navController = navController,
                onClickBackArrow = onClickBack,
                isCancelCtaDisplayed = false
            )
        },
        bottomBar = {
            GeneralBottomBar(
                navController = navController,
                onClickCategory = onClickCategory,
                onChangeBackground = {
                    isDimActive.value = !isDimActive.value
                },
                isButtonNewDisplayed = false
            )
        }
    ) { padding ->
        val interactionSource = remember { MutableInteractionSource() }
        var showText by remember { mutableStateOf(true) }
        var showCreateAccountForm by remember { mutableStateOf(false) }

        val numberOfIterations = remember { mutableIntStateOf(2) }

        val donateLink = stringResource(Res.string.account_donate_link)
        val shareContent = stringResource(Res.string.account_share_content)

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(top=110.dp)
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
                    textAlign = TextAlign.Center,
                    text = stringResource(Res.string.account_subscribe)
                )

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    modifier = Modifier.border(
                        BorderStroke(
                            width = 4.dp,
                            brush = brush
                        ), shape = RoundedCornerShape(50)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                    onClick = {
                        uriHandler.openUri(donateLink)
                    },
                ) {
                    Text(stringResource(Res.string.account_donate))
                }

                Spacer(modifier = Modifier.height(40.dp))

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    modifier = Modifier.border(
                        BorderStroke(
                            width = 4.dp,
                            brush = brush
                        ), shape = RoundedCornerShape(50)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                    onClick = {
                        onShareContent(shareContent)
                    },
                ) {
                //    Text(stringResource(id = R.string.account_share_button))
                }

                Spacer(modifier = Modifier.height(30.dp))

                var visibleText by remember { mutableStateOf(false) }

                Box(
                    Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() } // This is mandatory
                        ) {
                            numberOfIterations.intValue += 1
                            visibleText = !visibleText
                        }
                ) {
                    BatKiss(
                        modifier = Modifier
                            .width(300.dp)
                            .height(200.dp)
                            .align(Alignment.Center),
                        iterations = numberOfIterations.intValue
                    )
                }

                AnimatedVisibility(
                    visible = visibleText,
                    enter = fadeIn(
                        tween(
                            2000,
                            delayMillis = 100,
                            easing = LinearOutSlowInEasing
                        )
                    ),
                    exit = fadeOut(tween(100)),
                ) {
                    Text(
                        text = stringResource(Res.string.account_bat_love),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
