package com.a4a.g8invoicing.ui.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
actual fun keyboardAsState(): State<Keyboard> {
    // TODO: Implement proper keyboard detection for iOS
    // For now, return a static Closed state
    return remember { mutableStateOf(Keyboard.Closed) }
}
