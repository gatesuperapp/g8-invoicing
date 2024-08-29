package com.a4a.g8invoicing.ui.screens

import android.widget.Toast
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.data.auth.AuthResult
import com.a4a.g8invoicing.ui.states.AuthState

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
    var userAuthorized by remember { mutableStateOf(false) }

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
    }

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
                onClickCategory = onClickCategory
            )
        }
    ) { padding ->
        val interactionSource = remember { MutableInteractionSource() }
        var showText by remember { mutableStateOf(true) }
        var showCreateAccountForm by remember { mutableStateOf(false) }

        Column(
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
        }
    }
}

