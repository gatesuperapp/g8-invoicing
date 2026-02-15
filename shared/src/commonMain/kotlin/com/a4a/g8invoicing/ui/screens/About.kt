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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import com.a4a.g8invoicing.shared.resources.about_assistance_email_link_text
import com.a4a.g8invoicing.shared.resources.about_assistance_forum_link
import com.a4a.g8invoicing.shared.resources.about_assistance_forum_link_text
import com.a4a.g8invoicing.shared.resources.about_assistance_text_1
import com.a4a.g8invoicing.shared.resources.about_assistance_text_2
import com.a4a.g8invoicing.shared.resources.about_assistance_text_3
import com.a4a.g8invoicing.shared.resources.about_assistance_tutorials_link
import com.a4a.g8invoicing.shared.resources.about_assistance_tutorials_link_text
import com.a4a.g8invoicing.shared.resources.about_backup_text
import com.a4a.g8invoicing.shared.resources.about_build_version
import com.a4a.g8invoicing.shared.resources.about_community_cta
import com.a4a.g8invoicing.shared.resources.about_community_link
import com.a4a.g8invoicing.shared.resources.about_community_text
import com.a4a.g8invoicing.shared.resources.about_contact_email
import com.a4a.g8invoicing.shared.resources.about_donate_button
import com.a4a.g8invoicing.shared.resources.about_donate_link
import com.a4a.g8invoicing.shared.resources.about_download_database
import com.a4a.g8invoicing.shared.resources.about_privacy
import com.a4a.g8invoicing.shared.resources.about_share_button
import com.a4a.g8invoicing.shared.resources.about_share_text
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
import com.a4a.g8invoicing.shared.resources.about_title_early_access
import com.a4a.g8invoicing.shared.resources.about_title_language
import com.a4a.g8invoicing.shared.resources.about_title_legal
import com.a4a.g8invoicing.shared.resources.about_language_english
import com.a4a.g8invoicing.shared.resources.about_language_french
import com.a4a.g8invoicing.shared.resources.about_language_german
import com.a4a.g8invoicing.shared.resources.about_language_system
import com.a4a.g8invoicing.shared.resources.account_bat_love
import com.a4a.g8invoicing.shared.resources.account_donate
import com.a4a.g8invoicing.shared.resources.account_donate_link
import com.a4a.g8invoicing.shared.resources.account_share_content
import com.a4a.g8invoicing.shared.resources.account_subscribe
import com.a4a.g8invoicing.shared.resources.appbar_g8
import com.a4a.g8invoicing.shared.resources.ok
import com.a4a.g8invoicing.data.AppLanguage
import com.a4a.g8invoicing.data.LocaleManager
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.screens.shared.ScaffoldWithDimmedOverlay
import com.a4a.g8invoicing.ui.shared.GeneralBottomBar
import com.a4a.g8invoicing.ui.shared.animations.BatKiss
import com.a4a.g8invoicing.ui.theme.ColorHotPink
import com.a4a.g8invoicing.ui.theme.ColorVioletLight
import com.a4a.g8invoicing.ui.theme.ColorVioletLink
import com.a4a.g8invoicing.ui.theme.textNormalBold
import com.a4a.g8invoicing.ui.theme.textTitle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

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
    onExportDatabase: () -> ExportResult = { ExportResult.Error("Not implemented") },
    onSendDatabaseByEmail: (String) -> Unit = {},
    onComposeEmail: (address: String, subject: String, body: String) -> Unit = { _, _, _ -> },
    // Callbacks for categories menu state (managed by NavGraph for BackHandler)
    isCategoriesMenuOpen: Boolean = false,
    onCategoriesMenuOpenChange: (Boolean) -> Unit = {},
    showCategoryButton: Boolean = true,
) {
    val uriHandler = LocalUriHandler.current
    val isDimActive = remember { mutableStateOf(false) }

    // Export db popup
    var exportedFilePath by remember { mutableStateOf<String?>(null) }
    var exportErrorMessage by remember { mutableStateOf<String?>(null) }
    var showExportErrorDialog by remember { mutableStateOf(false) }
    var showSendDatabaseByEmailDialog by remember { mutableStateOf(false) }

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
    val donateLinkAbout = stringResource(Res.string.about_donate_link)
    val donateLinkAccount = stringResource(Res.string.account_donate_link)
    val shareContent = stringResource(Res.string.account_share_content)
    val contactEmail = stringResource(Res.string.about_contact_email)

    ScaffoldWithDimmedOverlay(
        isDimmed = isDimActive.value,
        onDismissDim = { isDimActive.value = false },
        topBar = {
            TopBar(
                title = stringResource(Res.string.appbar_g8),
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
            modifier = Modifier.fillMaxSize()
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
                // =====================
                // SECTION: À propos
                // =====================
                Text(
                    modifier = Modifier.padding(bottom = 16.dp),
                    text = stringResource(Res.string.about_title_about),
                    style = MaterialTheme.typography.textTitle,
                )

                Text(
                    modifier = Modifier.padding(bottom = 16.dp),
                    text = stringResource(Res.string.about)
                )

                // Donation dans À propos
                Text(
                    modifier = Modifier.padding(bottom = 12.dp),
                    text = stringResource(Res.string.about_share_text)
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
                        uriHandler.openUri(donateLinkAbout)
                    },
                ) {
                    Text(stringResource(Res.string.about_donate_button))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Partage dans À propos
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

                Spacer(modifier = Modifier.height(30.dp))

                // =====================
                // SECTION: Accès anticipé
                // =====================
                Text(
                    modifier = Modifier.padding(bottom = 16.dp),
                    text = stringResource(Res.string.about_title_early_access),
                    style = MaterialTheme.typography.textTitle,
                )

                Text(
                    modifier = Modifier.padding(bottom = 20.dp),
                    text = stringResource(Res.string.account_subscribe)
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
                        uriHandler.openUri(donateLinkAccount)
                    },
                ) {
                    Text(stringResource(Res.string.account_donate))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Animation chauve-souris
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

                Spacer(modifier = Modifier.height(20.dp))

                // =====================
                // SECTION: Communauté
                // =====================
                Text(
                    modifier = Modifier.padding(bottom = 16.dp),
                    text = stringResource(Res.string.about_title_community),
                    style = MaterialTheme.typography.textTitle,
                )

                CommunityText(uriHandler)

                Spacer(modifier = Modifier.height(30.dp))

                // =====================
                // SECTION: Assistance
                // =====================
                Text(
                    modifier = Modifier.padding(bottom = 16.dp),
                    text = stringResource(Res.string.about_title_assistance),
                    style = MaterialTheme.typography.textTitle,
                )

                AssistanceText(
                    uriHandler = uriHandler,
                    onComposeEmail = { address, body ->
                        onComposeEmail(address, "[G8] ", body)
                    }
                )

                Spacer(modifier = Modifier.height(30.dp))

                // =====================
                // SECTION: Sauvegarde
                // =====================
                Text(
                    modifier = Modifier.padding(bottom = 16.dp),
                    text = stringResource(Res.string.about_title_backup),
                    style = MaterialTheme.typography.textTitle,
                )

                Text(
                    modifier = Modifier.padding(bottom = 16.dp),
                    text = stringResource(Res.string.about_backup_text)
                )

                Text(
                    text = stringResource(Res.string.about_download_database),
                    color = ColorVioletLink,
                    style = MaterialTheme.typography.textNormalBold,
                    modifier = Modifier
                        .padding(bottom = 30.dp)
                        .clickable {
                            val result = onExportDatabase()
                            when (result) {
                                is ExportResult.Success -> {
                                    exportedFilePath = result.filePath
                                    showSendDatabaseByEmailDialog = true
                                }
                                is ExportResult.Error -> {
                                    exportErrorMessage = result.message
                                    showExportErrorDialog = true
                                }
                            }
                        }
                )

                // =====================
                // SECTION: Mentions légales
                // =====================
                Text(
                    modifier = Modifier.padding(bottom = 16.dp),
                    text = stringResource(Res.string.about_title_legal),
                    style = MaterialTheme.typography.textTitle,
                )

                TermsOfService(uriHandler)

                Spacer(modifier = Modifier.height(30.dp))

                // =====================
                // SECTION: Langue
                // =====================
                Text(
                    modifier = Modifier.padding(bottom = 16.dp),
                    text = stringResource(Res.string.about_title_language),
                    style = MaterialTheme.typography.textTitle,
                )

                LanguageSelector()

                Spacer(modifier = Modifier.height(30.dp))

                // =====================
                // SECTION: Version (en dernier)
                // =====================
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.about_build_version),
                        style = MaterialTheme.typography.textNormalBold
                    )
                    Text(text = versionName)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        if (showSendDatabaseByEmailDialog && exportedFilePath != null) {
            DatabaseEmailDialogShared(
                onDismiss = { showSendDatabaseByEmailDialog = false },
                onConfirm = {
                    showSendDatabaseByEmailDialog = false
                    exportedFilePath?.let { onSendDatabaseByEmail(it) }
                }
            )
        }

        if (showExportErrorDialog) {
            AlertDialog(
                onDismissRequest = { showExportErrorDialog = false },
                text = { Text(exportErrorMessage ?: "") },
                confirmButton = {
                    TextButton(onClick = { showExportErrorDialog = false }) {
                        Text(stringResource(Res.string.ok), color = ColorVioletLink)
                    }
                }
            )
        }
    }
}

