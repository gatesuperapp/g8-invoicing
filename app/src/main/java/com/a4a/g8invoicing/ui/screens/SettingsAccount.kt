package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.shared.ScreenElement
import com.a4a.g8invoicing.ui.shared.SharedBottomBar
import com.a4a.g8invoicing.ui.states.ClientsOrIssuerUiState

@Composable
fun SettingsAccount(
    navController: NavController,
    uiState: ClientsOrIssuerUiState,
    onClickCategory: (Category) -> Unit,
    onClickBack: () -> Unit,
    onClickForward: (ScreenElement) -> Unit
) {
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
            SharedBottomBar(
                navController = navController,
                onClickCategory = onClickCategory
            )
        }
    ) { padding ->
        val interactionSource = remember { MutableInteractionSource() }

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.Top, // For label to stay on top when multiline input
                modifier = Modifier
                    .clickable(
                        interactionSource = interactionSource,
                        indication = rememberRipple(color = Color.Black, bounded = true),
                        onClick = {
                            onClickForward(ScreenElement.SETTINGS_ACCOUNT)
                        }
                    )
                    .fillMaxWidth()
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = 14.dp,
                        bottom = 14.dp
                    )
            ) {

            }
        }
    }
}

