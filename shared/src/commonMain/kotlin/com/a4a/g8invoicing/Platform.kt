package com.a4a.g8invoicing

import androidx.compose.runtime.Composable

expect fun getPlatformName(): String

@Composable
expect fun getAppVersion(): String
