package com.onlinerptrans

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onlinerptrans.service.FloatingOverlayService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val isServiceRunning:    Boolean = false,
    val hasOverlayPermission: Boolean = false
)

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun refreshPermissionState(context: Context) {
        viewModelScope.launch {
            val hasPermission = Settings.canDrawOverlays(context)
            _uiState.update { it.copy(hasOverlayPermission = hasPermission) }
        }
    }

    fun startService(context: Context) {
        if (!Settings.canDrawOverlays(context)) return
        val intent = Intent(context, FloatingOverlayService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        _uiState.update { it.copy(isServiceRunning = true) }
    }

    fun stopService(context: Context) {
        val intent = Intent(context, FloatingOverlayService::class.java).apply {
            action = FloatingOverlayService.ACTION_STOP
        }
        context.startService(intent)
        _uiState.update { it.copy(isServiceRunning = false) }
    }

    fun onServiceStateChanged(running: Boolean) {
        _uiState.update { it.copy(isServiceRunning = running) }
    }
}
