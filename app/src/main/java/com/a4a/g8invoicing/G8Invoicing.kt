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
        // Snapshot the raw system region BEFORE anything (Koin, LocaleManager)
        // can call Locale.setDefault(). On Android 13+ Locale.Category.FORMAT
        // reflects Settings → System → Languages → Regional preferences → Region,
        // which is what we want to gate on (independent of the UI language the
        // user picked). Resources.getSystem().configuration.locales only carries
        // the Language picker choice — not the Region setting.
        SystemRegionSnapshot.formatCountry =
            java.util.Locale.getDefault(java.util.Locale.Category.FORMAT).country

        super.onCreate()
        instance = this

        startKoin {
            androidLogger()
            androidContext(this@G8Invoicing)
            modules(appModule)
        }
    }
}

/** Frozen at Application.onCreate — see [G8Invoicing.onCreate]. */
object SystemRegionSnapshot {
    var formatCountry: String = ""
        internal set
}

// Used to access R.strings from any class
object Strings {
    fun get(@StringRes stringRes: Int, vararg formatArgs: Any = emptyArray()): String {
        return G8Invoicing.instance.getString(stringRes, *formatArgs)
    }
}
