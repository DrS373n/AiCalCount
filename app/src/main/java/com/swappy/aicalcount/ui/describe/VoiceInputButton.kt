package com.swappy.aicalcount.ui.describe

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.swappy.aicalcount.R

/**
 * A button that starts speech recognition and passes the transcribed text to [onResult].
 * When [hasVoicePermission] is false, clicking calls [onRequestVoicePermission] so the host can request RECORD_AUDIO.
 */
@Composable
fun VoiceInputButton(
    enabled: Boolean,
    hasVoicePermission: Boolean,
    onResult: (String) -> Unit,
    onRequestVoicePermission: () -> Unit = {},
    onError: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val speechRecognizer = remember(context) {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else null
    }

    IconButton(
        onClick = {
            if (!hasVoicePermission) {
                onRequestVoicePermission()
                return@IconButton
            }
            if (speechRecognizer == null) {
                onError?.invoke()
                return@IconButton
            }
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, context.getString(R.string.voice_prompt))
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            }
            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onError(error: Int) {
                    if (error != SpeechRecognizer.ERROR_NO_MATCH) {
                        onError?.invoke()
                    }
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull()?.trim()
                    if (!text.isNullOrBlank()) {
                        onResult(text)
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
            speechRecognizer.startListening(intent)
        },
        enabled = enabled && (speechRecognizer != null || !hasVoicePermission),
        modifier = modifier.size(48.dp),
    ) {
        Icon(
            Icons.Outlined.Mic,
            contentDescription = stringResource(R.string.voice_input_desc),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}
