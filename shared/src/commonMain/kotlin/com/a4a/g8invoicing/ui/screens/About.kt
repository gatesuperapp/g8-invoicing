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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.shared.resources.Res
import com.a4a.g8invoicing.shared.resources.about
import com.a4a.g8invoicing.shared.resources.about_assistance_intro
import com.a4a.g8invoicing.shared.resources.about_assistance_more_lead
import com.a4a.g8invoicing.shared.resources.about_assistance_more_middle
import com.a4a.g8invoicing.shared.resources.about_assistance_more_tail
import com.a4a.g8invoicing.shared.resources.about_assistance_forum_link
import com.a4a.g8invoicing.shared.resources.about_assistance_forum_link_text
import com.a4a.g8invoicing.shared.resources.about_assistance_tutorials_link
import com.a4a.g8invoicing.shared.resources.about_assistance_tutorials_link_text
import com.a4a.g8invoicing.shared.resources.about_backup_text
import com.a4a.g8invoicing.shared.resources.about_community_cta
import com.a4a.g8invoicing.shared.resources.about_community_link
import com.a4a.g8invoicing.shared.resources.about_community_text
import com.a4a.g8invoicing.shared.resources.about_contact_email
import com.a4a.g8invoicing.shared.resources.about_download_database
import com.a4a.g8invoicing.shared.resources.about_privacy
import com.a4a.g8invoicing.shared.resources.about_share_button
import com.a4a.g8invoicing.shared.resources.about_share_text_2
import com.a4a.g8invoicing.shared.resources.about_terms_of_service
import com.a4a.g8invoicing.shared.resources.about_terms_of_service_header
import com.a4a.g8invoicing.shared.resources.about_terms_of_service_header_2
import com.a4a.g8invoicing.shared.resources.about_terms_of_service_url_1
import com.a4a.g8invoicing.shared.resources.about_terms_of_service_url_2
import com.a4a.g8invoicing.shared.resources.about_title_about
import com.a4a.g8invoicing.shared.resources.about_title_assistance
import com.a4a.g8invoicing.shared.resources.about_title_backup
import com.a4a.g8invoicing.shared.resources.about_title_community
import com.a4a.g8invoicing.shared.resources.about_title_legal
import com.a4a.g8invoicing.shared.resources.about_title_version
import com.a4a.g8invoicing.shared.resources.about_title_website
import com.a4a.g8invoicing.shared.resources.about_website_cta
import com.a4a.g8invoicing.shared.resources.about_website_intro
import com.a4a.g8invoicing.shared.resources.about_website_link
import com.a4a.g8invoicing.shared.resources.about_website_tail
import com.a4a.g8invoicing.shared.resources.account_bat_love
import com.a4a.g8invoicing.shared.resources.account_share_content
import com.a4a.g8invoicing.shared.resources.drawer_g8
import com.a4a.g8invoicing.shared.resources.ok
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.screens.shared.ScaffoldWithDimmedOverlay
import com.a4a.g8invoicing.ui.shared.CollapsibleSection
import com.a4a.g8invoicing.ui.shared.GeneralBottomBar
import com.a4a.g8invoicing.ui.shared.animations.BatKiss
import com.a4a.g8invoicing.ui.theme.ColorHotPink
import com.a4a.g8invoicing.ui.theme.ColorVioletLight
import com.a4a.g8invoicing.ui.theme.ColorVioletLink
import com.a4a.g8invoicing.ui.theme.textNormalBold
import org.jetbrains.compose.resources.stringResource

/**
 * Result of database export operation
 */
sealed class ExportResult {
    data class Success(val filePath: String) : ExportResult()
    data class Error(val message: String) : ExportResult()
}

