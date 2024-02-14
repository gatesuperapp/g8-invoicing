package com.a4a.g8invoicing

import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.a4a.g8invoicing.ui.MainCompose
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

/*
        // Compulsory to be able to detect keyboard visibility
        WindowCompat.setDecorFitsSystemWindows(window, false)
*/

        setContent {
            MainCompose()
           // val viewModel: PrepareDBViewModel = hiltViewModel()
        }
    }
}






