package com.a4a.g8invoicing

import android.app.Application
import androidx.annotation.StringRes
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class G8Invoicing : Application() {
    companion object {
        lateinit var instance: G8Invoicing private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}

// Used to access R.strings from any class
object Strings {
    fun get(@StringRes stringRes: Int, vararg formatArgs: Any = emptyArray()): String {
        return G8Invoicing.instance.getString(stringRes, *formatArgs)
    }
}
