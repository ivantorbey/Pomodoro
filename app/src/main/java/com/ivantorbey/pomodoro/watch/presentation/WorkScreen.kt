package com.ivantorbey.pomodoro.watch.presentation

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.ivantorbey.pomodoro.watch.data.SessionState
import kotlinx.coroutines.delay

@Composable
fun WorkScreen(
    state: SessionState,
    isAmbient: Boolean,
    onPauseNow: () -> Unit,
    onLongPress: () -> Unit,
) {
    val totalMs = state.effectiveWorkDurationMin() * 60_000L
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Tick : toutes les secondes en interactif, toutes les 30 s en ambiant.
    LaunchedEffect(isAmbient, state.endTimeMs) {
        while (true) {
            now = System.currentTimeMillis()
            delay(if (isAmbient) 30_000L else 1_000L)
        }
    }

    val remainingMs = maxOf(0L, state.endTimeMs - now)
    val progress = if (totalMs > 0) (1f - remainingMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f) else 0f
    val minutes = remainingMs / 60_000L
    val seconds = (remainingMs % 60_000L) / 1_000L
    val countdownText = if (isAmbient) "%02d".format(minutes) else "%02d:%02d".format(minutes, seconds)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures(onLongPress = { onLongPress() }) },
        contentAlignment = Alignment.Center
    ) {
        // Anneau de progression
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxSize().padding(4.dp),
            strokeWidth = 6.dp,
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = countdownText,
                style = if (isAmbient) MaterialTheme.typography.displaySmall
                        else MaterialTheme.typography.displayMedium,
                textAlign = TextAlign.Center
            )

            if (!isAmbient) {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onPauseNow,
                    modifier = Modifier.fillMaxWidth(0.75f)
                ) {
                    Text("Pause maintenant")
                }
            }
        }
    }
}
