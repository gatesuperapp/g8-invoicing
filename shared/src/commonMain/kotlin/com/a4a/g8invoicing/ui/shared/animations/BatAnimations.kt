package com.a4a.g8invoicing.ui.shared.animations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.a4a.g8invoicing.shared.resources.Res
import io.github.alexzhirkevich.compottie.LottieAnimation
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.animateLottieCompositionAsState
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun BatSmilingEyes(
    modifier: Modifier = Modifier,
    iterations: Int = 1,
    // Hold the first frame for [initialDelayMs] before starting the loop, so
    // the user sees the mascot's static blinking state land on screen before
    // the animation kicks in. 0 = play immediately (previous behaviour).
    initialDelayMs: Long = 0,
) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/bat_smiling_eyes.json").decodeToString()
        )
    }

    var isPlaying by remember { mutableStateOf(initialDelayMs <= 0L) }
    if (initialDelayMs > 0L) {
        LaunchedEffect(Unit) {
            delay(initialDelayMs)
            isPlaying = true
        }
    }

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations,
        isPlaying = isPlaying
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun BatOpenMouth(
    modifier: Modifier = Modifier,
    iterations: Int = 1
) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/bat_openmouth.json").decodeToString()
        )
    }

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations,
        isPlaying = true
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun BatWavyArms(
    modifier: Modifier = Modifier,
    iterations: Int = 1
) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/bat_wavy_arms.json").decodeToString()
        )
    }

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations,
        isPlaying = true
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun BatKiss(
    modifier: Modifier = Modifier,
    iterations: Int = 1
) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/bat_kiss_gif.json").decodeToString()
        )
    }

    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations,
        isPlaying = true
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier
    )
}
