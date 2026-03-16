package com.a4a.g8invoicing.ui

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.a4a.g8invoicing.data.LocaleManager
import com.a4a.g8invoicing.data.initializeVersionTracking
import com.a4a.g8invoicing.data.setSeenWhatsNew
import com.a4a.g8invoicing.data.shouldShowWhatsNew
import com.a4a.g8invoicing.ui.navigation.NavGraph
import com.a4a.g8invoicing.ui.screens.ExportPdfPlatform
import com.a4a.g8invoicing.ui.screens.ExportResult
import com.a4a.g8invoicing.ui.screens.exportDatabaseToDownloads
import com.a4a.g8invoicing.ui.screens.sendDatabaseByEmail
import com.a4a.g8invoicing.ui.states.InvoiceState
import android.content.Intent
import android.net.Uri
import java.io.File
import com.a4a.g8invoicing.ui.theme.G8InvoicingTheme
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun MainCompose(
    onSendReminder: (InvoiceState) -> Unit = {},
    localeManager: LocaleManager = koinInject()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Initialize locale and version tracking on first composition
    LaunchedEffect(Unit) {
        localeManager.initializeLocale()
        // Pour les nouvelles installations, enregistre la version actuelle
        // Ainsi lors de la prochaine mise à jour, la modale WhatsNew s'affichera
        initializeVersionTracking(context)
    }

    // What's New dialog state
    val shouldShow by shouldShowWhatsNew(context).collectAsState(initial = false)
    var showWhatsNew by remember { mutableStateOf(false) }

    // Update showWhatsNew when shouldShow changes
    LaunchedEffect(shouldShow) {
        if (shouldShow) {
            showWhatsNew = true
        }
    }

    G8InvoicingTheme {
        // Use Crossfade for smooth transition when language changes
        Crossfade(
            targetState = localeManager.currentLanguage,
            animationSpec = tween(300),
            label = "language_transition"
        ) { _ ->
            val navController: NavHostController = rememberNavController()

            Surface(
                modifier = Modifier.fillMaxSize()
            ) {
                NavGraph(
                    navController = navController,
                    onSendReminder = onSendReminder,
                    exportPdfContent = { document, onDismiss ->
                        ExportPdfPlatform(document, onDismiss)
                    },
                    showWhatsNew = showWhatsNew,
                    onWhatsNewDismissed = {
                        showWhatsNew = false
                        coroutineScope.launch {
                            setSeenWhatsNew(context)
                        }
                    },
                    onShareContent = { content ->
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, content)
                        }
                        context.startActivity(Intent.createChooser(intent, null))
                    },
                    onExportDatabase = {
                        try {
                            val file = exportDatabaseToDownloads(context)
                            ExportResult.Success(file.absolutePath)
                        } catch (e: Exception) {
                            ExportResult.Error(e.message ?: "Unknown error")
                        }
                    },
                    onSendDatabaseByEmail = { filePath ->
                        sendDatabaseByEmail(context, File(filePath))
                    },
                    onComposeEmail = { address, subject, body ->
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
                            putExtra(Intent.EXTRA_SUBJECT, subject)
                            putExtra(Intent.EXTRA_TEXT, body)
                        }
                        if (intent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(intent)
                        }
                    }
                )
            }
        }
    }
}
