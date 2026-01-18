package com.a4a.g8invoicing.data.util

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

actual object DispatcherProvider {
    actual val IO: CoroutineDispatcher = Dispatchers.Default
}
