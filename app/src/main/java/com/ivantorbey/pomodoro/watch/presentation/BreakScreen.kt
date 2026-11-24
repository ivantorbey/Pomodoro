package com.ivantorbey.pomodoro.watch.presentation

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Checkbox
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.ivantorbey.pomodoro.watch.PomodoroContent
import com.ivantorbey.pomodoro.watch.data.Phase
import com.ivantorbey.pomodoro.watch.data.SessionState
import kotlinx.coroutines.delay

@Composable
fun BreakScreen(
    state: SessionState,
    isAmbient: Boolean,
    onResume: () -> Unit,
    onToggleCheck: (Int) -> Unit,
    onLongPress: () -> Unit,
) {
    val isBreakDone = state.phase == Phase.BREAK_DONE
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(isAmbient, state.endTimeMs, isBreakDone) {
        if (!isBreakDone) {
            while (true) {
                now = System.currentTimeMillis()
                delay(if (isAmbient) 30_000L else 1_000L)
            }
        }
    }

    val remainingMs = if (isBreakDone) 0L else maxOf(0L, state.endTimeMs - now)
    val minutes = remainingMs / 60_000L
    val seconds = (remainingMs % 60_000L) / 1_000L

    val listState = rememberScalingLazyListState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures(onLongPress = { onLongPress() }) }
    ) {
        ScalingLazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // En-tête : titre + compte à rebours
            item {
                ListHeader {
                    if (isAmbient) {
                        Text(
                            text = if (isBreakDone) "Pause ✓" else "%02d".format(minutes),
                            style = MaterialTheme.typography.titleSmall,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        val headerText = if (isBreakDone) "Pause terminée"
                            else "Pause, lève-toi · %02d:%02d".format(minutes, seconds)
                        Text(
                            text = headerText,
                            style = MaterialTheme.typography.titleSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            if (!isAmbient) {
                // Tirage question
                if (state.drawnQuestion >= 0) {
                    item {
                        CheckItem(
                            emoji = "🧠",
                            label = "Question",
                            text = PomodoroContent.formatQuestion(
                                PomodoroContent.QUESTIONS[state.drawnQuestion],
                                state.workDurationMin
                            ),
                            checked = state.checkedMask and 1 != 0,
                            onToggle = { onToggleCheck(0) }
                        )
                    }
                }

                // Tirage action
                if (state.drawnAction >= 0) {
                    item {
                        CheckItem(
                            emoji = "🧘",
                            label = "Action",
                            text = PomodoroContent.ACTIONS[state.drawnAction],
                            checked = state.checkedMask and 2 != 0,
                            onToggle = { onToggleCheck(1) }
                        )
                    }
                }

                // Tirage lever
                if (state.drawnLever >= 0) {
                    item {
                        CheckItem(
                            emoji = "🚶",
                            label = "Lève-toi",
                            text = PomodoroContent.LEVER[state.drawnLever],
                            checked = state.checkedMask and 4 != 0,
                            onToggle = { onToggleCheck(2) }
                        )
                    }
                }

                // Bouton Reprendre
                item {
                    Spacer(Modifier.height(4.dp))
                    if (state.allChecked) {
                        Button(
                            onClick = onResume,
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            Text("Reprendre")
                        }
                    } else {
                        val n = state.uncheckedCount
                        Button(
                            onClick = {},
                            enabled = false,
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            Text(
                                text = "Encore $n case${if (n > 1) "s" else ""} à cocher",
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckItem(
    emoji: String,
    label: String,
    text: String,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onToggle() },
            modifier = Modifier.size(24.dp)
        )
        androidx.compose.foundation.layout.Column {
            Text(
                text = "$emoji $label",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
