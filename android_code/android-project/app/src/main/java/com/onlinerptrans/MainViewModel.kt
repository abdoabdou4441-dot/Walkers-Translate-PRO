package com.onlinerptrans

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlinerptrans.service.FloatingOverlayService
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val isServiceRunning:        Boolean = false,
    val hasOverlayPermission:    Boolean = false,
    val hasScreenCaptureConsent: Boolean = false
)

// One-shot events sent from ViewModel → Activity
sealed class UiEvent {
    data object RequestScreenCapture : UiEvent()
}

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    // ── Permission state ──────────────────────────────────────────────────────

    fun refreshPermissionState(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(hasOverlayPermission = Settings.canDrawOverlays(context)) }
        }
    }

    // ── Service control ───────────────────────────────────────────────────────

    /**
     * Called when the user taps "Start Service".
     * Emits [UiEvent.RequestScreenCapture] so the Activity can launch the
     * system screen-capture permission dialog and forward the result back via
     * [startServiceWithProjection].
     */
    fun requestStartService(context: Context) {
        if (!Settings.canDrawOverlays(context)) return
        viewModelScope.launch {
            _events.send(UiEvent.RequestScreenCapture)
        }
    }

    /**
     * Called from the Activity once the user has accepted the screen-capture
     * permission dialog. Starts [FloatingOverlayService] with the projection
     * credentials forwarded as extras.
     */
    fun startServiceWithProjection(context: Context, resultCode: Int, data: Intent) {
        val intent = Intent(context, FloatingOverlayService::class.java).apply {
            putExtra(FloatingOverlayService.EXTRA_RESULT_CODE, resultCode)
            putExtra(FloatingOverlayService.EXTRA_RESULT_DATA, data)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        _uiState.update { it.copy(hasScreenCaptureConsent = true) }
    }

    fun stopService(context: Context) {
        val intent = Intent(context, FloatingOverlayService::class.java).apply {
            action = FloatingOverlayService.ACTION_STOP
        }
        context.startService(intent)
        _uiState.update { it.copy(isServiceRunning = false, hasScreenCaptureConsent = false) }
    }

    fun onServiceStateChanged(running: Boolean) {
        _uiState.update { it.copy(isServiceRunning = running) }
    }
}
