package com.a4a.g8invoicing.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.data.auth.ActivatedModulesRepository
import com.a4a.g8invoicing.data.auth.SubscriptionRepository
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.gstore_quote_trial_exhausted_body
import com.a4a.g8invoicing.shared.resources.gstore_quote_trial_exhausted_cta
import com.a4a.g8invoicing.shared.resources.gstore_quote_trial_exhausted_title
import com.a4a.g8invoicing.ui.screens.QuoteList
import com.a4a.g8invoicing.ui.shared.PlatformBackHandler
import com.a4a.g8invoicing.ui.shared.currentTimeMillis
import com.a4a.g8invoicing.ui.theme.ColorVioletLink
import com.a4a.g8invoicing.ui.viewmodels.QuoteListViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.quoteList(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickListItem: (Int) -> Unit,
    onClickNew: () -> Unit,
    onClickBack: () -> Unit,
    onClickViewCreatedInvoice: (Long) -> Unit,
    showCategoryButton: Boolean = true,
) {
    composable(route = Screen.QuoteList.name) {
        val viewModel: QuoteListViewModel = koinViewModel()
        val quotesUiState by viewModel.quotesUiState.collectAsState()
        val activatedModules = koinInject<ActivatedModulesRepository>()
        val subscriptionRepository = koinInject<SubscriptionRepository>()

        var isCategoriesMenuOpen by remember { mutableStateOf(false) }
        var lastBackPressTime by remember { mutableStateOf(0L) }
        // Local state so the trial-exhausted modal stays owned by the QuoteList
        // route — it dismisses when the user leaves the screen and doesn't need
        // to escape the route as a side-channel.
        var showTrialExhausted by remember { mutableStateOf(false) }

        PlatformBackHandler {
            val currentTime = currentTimeMillis()
            if (currentTime - lastBackPressTime < 2000) {
                // Double back - exit (handled by platform)
            } else {
                lastBackPressTime = currentTime
                isCategoriesMenuOpen = true
            }
        }

        QuoteList(
            navController = navController,
            documentsUiState = quotesUiState,
            onClickDelete = viewModel::deleteQuotes,
            onClickDuplicate = viewModel::duplicateQuotes,
            onClickConvert = viewModel::convertQuotes,
            onClickNew = {
                // Trial cap applies only when the user is on the free discovery
                // module, doesn't have MODULE_QUOTE (premium unlimited) and
                // isn't premium via subscription. The subscription check keeps
                // a paid user who happens to also have the trial toggle from
                // being blocked accidentally.
                val premium = subscriptionRepository.isPremium()
                if (!premium && activatedModules.isQuoteTrialExhausted()) {
                    showTrialExhausted = true
                } else {
                    if (!premium &&
                        activatedModules.isActive(ActivatedModulesRepository.MODULE_QUOTE_TRIAL) &&
                        !activatedModules.isActive(ActivatedModulesRepository.MODULE_QUOTE)
                    ) {
                        activatedModules.incrementQuoteTrialCount()
                    }
                    onClickNew()
                }
            },
            onClickCategory = onClickCategory,
            onClickListItem = onClickListItem,
            onClickBack = { onClickBack() },
            onClickViewCreatedInvoice = { invoiceId ->
                viewModel.clearCreatedInvoiceId()
                onClickViewCreatedInvoice(invoiceId)
            },
            onDismissInvoiceCreatedDialog = viewModel::clearCreatedInvoiceId,
            isCategoriesMenuOpen = isCategoriesMenuOpen,
            onCategoriesMenuOpenChange = { isCategoriesMenuOpen = it },
            showCategoryButton = showCategoryButton
        )

        if (showTrialExhausted) {
            QuoteTrialExhaustedDialog(onDismiss = { showTrialExhausted = false })
        }
    }
}

@Composable
private fun QuoteTrialExhaustedDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .background(Color.White, shape = RoundedCornerShape(14.dp))
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(Res.string.gstore_quote_trial_exhausted_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(Res.string.gstore_quote_trial_exhausted_body),
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
                    color = Color.DarkGray,
                )
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onDismiss),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorVioletLink,
                        contentColor = Color.White,
                    ),
                ) { Text(stringResource(Res.string.gstore_quote_trial_exhausted_cta)) }
            }
        }
    }
}
