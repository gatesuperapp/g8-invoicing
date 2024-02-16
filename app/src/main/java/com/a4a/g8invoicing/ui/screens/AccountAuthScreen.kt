package com.a4a.g8invoicing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.states.AuthState

@Composable
fun AccountAuthScreen(
    uiState: AuthState,
    signUpUsernameChanged: (String) -> Unit,
    signUpPasswordChanged: (String) -> Unit,
    signUp: () -> Unit,
    signInUsernameChanged: (String) -> Unit,
    signInPasswordChanged: (String) -> Unit,
    signIn: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = uiState.signUpUsername,
            onValueChange = { signUpUsernameChanged(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(text = "Username")
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = uiState.signUpPassword,
            onValueChange = { signUpPasswordChanged(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(text = "Password")
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = signUp,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(text = "Sign up")
        }

        Spacer(modifier = Modifier.height(64.dp))

        /*     TextField(
               value = uiState.signInUsername,
               onValueChange = { signInUsernameChanged(it) },
               modifier = Modifier.fillMaxWidth(),
               placeholder = {
                   Text(text = "Username")
               }
           )
         Spacer(modifier = Modifier.height(16.dp))
           TextField(
               value = uiState.signInPassword,
               onValueChange = { signInPasswordChanged(it) },
               modifier = Modifier.fillMaxWidth(),
               placeholder = {
                   Text(text = "Password")
               }
           )
           Spacer(modifier = Modifier.height(16.dp))
           Button(
               onClick = signIn,
               modifier = Modifier.align(Alignment.End)
           ) {
               Text(text = "Sign in")
           }*/
    }
    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}


