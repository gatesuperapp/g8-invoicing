package com.a4a.g8invoicing.ui.navigation


import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.data.auth.AuthResult
import com.a4a.g8invoicing.ui.screens.Account
import com.a4a.g8invoicing.ui.screens.AccountViewModel
import com.a4a.g8invoicing.ui.states.AuthUiEvent
import kotlinx.coroutines.flow.collect

fun NavGraphBuilder.account(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickBack: () -> Unit,
) {
    composable(route = Screen.Account.name) {
        val viewModel: AccountViewModel = hiltViewModel()
        viewModel.fetchResult()

        Account(
            navController = navController,
            uiState = viewModel.state,
            httpRequestResult = viewModel.result.value,
            onClickCategory = onClickCategory,
            onClickBack = onClickBack,
            signUpUsernameChanged = {
                viewModel.onEvent(AuthUiEvent.SignUpUsernameChanged(it))
            },
            signUpPasswordChanged = {
                viewModel.onEvent(AuthUiEvent.SignUpPasswordChanged(it))
            },
            signUp = {
                viewModel.onEvent(AuthUiEvent.SignUp)
            },
            signInUsernameChanged = {
                viewModel.onEvent(AuthUiEvent.SignInUsernameChanged(it))
            },
            signInPasswordChanged = {
                viewModel.onEvent(AuthUiEvent.SignInPasswordChanged(it))
            },
            signIn = {
                viewModel.onEvent(AuthUiEvent.SignIn)
            }

        )
    }
}