@Composable
fun About(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickBack: () -> Unit,
    versionName: String = "?",
    onShareContent: (String) -> Unit = {},
    onComposeEmail: (address: String, subject: String, body: String) -> Unit = { _, _, _ -> },
    // Callbacks for categories menu state (managed by NavGraph for BackHandler)
    isCategoriesMenuOpen: Boolean = false,
    onCategoriesMenuOpenChange: (Boolean) -> Unit = {},
    showCategoryButton: Boolean = true,
) {
    val uriHandler = LocalUriHandler.current
    val isDimActive = remember { mutableStateOf(false) }

    // Animation pour le bouton "En savoir plus"
    val infiniteTransition = rememberInfiniteTransition(label = "border")
    val targetOffset = with(LocalDensity.current) { 1000.dp.toPx() }
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

    // String resources
    val shareContent = stringResource(Res.string.account_share_content)
    val contactEmail = stringResource(Res.string.about_contact_email)

    ScaffoldWithDimmedOverlay(
        isDimmed = isDimActive.value,
        onDismissDim = { isDimActive.value = false },
        topBar = {
            TopBar(
                title = stringResource(Res.string.drawer_g8),
                navController = navController,
                onClickBackArrow = onClickBack,
                isCancelCtaDisplayed = false
            )
        },
        bottomBar = {
            GeneralBottomBar(
                isButtonNewDisplayed = false,
                navController = navController,
                onClickCategory = onClickCategory,
                onChangeBackground = {
                    isDimActive.value = !isDimActive.value
                },
                isCategoriesMenuOpen = isCategoriesMenuOpen,
                onCategoriesMenuOpenChange = onCategoriesMenuOpenChange,
                showCategoryButton = showCategoryButton
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(
                        top = 130.dp,
                        bottom = 140.dp,
                        start = 40.dp,
                        end = 40.dp,
                    )
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                CollapsibleSection(title = stringResource(Res.string.about_title_website)) {
                    WebsiteText(uriHandler)
                }

                Spacer(modifier = Modifier.height(30.dp))

                CollapsibleSection(title = stringResource(Res.string.about_title_assistance)) {
                    AssistanceText(
                        uriHandler = uriHandler,
                        contactEmail = contactEmail,
                        brush = brush,
                        onComposeEmail = { address, body ->
                            onComposeEmail(address, "[G8] ", body)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                CollapsibleSection(title = stringResource(Res.string.about_title_about)) {
                    Text(
                        modifier = Modifier.padding(bottom = 16.dp),
                        text = stringResource(Res.string.about)
                    )

                    Text(
                        modifier = Modifier.padding(bottom = 12.dp),
                        text = stringResource(Res.string.about_share_text_2)
                    )

                    Button(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .border(
                                BorderStroke(width = 4.dp, brush = brush),
                                shape = RoundedCornerShape(50)
                            ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                        onClick = {
                            onShareContent(shareContent)
                        },
                    ) {
                        Text(stringResource(Res.string.about_share_button))
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                CollapsibleSection(title = stringResource(Res.string.about_title_community)) {
                    CommunityText(uriHandler)
                }

                Spacer(modifier = Modifier.height(30.dp))

                CollapsibleSection(title = stringResource(Res.string.about_title_legal)) {
                    TermsOfService(uriHandler)
                }

                Spacer(modifier = Modifier.height(30.dp))

                CollapsibleSection(title = stringResource(Res.string.about_title_version)) {
                    Text(
                        text = versionName,
                        style = MaterialTheme.typography.textNormalBold,
                    )
                }

                Spacer(modifier = Modifier.height(60.dp))

                // Language section moved to Mon Compte (Account.kt).

                // Bouton "contact" centré juste au-dessus du kaomoji : accès direct au
                // mail support depuis le bas de la page (doublon volontaire avec la
                // section Assistance, qui peut être repliée).
                Button(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .border(
                            BorderStroke(width = 4.dp, brush = brush),
                            shape = RoundedCornerShape(50)
                        ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                    onClick = { onComposeEmail(contactEmail, "[G8] ", "") },
                ) {
                    Text(contactEmail)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Animation chauve-souris (tout en bas, en easter egg).
                val numberOfIterations = remember { mutableIntStateOf(2) }
                var visibleText by remember { mutableStateOf(false) }

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            numberOfIterations.intValue += 1
                            visibleText = !visibleText
                        }
                ) {
                    BatKiss(
                        modifier = Modifier
                            .width(250.dp)
                            .height(170.dp)
                            .align(Alignment.Center),
                        iterations = numberOfIterations.intValue
                    )
                }

                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    visible = visibleText,
                    enter = fadeIn(tween(2000, delayMillis = 100, easing = LinearOutSlowInEasing)),
                    exit = fadeOut(tween(100)),
                ) {
                    Text(
                        text = stringResource(Res.string.account_bat_love),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }

    }
}

@Composable
private fun TermsOfService(uriHandler: UriHandler) {
    val termsHeader = stringResource(Res.string.about_terms_of_service_header)
    val termsUrl = stringResource(Res.string.about_terms_of_service_url_1)
    val termsText = stringResource(Res.string.about_terms_of_service)
    val termsHeader2 = stringResource(Res.string.about_terms_of_service_header_2)
    val policyUrl = stringResource(Res.string.about_terms_of_service_url_2)
    val policyText = stringResource(Res.string.about_privacy)

    val annotatedString = buildAnnotatedString {
        append("$termsHeader ")
        pushStringAnnotation(tag = "terms", annotation = termsUrl)
        withStyle(style = SpanStyle(color = ColorVioletLight)) {
            append("$termsText ")
        }
        pop()
        append("$termsHeader2 ")
        pushStringAnnotation(tag = "policy", annotation = policyUrl)
        withStyle(style = SpanStyle(color = ColorVioletLight)) {
            append(policyText)
        }
        pop()
    }

    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "terms", start = offset, end = offset)
                .firstOrNull()?.let {
                    uriHandler.openUri(it.item)
                }
            annotatedString.getStringAnnotations(tag = "policy", start = offset, end = offset)
                .firstOrNull()?.let {
                    uriHandler.openUri(it.item)
                }
        }
    )
}

@Composable
private fun WebsiteText(uriHandler: UriHandler) {
    val intro = stringResource(Res.string.about_website_intro)
    val cta = stringResource(Res.string.about_website_cta)
    val link = stringResource(Res.string.about_website_link)
    val tail = stringResource(Res.string.about_website_tail)

    val annotatedString = buildAnnotatedString {
        append(intro)
        pushStringAnnotation(tag = "website", annotation = link)
        withStyle(style = SpanStyle(color = ColorVioletLink)) {
            append(cta)
        }
        pop()
        append(tail)
    }

    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "website", start = offset, end = offset)
                .firstOrNull()?.let { uriHandler.openUri(it.item) }
        }
    )
}

@Composable
private fun CommunityText(uriHandler: UriHandler) {
    val communityText = stringResource(Res.string.about_community_text)
    val communityCta = stringResource(Res.string.about_community_cta)
    val communityLink = stringResource(Res.string.about_community_link)

    val annotatedString = buildAnnotatedString {
        append("$communityText ")
        pushStringAnnotation(tag = "forum", annotation = communityLink)
        withStyle(style = SpanStyle(color = ColorVioletLight)) {
            append(communityCta)
        }
        pop()
    }

    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "forum", start = offset, end = offset)
                .firstOrNull()?.let {
                    uriHandler.openUri(it.item)
                }
        }
    )
}

