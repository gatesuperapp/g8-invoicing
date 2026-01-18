package com.a4a.g8invoicing.data.util

import kotlinx.coroutines.CoroutineDispatcher

expect object DispatcherProvider {
    val IO: CoroutineDispatcher
}