@Composable
private fun DatabaseEmailDialogShared(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    // Simplified dialog - strings will be added if needed
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Envoyer par email ?") },
        text = { Text("Voulez-vous envoyer la base de données par email pour la sauvegarder ?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Oui", color = ColorVioletLink)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Non", color = ColorVioletLink)
            }
        }
    )
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
    onComposeEmail: (address: String, body: String) -> Unit
) {
    val text1 = stringResource(Res.string.about_assistance_text_1)
    val forumLinkText = stringResource(Res.string.about_assistance_forum_link_text)
    val forumLink = stringResource(Res.string.about_assistance_forum_link)
    val text2 = stringResource(Res.string.about_assistance_text_2)
    val tutorialsLinkText = stringResource(Res.string.about_assistance_tutorials_link_text)
    val tutorialsLink = stringResource(Res.string.about_assistance_tutorials_link)
    val text3 = stringResource(Res.string.about_assistance_text_3)
    val emailLinkText = stringResource(Res.string.about_assistance_email_link_text)
    val emailAddress = stringResource(Res.string.about_contact_email)

    val annotatedString = buildAnnotatedString {
        append("$text1 ")
        pushStringAnnotation(tag = "forum", annotation = forumLink)
        withStyle(style = SpanStyle(color = ColorVioletLight)) {
            append(forumLinkText)
        }
        pop()
        append("$text2 ")
        pushStringAnnotation(tag = "tutorials", annotation = tutorialsLink)
        withStyle(style = SpanStyle(color = ColorVioletLight)) {
            append("$tutorialsLinkText ")
        }
        pop()
        append("$text3 ")
        pushStringAnnotation(tag = "email", annotation = emailAddress)
        withStyle(style = SpanStyle(color = ColorVioletLight)) {
            append(emailLinkText)
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
            annotatedString.getStringAnnotations(tag = "tutorials", start = offset, end = offset)
                .firstOrNull()?.let {
                    uriHandler.openUri(it.item)
                }
            annotatedString.getStringAnnotations(tag = "email", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    // Debug info will be added by the platform-specific implementation
                    onComposeEmail(annotation.item, "")
                }
        }
    )
}

@Composable
private fun LanguageSelector(
    localeManager: LocaleManager = koinInject()
) {
    var expanded by remember { mutableStateOf(false) }
    val currentLanguage = localeManager.currentLanguage

    // String resources for language names
    val systemLabel = stringResource(Res.string.about_language_system)
    val frenchLabel = stringResource(Res.string.about_language_french)
    val englishLabel = stringResource(Res.string.about_language_english)
    val germanLabel = stringResource(Res.string.about_language_german)

    fun getDisplayName(language: AppLanguage): String = when (language) {
        AppLanguage.SYSTEM -> systemLabel
        AppLanguage.FRENCH -> frenchLabel
        AppLanguage.ENGLISH -> englishLabel
        AppLanguage.GERMAN -> germanLabel
    }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = Color.Gray,
                    shape = RoundedCornerShape(4.dp)
                )
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = getDisplayName(currentLanguage),
                color = Color.Black
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.Gray
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            AppLanguage.values().forEach { language ->
                DropdownMenuItem(
                    text = { Text(getDisplayName(language)) },
                    onClick = {
                        localeManager.setLanguage(language)
                        expanded = false
                    }
                )
            }
        }
    }
}
