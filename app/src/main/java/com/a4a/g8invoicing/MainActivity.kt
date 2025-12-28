package com.a4a.g8invoicing

import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.a4a.g8invoicing.ui.MainCompose
import com.a4a.g8invoicing.ui.theme.G8InvoicingTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Gives more detail on the "A resource failed to call close." error
        StrictMode.setVmPolicy(
            VmPolicy.Builder(StrictMode.getVmPolicy())
                .detectLeakedClosableObjects()
                .build()
        )

        // Compulsory for the bottom sheet modal to not overlap native navbar
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            G8InvoicingTheme {
                MainCompose()
            }
        }
    }
}






