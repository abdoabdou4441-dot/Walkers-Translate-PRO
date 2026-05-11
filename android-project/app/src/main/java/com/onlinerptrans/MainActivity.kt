package com.onlinerptrans

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat
import com.onlinerptrans.ui.screen.MainScreen
import com.onlinerptrans.ui.theme.GamingBlack
import com.onlinerptrans.ui.theme.OnlineRPTransTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    // Listen for service stop events sent from notification action
    private val serviceStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_SERVICE_STARTED -> viewModel.onServiceStateChanged(true)
                ACTION_SERVICE_STOPPED -> viewModel.onServiceStateChanged(false)
            }
        }
    }

    companion object {
        const val ACTION_SERVICE_STARTED = "com.onlinerptrans.SERVICE_STARTED"
        const val ACTION_SERVICE_STOPPED = "com.onlinerptrans.SERVICE_STOPPED"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Force dark status bar icons off (light icons on black bg)
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false

        val filter = IntentFilter().apply {
            addAction(ACTION_SERVICE_STARTED)
            addAction(ACTION_SERVICE_STOPPED)
        }
        registerReceiver(serviceStateReceiver, filter, RECEIVER_NOT_EXPORTED)

        setContent {
            OnlineRPTransTheme {
                // Force RTL for the entire app (Arabic UI)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color    = GamingBlack
                    ) {
                        MainScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check overlay permission each time user returns (e.g. from Settings)
        viewModel.refreshPermissionState(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(serviceStateReceiver)
    }
}
