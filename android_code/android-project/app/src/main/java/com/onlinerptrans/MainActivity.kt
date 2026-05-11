package com.onlinerptrans

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.onlinerptrans.ui.screen.MainScreen
import com.onlinerptrans.ui.theme.GamingBlack
import com.onlinerptrans.ui.theme.OnlineRPTransTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    // ── Screen capture permission launcher ────────────────────────────────────
    private val screenCaptureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            // User approved — hand credentials to ViewModel which starts the service
            viewModel.startServiceWithProjection(this, result.resultCode, result.data!!)
        }
        // If denied, the button simply returns to its idle state; no crash
    }

    // ── Service state receiver ────────────────────────────────────────────────
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

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false

        // Register broadcast receiver for service state updates
        val filter = IntentFilter().apply {
            addAction(ACTION_SERVICE_STARTED)
            addAction(ACTION_SERVICE_STOPPED)
        }
        registerReceiver(serviceStateReceiver, filter, RECEIVER_NOT_EXPORTED)

        // Observe one-shot ViewModel events
        lifecycleScope.launch {
            viewModel.events.collect { event ->
                when (event) {
                    is UiEvent.RequestScreenCapture -> launchScreenCapturePermission()
                }
            }
        }

        setContent {
            OnlineRPTransTheme {
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
        viewModel.refreshPermissionState(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(serviceStateReceiver)
    }

    // ── Screen capture permission ─────────────────────────────────────────────

    private fun launchScreenCapturePermission() {
        val projectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        screenCaptureLauncher.launch(projectionManager.createScreenCaptureIntent())
    }
}
