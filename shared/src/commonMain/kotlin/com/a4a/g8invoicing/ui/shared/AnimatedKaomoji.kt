package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Little 3-frame kaomoji mascot used across the onboarding wizard. Tapping it
 * plays a short cyclic animation — the flower held in the hand appears to
 * rotate frame-by-frame. Idle state is frame 0; the animation runs to frame 2
 * then wraps back to frame 0.
 *
 * Kept as text (not a drawable) so it stays crisp at any size, doesn't need
 * a light/dark theme variant, and adds zero APK weight.
 */
private val FRAMES = listOf(
    "(´◡‿◡)ノ   ✾",
    "(´◡‿◡)ノ   ❃",
    "(´◡‿◡)ノ   ✾",
    "(´◡‿◡)ノ   ❃",
    "(´◡‿◡)ノ   ✾",
    "(´◡‿◡)ノ   ❃",
)

private val FRAMES2 = listOf(
    "(´❃‿❃)ノ   ",
    "(´✾‿✾)ノ   ",
    "(´❃‿❃)ノ   ",
    "(´✾‿✾)ノ   ",
    "(´❃‿❃)ノ   ",
    "(´✾‿✾)ノ   ",
    "(´❃‿❃)ノ   ",
    "(´✾‿✾)ノ   ",
)

private const val FRAME_DURATION_MS = 240L

/** First-screen variant: waving mascot holding a flower that rotates. */
@Composable
fun AnimatedKaomoji(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 24.sp,
) = AnimatedKaomojiFrames(frames = FRAMES, modifier = modifier, fontSize = fontSize)

/** Thank-you-screen variant: eyes-and-cheeks morph (uses [FRAMES2]). */
@Composable
fun AnimatedKaomojiThanks(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 24.sp,
) = AnimatedKaomojiFrames(frames = FRAMES2, modifier = modifier, fontSize = fontSize)

@Composable
private fun AnimatedKaomojiFrames(
    frames: List<String>,
    modifier: Modifier,
    fontSize: TextUnit,
) {
    var frameIndex by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() }

    // Play the animation once when the composable enters composition — greets
    // the user with a wave without waiting for them to notice the mascot is
    // interactive. Subsequent replays are triggered by tap.
    LaunchedEffect(Unit) {
        for (i in 1 until frames.size) {
            delay(FRAME_DURATION_MS)
            frameIndex = i
        }
        delay(FRAME_DURATION_MS)
        frameIndex = 0
    }

    Text(
        text = frames[frameIndex],
        fontSize = fontSize,
        // Monospace keeps each glyph in a fixed cell so the mascot doesn't
        // resize/shift between frames — even when the eye or flower chars
        // have different intrinsic widths in a proportional font.
        fontFamily = FontFamily.Monospace,
        textAlign = TextAlign.Center,
        modifier = modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
        ) {
            // Restart from frame 0 on each tap so the animation is deterministic
            // — otherwise a mid-animation tap would compound the cycle.
            scope.launch {
                for (i in 1 until frames.size) {
                    delay(FRAME_DURATION_MS)
                    frameIndex = i
                }
                delay(FRAME_DURATION_MS)
                frameIndex = 0
            }
        },
    )
}
