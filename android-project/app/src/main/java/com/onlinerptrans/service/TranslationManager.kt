package com.onlinerptrans.service

import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

sealed class TranslationState {
    object Idle       : TranslationState()
    object Loading    : TranslationState()
    object Ready      : TranslationState()
    data class Result(val text: String)  : TranslationState()
    data class Error(val message: String): TranslationState()
}

sealed class ModelState {
    object NotDownloaded : ModelState()
    object Downloading   : ModelState()
    object Ready         : ModelState()
    data class Failed(val error: String) : ModelState()
}

class TranslationManager {

    private val options = TranslatorOptions.Builder()
        .setSourceLanguage(TranslateLanguage.RUSSIAN)
        .setTargetLanguage(TranslateLanguage.ARABIC)
        .build()

    private val translator = Translation.getClient(options)

    private val _translationState = MutableStateFlow<TranslationState>(TranslationState.Idle)
    val translationState: StateFlow<TranslationState> = _translationState.asStateFlow()

    private val _modelState = MutableStateFlow<ModelState>(ModelState.NotDownloaded)
    val modelState: StateFlow<ModelState> = _modelState.asStateFlow()

    /**
     * Downloads the RU→AR model if not already on-device.
     * Safe to call multiple times (ML Kit deduplicates downloads).
     */
    suspend fun downloadModelIfNeeded() {
        _modelState.value = ModelState.Downloading
        suspendCancellableCoroutine { cont ->
            translator.downloadModelIfNeeded()
                .addOnSuccessListener {
                    _modelState.value = ModelState.Ready
                    cont.resume(Unit)
                }
                .addOnFailureListener { e ->
                    _modelState.value = ModelState.Failed(e.message ?: "Model download failed")
                    cont.resumeWithException(e)
                }
        }
    }

    /**
     * Translates [text] from Russian → Arabic.
     * Updates [translationState] reactively. Returns the translated string
     * or throws if the model isn't ready or an error occurs.
     */
    suspend fun translate(text: String): String {
        if (text.isBlank()) {
            _translationState.value = TranslationState.Idle
            return ""
        }
        _translationState.value = TranslationState.Loading
        return suspendCancellableCoroutine { cont ->
            translator.translate(text)
                .addOnSuccessListener { translated ->
                    val result = TranslationState.Result(translated)
                    _translationState.value = result
                    cont.resume(translated)
                }
                .addOnFailureListener { e ->
                    val error = TranslationState.Error(e.message ?: "Translation error")
                    _translationState.value = error
                    cont.resumeWithException(e)
                }
        }
    }

    fun reset() {
        _translationState.value = TranslationState.Idle
    }

    fun close() {
        translator.close()
    }
}
