package com.a4a.g8invoicing.ui.screens.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
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
    content: @Composable (PaddingValues) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        Scaffold(
            topBar = topBar,
            bottomBar = bottomBar
        ) { paddingValues ->
            Box(Modifier.fillMaxSize()) {
                // Contenu principal
                content(paddingValues)

                // Fond sombre (au-dessus du contenu mais en-dessous de la bottom bar)
                if (isDimmed) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = paddingValues.calculateBottomPadding())
                            .fillMaxHeight()
                            .background(Color.Black.copy(alpha = 0.2f))
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onDismissDim() }
                            .zIndex(1f)
                    )
                }
            }
        }
    }
}
