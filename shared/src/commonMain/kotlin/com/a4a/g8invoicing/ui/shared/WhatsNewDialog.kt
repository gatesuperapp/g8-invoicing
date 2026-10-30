package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.whats_new_23_lock_decoupled_description
import com.a4a.g8invoicing.shared.resources.whats_new_23_lock_decoupled_title
import com.a4a.g8invoicing.shared.resources.whats_new_23_lock_migration_description
import com.a4a.g8invoicing.shared.resources.whats_new_23_lock_migration_title
import com.a4a.g8invoicing.shared.resources.whats_new_close
import com.a4a.g8invoicing.shared.resources.whats_new_discover
import com.a4a.g8invoicing.shared.resources.whats_new_done
import com.a4a.g8invoicing.shared.resources.whats_new_welcome
import com.a4a.g8invoicing.ui.theme.AppColors
import com.a4a.g8invoicing.ui.theme.textBodyBold
import com.a4a.g8invoicing.ui.theme.textBodySmall
import com.a4a.g8invoicing.ui.theme.textDisplay
import com.a4a.g8invoicing.ui.theme.textHeadline
import com.a4a.g8invoicing.ui.theme.textScreenTitle
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

data class WhatsNewPage(
    val emoji: String,
    val title: String,
    val description: String
)

@Composable
fun WhatsNewDialog(
    appVersion: String = "",
    onDismiss: () -> Unit
) {
    // Page de bienvenue + pages de fonctionnalités. 2.3 reshapes
    // "Verrouillée" from a status tag into an orthogonal flag: invoices
    // keep their classic status (payée / envoyée / en retard / …) and
    // the lock stacks on top via a dedicated "Verrouiller" button. The
    // migration auto-rebases pre-2.3 locked invoices onto "Envoyée" so
    // nothing loses its mutually-exclusive status silently — slide 2
    // tells the user where their locked docs ended up.
    val welcomeTitle = stringResource(Res.string.whats_new_welcome, appVersion)

    val featurePages = listOf(
        WhatsNewPage(
            emoji = "🔓",
            title = stringResource(Res.string.whats_new_23_lock_decoupled_title),
            description = stringResource(Res.string.whats_new_23_lock_decoupled_description)
        ),
        WhatsNewPage(
            emoji = "🔒",
            title = stringResource(Res.string.whats_new_23_lock_migration_title),
            description = stringResource(Res.string.whats_new_23_lock_migration_description)
        )
    )

    // Total pages = 1 (welcome) + feature pages
    val totalPages = 1 + featurePages.size
    val pagerState = rememberPagerState(pageCount = { totalPages })
    val coroutineScope = rememberCoroutineScope()

    // Modal hauteur = ~50 % de la fenêtre, clampée [420, 560] pour éviter
    // la micro-modale sur grand écran paysage et le pavé déborde sur
    // téléphone compact. Toutes les slides partagent la même hauteur (pas
    // de jump en swipe) ; si une traduction (DE, ES) dépasse, la Column
    // de la page scrolle en interne via verticalScroll.
    val windowInfo = androidx.compose.ui.platform.LocalWindowInfo.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    val dialogHeight = with(density) {
        val windowHeightPx = windowInfo.containerSize.height
        if (windowHeightPx > 0) (windowHeightPx * 0.5f).toInt().toDp()
            .coerceIn(420.dp, 560.dp)
        else 460.dp
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(dialogHeight)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.surface)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Croix en haut à droite
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(Res.string.whats_new_close),
                        tint = AppColors.iconSecondary
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Pager pour les pages (prend tout l'espace disponible)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxWidth()
                        ) { pageIndex ->
                            if (pageIndex == 0) {
                                // Page de bienvenue
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(horizontal = 24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "((„• ֊ •„)♡",
                                        style = MaterialTheme.typography.textHeadline,
                                        textAlign = TextAlign.Center,
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = welcomeTitle,
                                        style = MaterialTheme.typography.textBodyBold,
                                        textAlign = TextAlign.Center,
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = stringResource(Res.string.whats_new_discover),
                                        style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textSecondary),
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            } else {
                                // Pages de fonctionnalités
                                val page = featurePages[pageIndex - 1]
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState())
                                        .padding(horizontal = 24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = page.emoji,
                                        style = MaterialTheme.typography.textDisplay,
                                        textAlign = TextAlign.Center,
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = page.title,
                                        style = MaterialTheme.typography.textScreenTitle,
                                        textAlign = TextAlign.Center,
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = page.description,
                                        style = MaterialTheme.typography.textBodySmall.copy(color = AppColors.textSecondary),
                                        textAlign = TextAlign.Center,
                                        lineHeight = 20.sp,
                                    )
                                }
                            }
                        }
                    }

                    // Breathing room entre la fin du texte de la page et la
                    // rangée des dots — sans ce spacer la description colle
                    // visuellement aux indicateurs sur les pages à 4-5 lignes.
                    Spacer(modifier = Modifier.height(20.dp))

                    // Indicateurs de page (dots) cliquables - toujours en bas
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        repeat(totalPages) { index ->
                            val isSelected = pagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(if (isSelected) 10.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) AppColors.accent else AppColors.surfaceMuted
                                    )
                                    .clickable {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bouton terminer (visible uniquement sur la dernière page)
                    if (pagerState.currentPage == totalPages - 1) {
                        TextButton(onClick = onDismiss) {
                            Text(
                                text = stringResource(Res.string.whats_new_done),
                                color = AppColors.accent,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // Espace pour garder la même hauteur
                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}
