package com.a4a.g8invoicing

import android.app.Application
import androidx.annotation.StringRes
import com.a4a.g8invoicing.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class G8Invoicing : Application() {
    companion object {
        lateinit var instance: G8Invoicing private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        startKoin {
            androidLogger()
            androidContext(this@G8Invoicing)
            modules(appModule)
        }
    }
}

// Used to access R.strings from any class
object Strings {
    fun get(@StringRes stringRes: Int, vararg formatArgs: Any = emptyArray()): String {
        return G8Invoicing.instance.getString(stringRes, *formatArgs)
    }
}
