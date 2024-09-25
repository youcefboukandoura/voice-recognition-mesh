package com.youcef.voicerecognitionmesh

import CircularWaveform
import android.Manifest
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var speechRecognizer: SpeechRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(
                this, android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }


        setContent {
            var recognizedKeyword by remember { mutableStateOf<String?>(null) }
            var amplitude by remember { mutableFloatStateOf(0f) }
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            setupSpeechRecognizer(
                onWordRecognized = { recognizedKeyword = it },
                onAmplitudeChange = { amplitude = it },
            )

            AudioVisualizerScreen(
                amplitude = { amplitude },
                detectedWord = { recognizedKeyword },
                onStartListening = { startListening() },
            )
        }
    }

    private fun setupSpeechRecognizer(
        onWordRecognized: (String) -> Unit, onAmplitudeChange: (Float) -> Unit
    ) {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {
                onAmplitudeChange(normalizeRms(rmsdB))
            }

            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                Log.d("MainActivity", "onEndOfSpeech")
            }

            override fun onError(error: Int) {}
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val detectedWord = matches?.firstOrNull()
                onWordRecognized(detectedWord.orEmpty())
            }

            override fun onPartialResults(partialResults: Bundle?) {
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        speechRecognizer.startListening(intent)
    }

    private fun normalizeRms(rmsdB: Float): Float {
        // Define the min/max bounds for RMS values (-2 dB to +10 dB)
        val minRms = -2f  // minimum rms value that might be reported
        val maxRms = 10f  // maximum rms value that might be reported

        // Clip the rmsdB to be within this range
        val clippedRms = rmsdB.coerceIn(minRms, maxRms)

        // Normalize the value between 0 and 1 for easier UI handling
        return (clippedRms - minRms) / (maxRms - minRms)
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}

@Composable
fun AudioVisualizerScreen(
    amplitude: () -> Float, detectedWord: () -> String?, onStartListening: () -> Unit
) {
    LaunchedEffect(Unit) {
        while (true) {
            onStartListening()
            delay(5000) // Restart listening every 5 seconds
        }
    }

    VoiceResponsiveUI(
        amplitude = amplitude(), detectedWord = detectedWord()
    )
}

@Composable
fun VoiceResponsiveUI(
    modifier: Modifier = Modifier, amplitude: Float = 0f, detectedWord: String? = null
) {
    val iconAlpha by animateFloatAsState(
        targetValue = if (detectedWord != null) 1f else 0f,
        animationSpec = tween(durationMillis = 500)
    )

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Circular Animation
        CircularWaveform(amplitude = amplitude)
        // Icon with animation
        if (detectedWord != null) {
            Log.d(TAG, "detectedWord $detectedWord")
            val icon = when (detectedWord.lowercase()) {
                "home" -> Icons.Filled.Home
                "menu" -> Icons.Filled.Menu
                "back" -> Icons.Filled.ArrowBack
                else -> null
            }

            if (icon != null) {
                val iconSize by animateFloatAsState(
                    targetValue = 100f, animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )

                Icon(
                    imageVector = icon,
                    contentDescription = detectedWord,
                    modifier = Modifier
                        .size(iconSize.dp)
                        .alpha(iconAlpha),
                    tint = Color(0xFF00FFFF)
                )
            }
        }
    }
}