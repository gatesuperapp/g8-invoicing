package com.a4a.g8invoicing.ui.screens

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.Strings
import com.a4a.g8invoicing.ui.navigation.Category
import com.a4a.g8invoicing.ui.navigation.TopBar
import com.a4a.g8invoicing.ui.screens.shared.ScaffoldWithDimmedOverlay
import com.a4a.g8invoicing.ui.shared.BatAnimation
import com.a4a.g8invoicing.ui.shared.GeneralBottomBar
import com.a4a.g8invoicing.ui.theme.ColorHotPink
import com.a4a.g8invoicing.ui.theme.ColorVioletLight
import com.a4a.g8invoicing.ui.theme.ColorVioletLink
import com.a4a.g8invoicing.ui.theme.textNormalBold
import com.a4a.g8invoicing.ui.theme.textTitle
import java.io.File

@Composable
fun About(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickBack: () -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val isDimActive = remember { mutableStateOf(false) }

    var isCategoriesMenuOpen by remember { mutableStateOf(false) }
    var lastBackPressTime by remember { mutableStateOf(0L) }

    BackHandler {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressTime < 2000) {
            (context as? android.app.Activity)?.finish()
        } else {
            lastBackPressTime = currentTime
            isCategoriesMenuOpen = true
        }
    }

    // Export db popup
    var exportedFile: File? = null
    var exportErrorMessage: String? = null
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

    // Version de l'app
    val versionName = remember {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "?"
        } catch (e: Exception) {
            "?"
        }
    }

    ScaffoldWithDimmedOverlay(
        isDimmed = isDimActive.value,
        onDismissDim = { isDimActive.value = false },
        topBar = {
            TopBar(
                title = R.string.appbar_g8,
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
                onCategoriesMenuOpenChange = { isCategoriesMenuOpen = it }
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
                    text = stringResource(id = R.string.about_title_about),
                    style = MaterialTheme.typography.textTitle,
                )

                Text(
                    modifier = Modifier.padding(bottom = 16.dp),
                    text = stringResource(id = R.string.about)
                )

                // Partage dans À propos
                Text(
                    modifier = Modifier.padding(bottom = 12.dp),
                    text = stringResource(id = R.string.about_share_text)
                )

                val shareContent = stringResource(id = R.string.account_share_content)
                Button(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .border(
                            BorderStroke(width = 4.dp, brush = brush),
                            shape = RoundedCornerShape(50)
                        ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareContent)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    },
                ) {
                    Text(stringResource(id = R.string.about_share_button))
                }

                Spacer(modifier = Modifier.height(30.dp))

                // =====================
                // SECTION: Accès anticipé
                // =====================
                Text(
                    modifier = Modifier.padding(bottom = 16.dp),
                    text = stringResource(id = R.string.about_title_early_access),
                    style = MaterialTheme.typography.textTitle,
                )

                Text(
                    modifier = Modifier.padding(bottom = 20.dp),
                    text = stringResource(id = R.string.account_subscribe)
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
                        uriHandler.openUri(Strings.get(R.string.account_donate_link))
                    },
                ) {
                    Text(stringResource(id = R.string.account_donate))
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
                    BatAnimation(
                        modifier = Modifier
                            .width(250.dp)
                            .height(170.dp)
                            .align(Alignment.Center),
                        file = R.raw.bat_kiss_gif,
                        numberOfIteration = numberOfIterations.intValue
                    )
                }

                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    visible = visibleText,
                    enter = fadeIn(tween(2000, delayMillis = 100, easing = LinearOutSlowInEasing)),
                    exit = fadeOut(tween(100)),
                ) {
                    Text(
                        text = Strings.get(R.string.account_bat_love),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // =====================
                // SECTION: Communauté
                // =====================
                Text(
                    modifier = Modifier.padding(bottom = 16.dp),
                    text = stringResource(id = R.string.about_title_community),
                    style = MaterialTheme.typography.textTitle,
                )

                CommunityText(uriHandler)

                Spacer(modifier = Modifier.height(30.dp))

                // =====================
                // SECTION: Assistance
                // =====================
                Text(
                    modifier = Modifier.padding(bottom = 16.dp),
                    text = stringResource(id = R.string.about_title_assistance),
                    style = MaterialTheme.typography.textTitle,
                )

                AssistanceText(uriHandler, context)

                Spacer(modifier = Modifier.height(30.dp))

                // =====================
                // SECTION: Sauvegarde
                // =====================
                Text(
                    modifier = Modifier.padding(bottom = 16.dp),
                    text = stringResource(id = R.string.about_title_backup),
                    style = MaterialTheme.typography.textTitle,
                )

                Text(
                    modifier = Modifier.padding(bottom = 16.dp),
                    text = stringResource(id = R.string.about_backup_text)
                )

                Text(
                    text = stringResource(id = R.string.about_download_database),
                    color = ColorVioletLink,
                    style = MaterialTheme.typography.textNormalBold,
                    modifier = Modifier
                        .padding(bottom = 30.dp)
                        .clickable {
                            exportedFile = try {
                                exportDatabaseToDownloads(context)
                            } catch (e: Exception) {
                                exportErrorMessage = context.getString(
                                    R.string.about_download_database_error,
                                    e.message ?: ""
                                )
                                showExportErrorDialog = true
                                null
                            }
                            if (exportedFile != null) showSendDatabaseByEmailDialog = true
                        }
                )

                // =====================
                // SECTION: Mentions légales
                // =====================
                Text(
                    modifier = Modifier.padding(bottom = 16.dp),
                    text = stringResource(id = R.string.about_title_legal),
                    style = MaterialTheme.typography.textTitle,
                )

                TermsOfService(uriHandler)

                Spacer(modifier = Modifier.height(30.dp))

                // =====================
                // SECTION: Version (en dernier)
                // =====================
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(id = R.string.about_build_version),
                        style = MaterialTheme.typography.textNormalBold
                    )
                    Text(text = versionName)
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }

        if (showSendDatabaseByEmailDialog) {
            exportedFile?.let {
                DatabaseEmailDialog(
                    context = context,
                    onDismiss = { showSendDatabaseByEmailDialog = false },
                    file = it
                )
            }
        }

        if (showExportErrorDialog) {
            AlertDialog(
                onDismissRequest = { showExportErrorDialog = false },
                text = { Text(exportErrorMessage ?: "") },
                confirmButton = {
                    TextButton(onClick = { showExportErrorDialog = false }) {
                        Text(stringResource(id = R.string.ok), color = ColorVioletLink)
                    }
                }
            )
        }
    }
}

