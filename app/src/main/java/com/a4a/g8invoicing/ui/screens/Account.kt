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
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.shared.GeneralBottomBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.data.auth.AuthResult
import com.a4a.g8invoicing.ui.shared.AnimationLottie
import com.a4a.g8invoicing.ui.states.AuthState
import com.a4a.g8invoicing.ui.theme.ColorHotPink
import com.a4a.g8invoicing.ui.theme.ColorVioletLight

@Composable
fun Account(
    navController: NavController,
    uiState: AuthState,
    httpRequestResult: AuthResult<Unit>?,
    onClickCategory: (Category) -> Unit,
    onClickBack: () -> Unit,
    signUpUsernameChanged: (String) -> Unit,
    signUpPasswordChanged: (String) -> Unit,
    signUp: () -> Unit,
    signInUsernameChanged: (String) -> Unit,
    signInPasswordChanged: (String) -> Unit,
    signIn: () -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    // Add background when bottom menu expanded
    val transparent = Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
    val backgroundColor = remember { mutableStateOf(transparent) }

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
    /*
        LaunchedEffect(httpRequestResult, context) {
            when (httpRequestResult) {
                is AuthResult.Authorized -> {
                    userAuthorized = true
                    Toast.makeText(
                        context,
                        "You're authorized",
                        Toast.LENGTH_LONG
                    ).show()
                }

                is AuthResult.Unauthorized -> {
                    Toast.makeText(
                        context,
                        "You're not authorized",
                        Toast.LENGTH_LONG
                    ).show()
                }

                is AuthResult.UnknownError -> {
                    Toast.makeText(
                        context,
                        "An unknown error occurred",
                        Toast.LENGTH_LONG
                    ).show()
                }

                else -> {}
            }
        }*/

    Scaffold(
        topBar = {
            com.a4a.g8invoicing.ui.navigation.TopBar(
                title = R.string.appbar_account,
                navController = navController,
                onClickBackArrow = onClickBack
            )
        },
        //   private val _uiState = MutableStateFlow(ClientsUiState())
        // val uiState: StateFlow<ClientsUiState> = _uiState.asStateFlow()
        bottomBar = {
            GeneralBottomBar(
                navController = navController,
                onClickCategory = onClickCategory,
                onChangeBackground = {
                    backgroundColor.value =
                        changeBackgroundWithVerticalGradient(backgroundColor.value)
                }
            )
        }
    ) { padding ->
        val interactionSource = remember { MutableInteractionSource() }
        var showText by remember { mutableStateOf(true) }
        var showCreateAccountForm by remember { mutableStateOf(false) }

        /*Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (showText && !userAuthorized) {
                Text(
                    modifier = Modifier.padding(40.dp),
                    text =Strings.get(R.string.account_project_info)
                )

                Button(onClick = { showCreateAccountForm = true }) {
                    Text(Strings.get(R.string.account_button_create))
                }
            }

            if (showCreateAccountForm && !userAuthorized) {
                showText = false

                AccountAuthScreen(
                    uiState = uiState,
                    signUpUsernameChanged = signUpUsernameChanged,
                    signUpPasswordChanged = signUpPasswordChanged,
                    signUp = signUp,
                    signInUsernameChanged = signInUsernameChanged,
                    signInPasswordChanged = signInPasswordChanged,
                    signIn = signIn
                )
            }

            if (userAuthorized) {
                Text(Strings.get(R.string.account_creation_success))
            }
        }*/

        val numberOfIterations = remember { mutableIntStateOf(1) }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
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
                    textAlign = TextAlign.Center,
                    text = stringResource(id = R.string.account_subscribe)
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
                        uriHandler.openUri(Strings.get(R.string.account_donate_link))
                    },
                ) {
                    Text(stringResource(id = R.string.account_donate))
                }

                Spacer(modifier = Modifier.height(30.dp))

              //  var visibleText by remember { mutableStateOf(false) }

                Box(
                    Modifier
                        .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() } // This is mandatory
                    ) {
                        numberOfIterations.intValue += 1
           //             visibleText = !visibleText
                    }
                ) {
                    AnimationLottie(
                        modifier = Modifier
                            .width(300.dp)
                            .height(200.dp)
                            .align(Alignment.Center),
                        file = R.raw.bat_kiss_gif,
                        numberOfIteration = numberOfIterations.intValue
                    )
                }
/*
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
                        text = Strings.get(R.string.account_bat_love),
                        textAlign = TextAlign.Center
                    )
                }*/

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

