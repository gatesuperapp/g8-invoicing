package com.a4a.g8invoicing.ui.shared

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.theme.ColorGrayTransp

@Composable
fun FlippyCheckBox(
    fillColorWhenSelectionOff: Color?,
    backgroundColorWhenSelectionOn: Color? = null,
    onItemCheckboxClick: () -> Unit = {},
    checkboxFace: CheckboxFace,
    checkedState: Boolean,
    displayBorder: Boolean = true,
) {
    val rotation = animateFloatAsState(
        targetValue = checkboxFace.angle,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing,
        ), label = ""
    )

    Card(
        modifier = Modifier
            .padding(12.dp)
            .padding(
                //to increase the click touch zone
                start = 20.dp,
            )
            .height(16.dp)
            .width(16.dp)
            .graphicsLayer {
                rotationY = rotation.value
                cameraDistance = 12f * density
            }
    ) {
        if (rotation.value <= 90f) {
            Row(
                Modifier
                    .fillMaxSize()
                    .border(
                        width = 1.5.dp,
                        color = Color.Transparent,
                        shape = CircleShape
                    )
            ) {
                MaterialTheme(
                    colorScheme = lightColorScheme(
                        primary = backgroundColorWhenSelectionOn ?: Color.White,
                        onPrimary = Color.Black,
                    )
                ) {
                    Checkbox(
                        checked = checkedState,
                        onCheckedChange = {
                            onItemCheckboxClick()
                        },
                    )
                }
            }
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .clickable {
                        onItemCheckboxClick()
                    }
                    .border(
                        width = 1.5.dp,
                        color = if (displayBorder) ColorGrayTransp else Color.Transparent,
                        shape = CircleShape
                    )
                    .graphicsLayer {
                        rotationY = 180f
                    },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(fillColorWhenSelectionOff ?: Color.White)
                )
            }
        }
    }
}

enum class CheckboxFace(val angle: Float) {
    Front(0f) {
        override val next: CheckboxFace
            get() = Back
    },
    Back(180f) {
        override val next: CheckboxFace
            get() = Front
    };

    abstract val next: CheckboxFace
}
