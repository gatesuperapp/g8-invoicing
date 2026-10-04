package com.a4a.g8invoicing.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.shared.animations.BatSmilingEyes
import com.a4a.g8invoicing.ui.shared.animations.LottieMascotHalo

/**
 * Shared "no documents yet" placeholder for the BL / Devis / Avoir tabs.
 * Haloed BatSmilingEyes mascot with a tap-to-toggle CTA; the invoice tab
 * keeps its own `DisplayBatHelperWelcome` for the cycling advice flow.
 */
@Composable
fun EmptyListMascot(advice: String) {
    var textVisible by remember { mutableStateOf(true) }
    val iterations = remember { mutableIntStateOf(1) }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(top = 40.dp)
            .padding(top = 20.dp, start = 40.dp, end = 40.dp, bottom = 20.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        LottieMascotHalo(
            modifier = Modifier
                .size(320.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) {
                    textVisible = !textVisible
                    iterations.intValue += 1
                },
        ) {
            BatSmilingEyes(
                modifier = Modifier
                    .width(110.dp)
                    .height(110.dp),
                iterations = iterations.intValue,
            )
        }
        Crossfade(
            targetState = textVisible,
            animationSpec = tween(durationMillis = 400),
            label = "emptyListAdvice",
            modifier = Modifier.fillMaxWidth(),
        ) { visible ->
            if (visible) {
                Text(
                    text = advice,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
