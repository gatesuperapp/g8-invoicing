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
import com.a4a.g8invoicing.shared.resources.whats_new_close
import com.a4a.g8invoicing.shared.resources.whats_new_discover
import com.a4a.g8invoicing.shared.resources.whats_new_done
import com.a4a.g8invoicing.shared.resources.whats_new_emails_title
import com.a4a.g8invoicing.shared.resources.whats_new_emails_description
import com.a4a.g8invoicing.shared.resources.whats_new_translations_title
import com.a4a.g8invoicing.shared.resources.whats_new_translations_description
import com.a4a.g8invoicing.shared.resources.whats_new_taxes_title
import com.a4a.g8invoicing.shared.resources.whats_new_taxes_description
import com.a4a.g8invoicing.shared.resources.whats_new_logo_title
import com.a4a.g8invoicing.shared.resources.whats_new_logo_description
import com.a4a.g8invoicing.shared.resources.whats_new_welcome
import com.a4a.g8invoicing.ui.theme.ColorVioletLight
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
    // Page de bienvenue + pages de fonctionnalités
    val welcomeTitle = stringResource(Res.string.whats_new_welcome, "$appVersion - Corylus avellana")

    val featurePages = listOf(
        WhatsNewPage(
            emoji = "💌",
            title = stringResource(Res.string.whats_new_emails_title),
            description = stringResource(Res.string.whats_new_emails_description)
        ),
        WhatsNewPage(
            emoji = "🌍",
            title = stringResource(Res.string.whats_new_translations_title),
            description = stringResource(Res.string.whats_new_translations_description)
        ),
        WhatsNewPage(
            emoji = "💶",
            title = stringResource(Res.string.whats_new_taxes_title),
            description = stringResource(Res.string.whats_new_taxes_description)
        ),
        WhatsNewPage(
            emoji = "🖼️",
            title = stringResource(Res.string.whats_new_logo_title),
            description = stringResource(Res.string.whats_new_logo_description)
        )
    )

    // Total pages = 1 (welcome) + feature pages
    val totalPages = 1 + featurePages.size
    val pagerState = rememberPagerState(pageCount = { totalPages })
    val coroutineScope = rememberCoroutineScope()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
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
                        tint = Color.Gray
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 40.dp, start = 24.dp, end = 24.dp, bottom = 16.dp),
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
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "((„• ֊ •„)♡",
                                        fontSize = 24.sp,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = welcomeTitle,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = stringResource(Res.string.whats_new_discover),
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center,
                                        color = Color.DarkGray
                                    )
                                }
                            } else {
                                // Pages de fonctionnalités
                                val page = featurePages[pageIndex - 1]
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = page.emoji,
                                        fontSize = 48.sp,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = page.title,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = page.description,
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 20.sp,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }

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
                                        if (isSelected) ColorVioletLight else Color.LightGray
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
                                color = ColorVioletLight,
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
