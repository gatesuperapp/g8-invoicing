package com.a4a.g8invoicing.di

import org.koin.core.context.startKoin

fun initKoin() {
    startKoin {
        modules(iosModule)
    }
}