@Composable
fun TermsOfService(uriHandler: UriHandler) {
    val annotatedString = buildAnnotatedString {
        append(Strings.get(R.string.about_terms_of_service_header) + " ")
        pushStringAnnotation(
            tag = "terms",
            annotation = Strings.get(R.string.about_terms_of_service_url_1)
        )
        withStyle(style = SpanStyle(color = ColorVioletLight)) {
            append(Strings.get(R.string.about_terms_of_service) + " ")
        }
        pop()
        append(Strings.get(R.string.about_terms_of_service_header_2) + " ")
        pushStringAnnotation(
            tag = "policy",
            annotation = Strings.get(R.string.about_terms_of_service_url_2)
        )
        withStyle(style = SpanStyle(color = ColorVioletLight)) {
            append(Strings.get(R.string.about_privacy))
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
fun CommunityText(uriHandler: UriHandler) {
    val annotatedString = buildAnnotatedString {
        append(Strings.get(R.string.about_community_text) + " ")
        pushStringAnnotation(
            tag = "forum",
            annotation = Strings.get(R.string.about_community_link)
        )
        withStyle(style = SpanStyle(color = ColorVioletLight)) {
            append(Strings.get(R.string.about_community_cta))
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
fun AssistanceText(uriHandler: UriHandler, context: android.content.Context) {
    // Récupérer les infos de debug
    val appVersion = remember {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "?"
        } catch (e: Exception) {
            "?"
        }
    }

    val debugInfo = remember {
        """


---
Infos techniques (ne pas supprimer) :
• Version app : $appVersion
• Android : ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
• Appareil : ${Build.MANUFACTURER} ${Build.MODEL}
• Produit : ${Build.PRODUCT}
        """.trimIndent()
    }

    val annotatedString = buildAnnotatedString {
        append(Strings.get(R.string.about_assistance_text_1) + " ")
        pushStringAnnotation(
            tag = "forum",
            annotation = Strings.get(R.string.about_assistance_forum_link)
        )
        withStyle(style = SpanStyle(color = ColorVioletLight)) {
            append(Strings.get(R.string.about_assistance_forum_link_text))
        }
        pop()
        append(Strings.get(R.string.about_assistance_text_2) + " ")
        pushStringAnnotation(
            tag = "tutorials",
            annotation = Strings.get(R.string.about_assistance_tutorials_link)
        )
        withStyle(style = SpanStyle(color = ColorVioletLight)) {
            append(Strings.get(R.string.about_assistance_tutorials_link_text) + " ")
        }
        pop()
        append(Strings.get(R.string.about_assistance_text_3) + " ")
        pushStringAnnotation(
            tag = "email",
            annotation = Strings.get(R.string.about_contact_email)
        )
        withStyle(style = SpanStyle(color = ColorVioletLight)) {
            append(Strings.get(R.string.about_assistance_email_link_text))
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
                .firstOrNull()?.let {
                    composeEmail(
                        context = context,
                        address = it.item,
                        emailSubject = "[G8] ",
                        emailMessage = debugInfo
                    )
                }
        }
    )
}
