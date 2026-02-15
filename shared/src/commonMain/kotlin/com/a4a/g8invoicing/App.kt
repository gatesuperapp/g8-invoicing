package com.a4a.g8invoicing

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.rememberNavController
import com.a4a.g8invoicing.data.LocaleManager
import com.a4a.g8invoicing.ui.navigation.CategorySidebar
import com.a4a.g8invoicing.ui.navigation.NavGraph
import com.a4a.g8invoicing.ui.screens.ExportPdfPlatform
import com.a4a.g8invoicing.ui.states.InvoiceState
import org.koin.compose.koinInject

/**
 * Shared App composable - entry point for iOS and potentially other platforms.
 * Uses the shared NavGraph for navigation.
 */
@Composable
fun App(
    // Platform-specific callbacks can be passed here
    onSendReminder: (InvoiceState) -> Unit = {},
    localeManager: LocaleManager = koinInject()
) {
    // Initialize locale on first composition
    LaunchedEffect(Unit) {
        localeManager.initializeLocale()
    }

    // Use Crossfade for smooth transition when language changes
    Crossfade(
        targetState = localeManager.currentLanguage,
        animationSpec = tween(300),
        label = "language_transition"
    ) { _ ->
        AppContent(onSendReminder = onSendReminder)
    }
}

@Composable
private fun AppContent(
    onSendReminder: (InvoiceState) -> Unit = {},
) {
    val navController = rememberNavController()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        // Desktop: show sidebar when screen is wide enough (>900dp)
        val isDesktopLayout = maxWidth > 900.dp

        if (isDesktopLayout) {
            Row(modifier = Modifier.fillMaxSize()) {
                CategorySidebar(
                    navController = navController,
                    onClickCategory = { category ->
                        // Navigate to category and set as start destination
                        val currentStartRoute = navController.graph.startDestinationRoute
                        if (currentStartRoute != null) {
                            navController.popBackStack(currentStartRoute, true)
                        }
                        navController.graph.setStartDestination(category.route)
                        navController.navigate(category.route)
                    }
                )
                NavGraph(
                    navController = navController,
                    onSendReminder = onSendReminder,
                    showCategoryButton = false, // Hide on desktop since we have sidebar
                    exportPdfContent = { document, onDismiss ->
                        ExportPdfPlatform(document, onDismiss)
                    }
                )
            }
        } else {
            NavGraph(
                navController = navController,
                onSendReminder = onSendReminder,
                showCategoryButton = true, // Show on mobile
                exportPdfContent = { document, onDismiss ->
                    ExportPdfPlatform(document, onDismiss)
                }
            )
        }
    }
}
