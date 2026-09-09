package com.a4a.g8invoicing.ui.shared

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlin.math.sin
import kotlin.random.Random

/**
 * Continuous confetti fall used on onboarding completion. Particles spawn at
 * the top edge, fall down through the full canvas height with slight
 * horizontal wobble, and respawn at the top when they exit the bottom. Runs
 * for [durationMs] then stops emitting; particles still in-flight complete
 * their fall.
 */
@Composable
fun ConfettiBurst(
    modifier: Modifier = Modifier,
    particleCount: Int = 90,
    durationMs: Int = 5000,
) {
    // Each particle carries its ballistic parameters + spin. All positions
    // are parameterised on elapsed time so the burst behaves the same at
    // any frame rate.
    data class Particle(
        val startX: Float,      // 0..1 across canvas width, at spawn
        val fallSpeed: Float,   // canvas heights per second (0.2..0.55)
        val size: Float,        // px
        val color: Color,
        val wobbleFreq: Float,  // hz — how fast the horizontal drift oscillates
        val wobbleAmp: Float,   // fraction of canvas width
        val phase: Float,       // random phase offset so particles don't sync
        val spawnDelay: Float,  // 0..1 fraction of durationMs before appearing
    )

    val palette = listOf(
        Color(0xFF932092), // brand violet
        Color(0xFFEDE7F6), // pale lavender
        Color(0xFF12B76A), // green
        Color(0xFFF6C90E), // yellow
        Color(0xFFDC2A2A), // red
        Color(0xFF7E96B8), // dusty blue
    )

    val particles = remember {
        List(particleCount) { i ->
            val rng = Random(i * 7919L + 13L)
            Particle(
                startX = rng.nextFloat(),
                fallSpeed = 0.2f + rng.nextFloat() * 0.35f,
                size = 6f + rng.nextFloat() * 8f,
                color = palette[i % palette.size],
                wobbleFreq = 0.6f + rng.nextFloat() * 1.4f,
                wobbleAmp = 0.03f + rng.nextFloat() * 0.05f,
                phase = rng.nextFloat() * 6.28f,
                spawnDelay = rng.nextFloat() * 0.6f,
            )
        }
    }

    var elapsedMs by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        // Keep ticking a little past durationMs so late-spawning particles
        // still have time to fall through the screen.
        val runFor = durationMs + 2500L
        while (elapsedMs < runFor) {
            val now = withFrameNanos { it }
            elapsedMs = ((now - start) / 1_000_000L).coerceAtMost(runFor)
        }
    }

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val t = elapsedMs / 1000f
            val w = size.width
            val h = size.height
            particles.forEach { p ->
                val localT = t - p.spawnDelay * (durationMs / 1000f)
                if (localT < 0f) return@forEach
                // Loop each particle: once it exits the bottom, wrap back
                // to the top so the fall looks continuous while durationMs
                // is still counting.
                val fallProgress = localT * p.fallSpeed
                val y = (fallProgress % 1.2f) - 0.1f
                if (y < -0.05f || y > 1.05f) return@forEach
                // Stop spawning once we're past durationMs — particles
                // already in flight keep falling until they clear the bottom.
                val spawnCutoff = durationMs / 1000f
                if (localT > spawnCutoff && y < 0f) return@forEach

                val x = p.startX + sin(p.phase + localT * p.wobbleFreq * 6.28f) * p.wobbleAmp
                val cx = x * w
                val cy = y * h
                drawRect(
                    color = p.color,
                    topLeft = Offset(cx - p.size / 2f, cy - p.size / 2f),
                    size = Size(p.size, p.size),
                )
            }
        }
    }
}
