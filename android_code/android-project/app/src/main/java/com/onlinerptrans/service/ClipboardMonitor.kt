package com.onlinerptrans.service

import android.content.ClipboardManager
import android.content.Context
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Emits clipboard text changes via a cold [Flow].
 * Android 10+ restricts clipboard reads to the focused app or IME,
 * so readings happen only when the user explicitly copies while our
 * foreground service is the listener registered via [ClipboardManager].
 *
 * Specifically, the [ClipboardManager.OnPrimaryClipChangedListener] fires
 * on every copy action even when another app is in focus — the text
 * read at that moment is valid because it's the freshly set clipboard data.
 */
class ClipboardMonitor(private val context: Context) {

    // Minimum Cyrillic characters to consider it a Russian text worth translating
    private val cyrillicRegex = Regex("[а-яА-ЯёЁ]")
    private val minCyrillicCount = 2

    /**
     * Returns a [Flow] that emits clipboard text whenever:
     * 1. The primary clip changes
     * 2. The new text contains sufficient Cyrillic characters (likely Russian)
     */
    fun observeRussianClipboard(): Flow<String> = callbackFlow {
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val listener = ClipboardManager.OnPrimaryClipChangedListener {
            val clip = clipboardManager.primaryClip ?: return@OnPrimaryClipChangedListener
            if (clip.itemCount == 0) return@OnPrimaryClipChangedListener

            val text = clip.getItemAt(0).coerceToText(context).toString()
            val cyrillicCount = cyrillicRegex.findAll(text).count()

            if (cyrillicCount >= minCyrillicCount) {
                trySend(text)
            }
        }

        clipboardManager.addPrimaryClipChangedListener(listener)

        awaitClose {
            clipboardManager.removePrimaryClipChangedListener(listener)
        }
    }
}
