package com.ivantorbey.pomodoro.watch.presentation

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.wear.compose.material3.CheckboxButton
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
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // En-tête : titre + compte à rebours
            item {
                ListHeader {
                    val headerText = when {
                        isAmbient && isBreakDone -> "Pause ✓"
                        isAmbient -> "%02d".format(minutes)
                        isBreakDone -> "Pause terminée"
                        else -> "Pause, lève-toi · %02d:%02d".format(minutes, seconds)
                    }
                    Text(
                        text = headerText,
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (!isAmbient) {
                // Tirage question
                if (state.drawnQuestion >= 0) {
                    item {
                        CheckboxButton(
                            checked = state.checkedMask and 1 != 0,
                            onCheckedChange = { onToggleCheck(0) },
                            label = {
                                Text("🧠 Question", style = MaterialTheme.typography.labelSmall)
                            },
                            secondaryLabel = {
                                Text(
                                    PomodoroContent.formatQuestion(
                                        PomodoroContent.QUESTIONS[state.drawnQuestion],
                                        state.workDurationMin
                                    ),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Tirage action
                if (state.drawnAction >= 0) {
                    item {
                        CheckboxButton(
                            checked = state.checkedMask and 2 != 0,
                            onCheckedChange = { onToggleCheck(1) },
                            label = {
                                Text("🧘 Action", style = MaterialTheme.typography.labelSmall)
                            },
                            secondaryLabel = {
                                Text(
                                    PomodoroContent.ACTIONS[state.drawnAction],
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Tirage lever
                if (state.drawnLever >= 0) {
                    item {
                        CheckboxButton(
                            checked = state.checkedMask and 4 != 0,
                            onCheckedChange = { onToggleCheck(2) },
                            label = {
                                Text("🚶 Lève-toi", style = MaterialTheme.typography.labelSmall)
                            },
                            secondaryLabel = {
                                Text(
                                    PomodoroContent.LEVER[state.drawnLever],
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
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
