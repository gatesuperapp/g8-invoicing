package com.a4a.g8invoicing.ui.screens

import android.R.attr.label
import android.R.attr.text
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.NavController
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.shared.BottomBar


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
                navController = navController,
                onClickCategory = onClickCategory
            )
        }
    ) { padding ->
        var showCreateAccountForm by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(40.dp),
                text = stringResource(id = R.string.about)
            )

            Button(onClick = {
                composeEmail(
                    context = context,
                    address = Strings.get(R.string.about_contact_email)
                )
            }) {
                Text(stringResource(id = R.string.about_button_contact))
            }
        }
    }
}

/*fun Context.copyToClipboard(text: CharSequence) {
    val clipboard = getSystemService(this, ClipboardManager::class.java)
    clipboard?.setPrimaryClip(ClipData.newPlainText("", text))
}*/


