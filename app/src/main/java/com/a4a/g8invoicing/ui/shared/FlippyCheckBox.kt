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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.a4a.g8invoicing.ui.navigation.AppBarAction
import com.a4a.g8invoicing.ui.theme.ColorGreenPaid

@Composable
fun FlippyCheckBox(
    action: AppBarAction,
    onItemCheckboxClick: (Boolean) -> Unit = {},
    ) {
    var cardState by remember {
        mutableStateOf(CardFace.Back)
    }

    val checkedState = remember { mutableStateOf(false) }

    FlipCard(
        cardFace = cardState,
        onClick = {
            onItemCheckboxClick(true)
            cardState = it.next
            checkedState.value = true
        },
        axis = RotationAxis.AxisY,
        back = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(action.iconColor ?: MaterialTheme.colorScheme.onBackground)
            )
        },
        front = {
            Surface {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Checkbox(
                        //  modifier = Modifier.background(Color.Yellow),
                        checked = checkedState.value,
                        onCheckedChange =
                        {
                            onItemCheckboxClick(false)
                            checkedState.value = false
                            cardState = CardFace.Back
                        },
                    )
                }
            }
        }
    )
}

@Composable
fun FlipCard(
    cardFace: CardFace,
    onClick: (CardFace) -> Unit,
    modifier: Modifier = Modifier,
    axis: RotationAxis = RotationAxis.AxisY,
    back: @Composable () -> Unit = {},
    front: @Composable () -> Unit = {},
) {
    val rotation = animateFloatAsState(
        targetValue = cardFace.angle,
        animationSpec = tween(
            durationMillis = 400,
            easing = FastOutSlowInEasing,
        )
    )
    Card(
        onClick = { onClick(cardFace) },
        modifier = modifier
            .padding(12.dp)
            .border(
                width = 2.dp,
                color = Color.Transparent,
                shape = CircleShape
            )
            .height(24.dp)
            .width(24.dp)

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
                Modifier.fillMaxSize()
            ) {
                front()
            }
        } else {
            Box(
                Modifier
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

enum class CardFace(val angle: Float) {
    Front(0f) {
        override val next: CardFace
            get() = Back
    },
    Back(180f) {
        override val next: CardFace
            get() = Front
    };

    abstract val next: CardFace
}

enum class RotationAxis {
    AxisX,
    AxisY,
}