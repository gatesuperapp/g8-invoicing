package com.a4a.g8invoicing.di

import com.a4a.g8invoicing.data.DatabaseDriverFactory
import org.koin.dsl.module

val desktopModule = module {
    includes(sharedModule)
    single { DatabaseDriverFactory() }
}
