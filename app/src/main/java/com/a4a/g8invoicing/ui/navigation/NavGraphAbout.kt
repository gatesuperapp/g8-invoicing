package com.a4a.g8invoicing.ui.navigation

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.a4a.g8invoicing.R
import com.a4a.g8invoicing.ui.screens.About
import com.a4a.g8invoicing.ui.screens.ExportResult
import com.a4a.g8invoicing.ui.screens.exportDatabaseToDownloads
import java.io.File

fun NavGraphBuilder.about(
    navController: NavController,
    onClickCategory: (Category) -> Unit,
    onClickBack: () -> Unit,
) {
    composable(route = Screen.About.name) {
        val context = LocalContext.current
        var isCategoriesMenuOpen by remember { mutableStateOf(false) }
        var lastBackPressTime by remember { mutableStateOf(0L) }

        // Handle system back button (Android-specific)
        BackHandler {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBackPressTime < 2000) {
                (context as? android.app.Activity)?.finish()
            } else {
                lastBackPressTime = currentTime
                isCategoriesMenuOpen = true
            }
        }

        // Get version name
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

        // Debug info for email
        val debugInfo = remember {
            """


---
Infos techniques (ne pas supprimer) :
• Version app : $versionName
• Android : ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})
• Appareil : ${Build.MANUFACTURER} ${Build.MODEL}
• Produit : ${Build.PRODUCT}
            """.trimIndent()
        }

        About(
            navController = navController,
            onClickCategory = onClickCategory,
            onClickBack = onClickBack,
            versionName = versionName,
            onShareContent = { content ->
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, content)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                context.startActivity(shareIntent)
            },
            onExportDatabase = {
                try {
                    val file = exportDatabaseToDownloads(context)
                    ExportResult.Success(file.absolutePath)
                } catch (e: Exception) {
                    ExportResult.Error(
                        context.getString(
                            R.string.about_download_database_error,
                            e.message ?: ""
                        )
                    )
                }
            },
            onSendDatabaseByEmail = { filePath ->
                val file = File(filePath)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/octet-stream"
                    putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.database_email_subject))
                    putExtra(Intent.EXTRA_TEXT, context.getString(R.string.database_email_body))
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(
                    Intent.createChooser(
                        intent,
                        context.getString(R.string.database_email_chooser)
                    )
                )
            },
            onComposeEmail = { address, subject, body ->
                val fullBody = body + debugInfo
                try {
                    val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
                    intent.selector = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
                    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
                    intent.putExtra(Intent.EXTRA_SUBJECT, subject)
                    intent.putExtra(Intent.EXTRA_TEXT, fullBody)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Fallback if no email client
                }
            },
            isCategoriesMenuOpen = isCategoriesMenuOpen,
            onCategoriesMenuOpenChange = { isCategoriesMenuOpen = it }
        )
    }
}