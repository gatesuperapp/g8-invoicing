package com.a4a.g8invoicing.ui.shared

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.theme.ColorGrayTransp

@Composable
fun FlippyCheckBox(
    fillColor: Color?,
    onItemCheckboxClick: (Boolean) -> Unit = {},
    checkboxFace: CheckboxFace,
    checkedState: Boolean,
    displayBorder: Boolean = true
) {
    FlipCard(
        cardFace = checkboxFace,
        onClick = {
            onItemCheckboxClick(true)
        },
        axis = RotationAxis.AxisY,
        back = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(fillColor ?: Color.White)
            )
        },
        front = {
            Surface {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Checkbox(
                        checked = checkedState,
                        onCheckedChange = {
                            onItemCheckboxClick(false)
                        },
                    )
                }
            }
        },
        displayBorder = displayBorder
    )
}

@Composable
fun FlipCard(
    cardFace: CheckboxFace,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    axis: RotationAxis = RotationAxis.AxisY,
    back: @Composable () -> Unit = {},
    front: @Composable () -> Unit = {},
    displayBorder: Boolean
) {
    val rotation = animateFloatAsState(
        targetValue = cardFace.angle,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing,
        ), label = ""
    )
    Card(
        onClick = { onClick() },
        modifier = modifier
            .padding(12.dp)
            .padding(
                //to increase the click touch zone
                start = 20.dp,
            )
            .height(16.dp)
            .width(16.dp)
            .graphicsLayer {
                if (axis == RotationAxis.AxisX) {
                    rotationX = rotation.value
                } else {
                    rotationY = rotation.value
                }
                cameraDistance = 12f * density
            },
    ) {
        if (rotation.value <= 90f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .border(
                        width = 1.5.dp,
                        color = Color.Transparent,
                        shape = CircleShape
                    )
            ) {
                front()
            }
        } else {
            Box(
                Modifier
                    .border(
                        width = 1.5.dp,
                        color = if(displayBorder) ColorGrayTransp else Color.Transparent,
                        shape = CircleShape
                    )
                    .background(Color.White)
                    .fillMaxSize()
                    .graphicsLayer {
                        if (axis == RotationAxis.AxisX) {
                            rotationX = 180f
                        } else {
                            rotationY = 180f
                        }
                    },
            ) {
                back()
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

enum class RotationAxis {
    AxisX,
    AxisY,
}