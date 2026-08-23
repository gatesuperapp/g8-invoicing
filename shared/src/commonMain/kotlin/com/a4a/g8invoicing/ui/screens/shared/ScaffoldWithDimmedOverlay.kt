package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex

@Composable
fun ScaffoldWithDimmedOverlay(
    isDimmed: Boolean,
    onDismissDim: () -> Unit,
    topBar: @Composable (() -> Unit),
    bottomBar: @Composable (() -> Unit),
    // Mirrors Scaffold's own containerColor default so callers that want the
    // stock Material behaviour don't have to specify it explicitly.
    containerColor: Color = MaterialTheme.colorScheme.background,
    content: @Composable (PaddingValues) -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        Scaffold(
            topBar = topBar,
            bottomBar = bottomBar,
            containerColor = containerColor,
            contentColor = contentColorFor(containerColor),
        ) { paddingValues ->
            content(paddingValues)
            // Scrim is stacked INSIDE the Scaffold so it inherits the same
            // paddingValues as content — it covers everything the content
            // covers and stops exactly at the bottom bar's top edge.
            if (isDimmed) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(bottom = paddingValues.calculateBottomPadding())
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { onDismissDim() }
                        .zIndex(10f)
                )
            }
        }
    }
}
