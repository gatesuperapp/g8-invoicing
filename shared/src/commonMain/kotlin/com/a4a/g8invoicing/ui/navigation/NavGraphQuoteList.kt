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
import androidx.compose.material3.MaterialTheme
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
import com.a4a.g8invoicing.data.models.TagUpdateOrCreationCase
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.gstore_quote_trial_exhausted_body
import com.a4a.g8invoicing.shared.resources.gstore_quote_trial_exhausted_cta
import com.a4a.g8invoicing.shared.resources.gstore_quote_trial_exhausted_title
import com.a4a.g8invoicing.ui.screens.QuoteList
import com.a4a.g8invoicing.ui.shared.PlatformBackHandler
import com.a4a.g8invoicing.ui.shared.currentTimeMillis
import com.a4a.g8invoicing.ui.theme.AppColors
import com.a4a.g8invoicing.ui.theme.ColorVioletLink
import com.a4a.g8invoicing.ui.theme.textBodyBold
import com.a4a.g8invoicing.ui.theme.textBodySmall
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
        val activatedState by activatedModules.state.collectAsState()
        val isTagPickerEnabled =
            ActivatedModulesRepository.MODULE_QUOTE_TAGGING in activatedState

        var isCategoriesMenuOpen by remember { mutableStateOf(false) }
        var lastBackPressTime by remember { mutableStateOf(0L) }
        // Local state so the trial-exhausted modal stays owned by the QuoteList
        // route — it dismisses when the user leaves the screen and doesn't need
        // to escape the route as a side-channel.
        var showTrialExhausted by remember { mutableStateOf(false) }
        // Distinct modal for ex-premium users (was premium, activated MODULE_QUOTE,
        // no longer premium). They keep read access to their quotes but lose the
        // ability to create new ones — see the buildList in [CategorySidebar] which
        // keeps the category visible via [ActivatedModulesRepository.everActivated].
        var showExPremium by remember { mutableStateOf(false) }

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
            onClickDuplicate = { selected ->
                // Duplicating N quotes is equivalent to N net-new quotes for the
                // trial cap — see ActivatedModulesRepository. Block the whole
                // batch when it wouldn't fit, rather than silently duplicating
                // only some of the selection.
                val premium = subscriptionRepository.isPremium()
                val onTrial = activatedModules.isActive(ActivatedModulesRepository.MODULE_QUOTE_TRIAL) &&
                    !activatedModules.isActive(ActivatedModulesRepository.MODULE_QUOTE)
                when {
                    premium -> viewModel.duplicateQuotes(selected)
                    onTrial && activatedModules.wouldExhaustQuoteTrial(selected.size) ->
                        showTrialExhausted = true
                    onTrial -> {
                        activatedModules.incrementQuoteTrialCount(by = selected.size)
                        viewModel.duplicateQuotes(selected)
                    }
                    // Not premium, not on trial — this is an ex-premium user whose
                    // MODULE_QUOTE preference kept the category visible via
                    // everActivated. Read-only mode: block creation, keep listing.
                    else -> showExPremium = true
                }
            },
            onClickConvert = viewModel::convertQuotes,
            onClickNew = {
                // Trial cap applies only when the user is on the free discovery
                // module, doesn't have MODULE_QUOTE (premium unlimited) and
                // isn't premium via subscription. The subscription check keeps
                // a paid user who happens to also have the trial toggle from
                // being blocked accidentally.
                val premium = subscriptionRepository.isPremium()
                val onTrial = activatedModules.isActive(ActivatedModulesRepository.MODULE_QUOTE_TRIAL) &&
                    !activatedModules.isActive(ActivatedModulesRepository.MODULE_QUOTE)
                when {
                    premium -> onClickNew()
                    onTrial && activatedModules.isQuoteTrialExhausted() ->
                        showTrialExhausted = true
                    onTrial -> {
                        activatedModules.incrementQuoteTrialCount()
                        onClickNew()
                    }
                    // Ex-premium: quotes stay listable but creation is blocked.
                    else -> showExPremium = true
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
            onClickTag = { selected, tag ->
                viewModel.setTag(selected, tag, TagUpdateOrCreationCase.UPDATED_BY_USER)
            },
            isTagPickerEnabled = isTagPickerEnabled,
            isCategoriesMenuOpen = isCategoriesMenuOpen,
            onCategoriesMenuOpenChange = { isCategoriesMenuOpen = it },
            showCategoryButton = showCategoryButton
        )

        if (showTrialExhausted) {
            QuoteTrialExhaustedDialog(onDismiss = { showTrialExhausted = false })
        }
        if (showExPremium) {
            QuoteExPremiumDialog(onDismiss = { showExPremium = false })
        }
    }
}

// TODO(strings): move the FR literals below to shared/src/commonMain/composeResources/values/strings.xml
// on the `translations` branch. Suggested keys:
//   gstore_quote_ex_premium_title = "Compte premium requis"
//   gstore_quote_ex_premium_body  = "Vos devis existants restent accessibles et exportables. Pour créer ou dupliquer de nouveaux devis, il faut être membre premium."
//   gstore_quote_ex_premium_cta   = "D'accord"
// Then replace the hardcoded strings in [QuoteExPremiumDialog] with stringResource(Res.string.*).
@Composable
private fun QuoteExPremiumDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .background(AppColors.surface, shape = RoundedCornerShape(14.dp))
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Compte premium requis",
                    style = MaterialTheme.typography.textBodyBold.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Vos devis existants restent accessibles et exportables. Pour créer ou dupliquer de nouveaux devis, il faut être membre premium.",
                    style = MaterialTheme.typography.textBodySmall.copy(color = Color.DarkGray),
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
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
                ) { Text("D'accord") }
            }
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
                .background(AppColors.surface, shape = RoundedCornerShape(14.dp))
                .padding(horizontal = 20.dp, vertical = 24.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(Res.string.gstore_quote_trial_exhausted_title),
                    style = MaterialTheme.typography.textBodyBold.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = stringResource(Res.string.gstore_quote_trial_exhausted_body),
                    style = MaterialTheme.typography.textBodySmall.copy(color = Color.DarkGray),
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center,
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