@Composable
private fun AssistanceText(
    uriHandler: UriHandler,
    contactEmail: String,
    brush: Brush,
    onComposeEmail: (address: String, body: String) -> Unit,
) {
    val intro = stringResource(Res.string.about_assistance_intro)
    val moreLead = stringResource(Res.string.about_assistance_more_lead)
    val forumLinkText = stringResource(Res.string.about_assistance_forum_link_text)
    val forumLink = stringResource(Res.string.about_assistance_forum_link)
    val moreMiddle = stringResource(Res.string.about_assistance_more_middle)
    val tutorialsLinkText = stringResource(Res.string.about_assistance_tutorials_link_text)
    val tutorialsLink = stringResource(Res.string.about_assistance_tutorials_link)
    val moreTail = stringResource(Res.string.about_assistance_more_tail)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.padding(bottom = 16.dp),
            text = intro,
        )

        Button(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .border(
                    BorderStroke(width = 4.dp, brush = brush),
                    shape = RoundedCornerShape(50)
                ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
            onClick = { onComposeEmail(contactEmail, "") },
        ) {
            Text(contactEmail)
        }

        Spacer(modifier = Modifier.height(16.dp))

        val annotatedString = buildAnnotatedString {
            append(moreLead)
            pushStringAnnotation(tag = "forum", annotation = forumLink)
            withStyle(style = SpanStyle(color = ColorVioletLight)) {
                append(forumLinkText)
            }
            pop()
            append(moreMiddle)
            pushStringAnnotation(tag = "tutorials", annotation = tutorialsLink)
            withStyle(style = SpanStyle(color = ColorVioletLight)) {
                append(tutorialsLinkText)
            }
            pop()
            append(moreTail)
        }

        ClickableText(
            text = annotatedString,
            style = MaterialTheme.typography.bodyLarge,
            onClick = { offset ->
                annotatedString.getStringAnnotations(tag = "forum", start = offset, end = offset)
                    .firstOrNull()?.let { uriHandler.openUri(it.item) }
                annotatedString.getStringAnnotations(tag = "tutorials", start = offset, end = offset)
                    .firstOrNull()?.let { uriHandler.openUri(it.item) }
            }
        )
    }
}

// LanguageSelector moved to Account.kt (Mon Compte → section "Langue").
