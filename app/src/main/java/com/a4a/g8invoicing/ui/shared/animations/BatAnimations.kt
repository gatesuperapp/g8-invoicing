package com.a4a.g8invoicing.ui.shared.animations

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlinx.coroutines.delay

// 🎨 Palette
private val Dark = Color(0xFF4A3A55)
private val Mid = Color(0xFF6B5A78)
private val Eye = Color.White
private val Blush = Color(0xFFFFB6C1)
private val Nose = Color(0xFFFF8FA3)
private val HeartColor = Color(0xFFFF6B9D)

/**
 * Chauve-souris kawaii - yeux qui clignent au clic
 */
@Composable
fun BatSmilingEyes(
    modifier: Modifier = Modifier,
    iterations: Int = 0
) {
    var eyesClosed by remember { mutableStateOf(false) }

    LaunchedEffect(iterations) {
        if (iterations > 0) {
            eyesClosed = true
            delay(150)
            eyesClosed = false
            delay(100)
            eyesClosed = true
            delay(150)
            eyesClosed = false
        }
    }

    Canvas(modifier = modifier) {
        val p = minOf(size.width, size.height) / 16f
        val ox = (size.width - 16 * p) / 2
        val oy = (size.height - 14 * p) / 2
        drawKawaiiBat(p, ox, oy, eyesClosed = eyesClosed)
    }
}

/**
 * Chauve-souris kawaii - bouche ouverte au clic
 */
@Composable
fun BatOpenMouth(
    modifier: Modifier = Modifier,
    iterations: Int = 0
) {
    var mouthOpen by remember { mutableStateOf(false) }

    LaunchedEffect(iterations) {
        if (iterations > 0) {
            mouthOpen = true
            delay(300)
            mouthOpen = false
            delay(150)
            mouthOpen = true
            delay(300)
            mouthOpen = false
        }
    }

    Canvas(modifier = modifier) {
        val p = minOf(size.width, size.height) / 16f
        val ox = (size.width - 16 * p) / 2
        val oy = (size.height - 14 * p) / 2
        drawKawaiiBat(p, ox, oy, mouthOpen = mouthOpen)
    }
}

/**
 * Chauve-souris kawaii - ailes qui battent au clic
 */
@Composable
fun BatWavyArms(
    modifier: Modifier = Modifier,
    iterations: Int = 0
) {
    var wingsUp by remember { mutableStateOf(false) }

    LaunchedEffect(iterations) {
        if (iterations > 0) {
            repeat(3) {
                wingsUp = true
                delay(120)
                wingsUp = false
                delay(120)
            }
        }
    }

    Canvas(modifier = modifier) {
        val p = minOf(size.width, size.height) / 16f
        val ox = (size.width - 16 * p) / 2
        val oy = (size.height - 14 * p) / 2
        drawKawaiiBat(p, ox, oy, wingsUp = wingsUp)
    }
}

/**
 * Chauve-souris kawaii - envoie un coeur au clic
 */
@Composable
fun BatKiss(
    modifier: Modifier = Modifier,
    iterations: Int = 0
) {
    var showHeart by remember { mutableStateOf(false) }
    var heartOffset by remember { mutableFloatStateOf(0f) }
    var wink by remember { mutableStateOf(false) }

    LaunchedEffect(iterations) {
        if (iterations > 0) {
            showHeart = true
            wink = true
            heartOffset = 0f
            repeat(12) {
                heartOffset += 1f
                delay(70)
            }
            showHeart = false
            wink = false
        }
    }

    Canvas(modifier = modifier) {
        val p = minOf(size.width, size.height) / 20f
        val ox = (size.width - 16 * p) / 2
        val oy = (size.height - 14 * p) / 2 + p * 2

        drawKawaiiBat(p, ox, oy, wink = wink)

        if (showHeart) {
            drawPixelHeart(p, ox + 14 * p, oy + 2 * p - heartOffset * p)
        }
    }
}

/**
 * Dessine la chauve-souris kawaii pixel art
 */
private fun DrawScope.drawKawaiiBat(
    p: Float,
    ox: Float,
    oy: Float,
    eyesClosed: Boolean = false,
    mouthOpen: Boolean = false,
    wingsUp: Boolean = false,
    wink: Boolean = false
) {
    fun px(x: Int, y: Int, color: Color) {
        drawRect(color, Offset(ox + x * p, oy + y * p), Size(p, p))
    }

    // 🦇 Tête (pattern string)
    val head = listOf(
        "....XXXXXX....",
        "...XXXXXXXX...",
        "..XXXXXXXXXX..",
        ".XXXLLLLLLXXX.",
        ".XXLLLLLLLLXX.",
        "XXLLLLLLLLLLXX",
        "XXLLLLLLLLLLXX",
        "XXLLLLLLLLLLXX",
        "XXLLLLLLLLLLXX",
        ".XXLLLLLLLLXX.",
        ".XXXLLLLLLXXX.",
        "..XXXXXXXXXX..",
        "...XXXXXXXX...",
        "....XXXXXX...."
    )

    head.forEachIndexed { y, row ->
        row.forEachIndexed { x, c ->
            when (c) {
                'X' -> px(x, y, Dark)
                'L' -> px(x, y, Mid)
            }
        }
    }

    // 🦇 Ailes
    val wY = if (wingsUp) -1 else 0
    px(0, 6 + wY, Dark); px(1, 7 + wY, Dark); px(2, 8 + wY, Dark)
    px(15, 6 + wY, Dark); px(14, 7 + wY, Dark); px(13, 8 + wY, Dark)

    // 👀 Yeux
    if (eyesClosed) {
        px(5, 6, Dark); px(10, 6, Dark)
    } else if (wink) {
        px(5, 6, Eye)  // Oeil gauche ouvert
        px(10, 6, Dark) // Oeil droit fermé
    } else {
        px(5, 6, Eye); px(10, 6, Eye)
    }

    // 😊 Joues
    px(4, 7, Blush); px(11, 7, Blush)

    // 👃 Nez
    px(7, 8, Nose); px(8, 8, Nose)

    // 😺 Bouche / Crocs
    if (mouthOpen) {
        px(7, 9, Dark); px(8, 9, Dark)
    } else {
        px(6, 9, Eye); px(9, 9, Eye)
    }
}

/**
 * Petit coeur pixel
 */
private fun DrawScope.drawPixelHeart(p: Float, ox: Float, oy: Float) {
    fun px(x: Int, y: Int) {
        drawRect(HeartColor, Offset(ox + x * p, oy + y * p), Size(p, p))
    }
    // Coeur 5x4
    px(1, 0); px(3, 0)
    px(0, 1); px(1, 1); px(2, 1); px(3, 1); px(4, 1)
    px(1, 2); px(2, 2); px(3, 2)
    px(2, 3)
}
