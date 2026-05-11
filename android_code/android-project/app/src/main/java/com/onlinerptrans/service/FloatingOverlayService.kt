package com.onlinerptrans.service

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.onlinerptrans.MainActivity
import com.onlinerptrans.R
import com.onlinerptrans.ui.overlay.FloatingBubbleContent
import com.onlinerptrans.ui.overlay.TranslationPanelContent
import com.onlinerptrans.ui.theme.OnlineRPTransTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FloatingOverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    // ── Lifecycle boilerplate for ComposeView outside an Activity ────────────
    private val lifecycleRegistry = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry

    private val _viewModelStore = ViewModelStore()
    override val viewModelStore: ViewModelStore get() = _viewModelStore

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    // ── Service state ────────────────────────────────────────────────────────
    private lateinit var windowManager: WindowManager
    private var bubbleView: ComposeView? = null
    private var panelView: ComposeView? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val translationManager = TranslationManager()
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    // ── UI state (read-only from OCR — no manual input) ──────────────────────
    var ocrText       by mutableStateOf("")
    var outputText    by mutableStateOf("")
    var isTranslating by mutableStateOf(false)
    var isCaptureActive by mutableStateOf(false)
    var panelScale    by mutableFloatStateOf(1f)

    // PRO Features State
    var isAutoTranslateEnabled by mutableStateOf(false)
    var bubbleSize by mutableFloatStateOf(60f) // dp
    var bubbleAlpha by mutableFloatStateOf(1f)
    val translationHistory = mutableStateListOf<HistoryEntry>()

    // ── Screen capture state ─────────────────────────────────────────────────
    private var mediaProjection: MediaProjection? = null
    private var imageReader: ImageReader? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var captureJob: Job? = null
    private var autoTranslateJob: Job? = null

    data class HistoryEntry(val original: String, val translated: String, val timestamp: Long = System.currentTimeMillis())

    companion object {
        const val ACTION_STOP       = "com.onlinerptrans.STOP_SERVICE"
        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_RESULT_DATA = "result_data"
        const val CHANNEL_ID        = "floating_overlay_channel"
        const val NOTIFICATION_ID   = 1001
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        sendBroadcast(Intent(MainActivity.ACTION_SERVICE_STARTED))
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        serviceScope.launch {
            runCatching { translationManager.downloadModelIfNeeded() }
        }

        addBubble()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopSelf()
            else -> {
                val resultCode = intent?.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
                @Suppress("DEPRECATION")
                val resultData = intent?.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)
                if (resultCode == Activity.RESULT_OK && resultData != null) {
                    val mgr = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                    mediaProjection = mgr.getMediaProjection(resultCode, resultData)
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        stopCapture()
        textRecognizer.close()
        serviceScope.cancel()
        translationManager.close()
        removeBubble()
        removePanel()
        _viewModelStore.clear()
        sendBroadcast(Intent(MainActivity.ACTION_SERVICE_STOPPED))
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // ── Bubble ───────────────────────────────────────────────────────────────

    private fun addBubble() {
        val params = bubbleLayoutParams(100, 200)
        bubbleView = ComposeView(this).also { view ->
            view.setViewTreeLifecycleOwner(this)
            view.setViewTreeViewModelStoreOwner(this)
            view.setViewTreeSavedStateRegistryOwner(this)
            view.setContent {
                OnlineRPTransTheme {
                    FloatingBubbleContent(
                        size = bubbleSize,
                        alpha = bubbleAlpha,
                        onBubbleClick = { onBubbleToggle() }
                    )
                }
            }
            setupDragListener(view, params)
            windowManager.addView(view, params)
        }
    }

    private fun setupDragListener(view: ComposeView, params: WindowManager.LayoutParams) {
        var initialX = 0; var initialY = 0
        var touchX = 0f; var touchY = 0f
        var isDragging = false

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x; initialY = params.y
                    touchX = event.rawX; touchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - touchX).toInt()
                    val dy = (event.rawY - touchY).toInt()
                    if (!isDragging && (Math.abs(dx) > 5 || Math.abs(dy) > 5)) isDragging = true
                    if (isDragging) {
                        params.x = initialX + dx
                        params.y = initialY + dy
                        windowManager.updateViewLayout(view, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) view.performClick()
                    true
                }
                else -> false
            }
        }
    }

    private fun removeBubble() {
        bubbleView?.let { runCatching { windowManager.removeView(it) } }
        bubbleView = null
    }

    // ── Toggle: 1st tap = show panel + start OCR; 2nd tap = hide + stop ─────

    private fun onBubbleToggle() {
        if (isCaptureActive) {
            if (!isAutoTranslateEnabled) {
                removePanel()
                stopCapture()
                isCaptureActive = false
                ocrText = ""
                outputText = ""
            } else {
                // In auto-mode, toggle panel visibility but keep capture running
                if (panelView == null) addPanel() else removePanel()
            }
        } else {
            addPanel()
            startCapture()
            isCaptureActive = true
        }
    }

    fun toggleAutoTranslate(enabled: Boolean) {
        isAutoTranslateEnabled = enabled
        if (enabled) {
            startAutoTranslateLoop()
        } else {
            autoTranslateJob?.cancel()
        }
    }

    fun updateBubbleSize(newSize: Float) {
        bubbleSize = newSize
        bubbleView?.let { view ->
            val params = view.layoutParams as WindowManager.LayoutParams
            // We don't strictly need to update WRAP_CONTENT params unless we want to force a resize
            // but the ComposeView will resize itself. We just need to ensure the layout is updated.
            windowManager.updateViewLayout(view, params)
        }
    }

    fun updateBubbleAlpha(newAlpha: Float) {
        bubbleAlpha = newAlpha
    }

    private fun startAutoTranslateLoop() {
        autoTranslateJob?.cancel()
        autoTranslateJob = serviceScope.launch {
            while (isActive) {
                if (isAutoTranslateEnabled && isCaptureActive) {
                    // Trigger a capture if not already running or just wait for the next frame
                    // The existing captureJob already runs every 1500ms
                    // We can just ensure it's running
                }
                delay(3000L) // 3-5 seconds as requested
            }
        }
    }

    // ── Translation Panel ────────────────────────────────────────────────────

    private fun addPanel() {
        if (panelView != null) return
        val params = panelLayoutParams()
        panelView = ComposeView(this).also { view ->
            view.setViewTreeLifecycleOwner(this)
            view.setViewTreeViewModelStoreOwner(this)
            view.setViewTreeSavedStateRegistryOwner(this)
            view.setContent {
                OnlineRPTransTheme {
                    TranslationPanelContent(service = this@FloatingOverlayService)
                }
            }
            windowManager.addView(view, params)
        }
    }

    private fun removePanel() {
        panelView?.let { runCatching { windowManager.removeView(it) } }
        panelView = null
    }

    // ── Screen Capture & OCR ─────────────────────────────────────────────────

    private fun startCapture() {
        val projection = mediaProjection ?: return
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealMetrics(metrics)

        val width   = metrics.widthPixels
        val height  = metrics.heightPixels
        val density = metrics.densityDpi

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        virtualDisplay = projection.createVirtualDisplay(
            "OrtCapture", width, height, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader!!.surface, null, null
        )

        captureJob = serviceScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(1500L)
                val image: Image? = imageReader?.acquireLatestImage()
                if (image != null) {
                    val bitmap = imageToBitmap(image)
                    image.close()
                    bitmap?.let { processOcrFrame(it) }
                }
            }
        }
    }

    private fun stopCapture() {
        captureJob?.cancel()
        captureJob = null
        virtualDisplay?.release()
        virtualDisplay = null
        imageReader?.close()
        imageReader = null
    }

    private fun imageToBitmap(image: Image): Bitmap? = runCatching {
        val plane      = image.planes[0]
        val buffer     = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride  = plane.rowStride
        val rowPadding = rowStride - pixelStride * image.width
        val bmp = Bitmap.createBitmap(
            image.width + rowPadding / pixelStride,
            image.height,
            Bitmap.Config.ARGB_8888
        )
        bmp.copyPixelsFromBuffer(buffer)
        bmp
    }.getOrNull()

    private var lastBitmap: Bitmap? = null

    private suspend fun processOcrFrame(bitmap: Bitmap) {
        // Optimization: Skip OCR if the frame hasn't changed significantly
        if (isSameAsLast(bitmap)) {
            return
        }
        lastBitmap = bitmap

        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val result = runCatching {
            Tasks.await(textRecognizer.process(inputImage))
        }.getOrNull() ?: return

        val detected = result.text.trim()
        if (detected.isNotBlank() && detected != ocrText) {
            withContext(Dispatchers.Main) { ocrText = detected }
            performTranslation(detected)
        }
    }

    private fun isSameAsLast(newBitmap: Bitmap): Boolean {
        val last = lastBitmap ?: return false
        if (newBitmap.width != last.width || newBitmap.height != last.height) return false
        
        // Simple pixel sampling for speed
        val step = 20 
        for (y in 0 until newBitmap.height step step) {
            for (x in 0 until newBitmap.width step step) {
                if (newBitmap.getPixel(x, y) != last.getPixel(x, y)) return false
            }
        }
        return true
    }

    fun performTranslation(text: String) {
        if (text.isBlank()) return
        serviceScope.launch {
            isTranslating = true
            runCatching {
                val translated = translationManager.translate(text)
                outputText = translated
                
                // Add to history
                if (translationHistory.none { it.original == text }) {
                    translationHistory.add(0, HistoryEntry(text, translated))
                    if (translationHistory.size > 20) {
                        translationHistory.removeLast()
                    }
                }
            }.onFailure { e ->
                outputText = "خطأ في الترجمة: ${e.message}"
            }
            isTranslating = false
        }
    }

    // ── WindowManager layout params ──────────────────────────────────────────
    // FLAG_NOT_FOCUSABLE on EVERYTHING = zero game interference (Anti-AFK)

    @Suppress("DEPRECATION")
    private fun bubbleLayoutParams(x: Int, y: Int) = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.TOP or Gravity.START
        this.x = x; this.y = y
    }

    @Suppress("DEPRECATION")
    private fun panelLayoutParams() = WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT
    ).apply {
        gravity = Gravity.BOTTOM
    }

    // ── Notification ─────────────────────────────────────────────────────────

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_desc)
            setShowBadge(false)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = PendingIntent.getService(
            this, 1,
            Intent(this, FloatingOverlayService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(openIntent)
            .addAction(0, "إيقاف", stopIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
