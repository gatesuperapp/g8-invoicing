package com.a4a.g8invoicing.ui.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

enum class Keyboard {
    Opened, Closed
}

@Composable
expect fun keyboardAsState(): State<Keyboard>
