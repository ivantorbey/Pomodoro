package com.ivantorbey.pomodoro.watch.presentation

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.wear.compose.material3.ButtonDefaults
import androidx.wear.compose.material3.CheckboxButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.ivantorbey.pomodoro.watch.PomodoroContent
import com.ivantorbey.pomodoro.watch.data.Phase
import com.ivantorbey.pomodoro.watch.data.SessionState
import kotlinx.coroutines.delay

/** Index des items de pause. */
private val ITEMS = listOf(
    Triple(0, "🧠", "Question"),   // bit 0, checkedMask & 1
    Triple(1, "🧘", "Action"),      // bit 1, checkedMask & 2
    Triple(2, "🚶", "Lève-toi"),   // bit 2, checkedMask & 4
)

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
    // Page courante : 0=Question, 1=Action, 2=Lever
    var page by remember { mutableIntStateOf(0) }
    val listState = rememberScalingLazyListState()

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { detectTapGestures(onLongPress = { onLongPress() }) }
    ) {
        if (isAmbient) {
            // Mode ambiant : uniquement le temps restant ou le statut
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (isBreakDone) "Pause ✓" else "%02d".format(minutes),
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            ScalingLazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // En-tête : timer + indicateur de page
                item {
                    ListHeader {
                        val timerText = if (isBreakDone) "Pause terminée"
                            else "%02d:%02d".format(minutes, seconds)
                        Text(
                            text = "$timerText  ${page + 1}/3",
                            style = MaterialTheme.typography.titleSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Item courant
                item {
                    val (bit, emoji, label) = ITEMS[page]
                    val checked = state.checkedMask and (1 shl bit) != 0
                    val itemText = when (bit) {
                        0 -> PomodoroContent.formatQuestion(
                            PomodoroContent.QUESTIONS[state.drawnQuestion.coerceAtLeast(0)],
                            state.workDurationMin
                        )
                        1 -> PomodoroContent.ACTIONS[state.drawnAction.coerceAtLeast(0)]
                        else -> PomodoroContent.LEVER[state.drawnLever.coerceAtLeast(0)]
                    }
                    CheckboxButton(
                        checked = checked,
                        onCheckedChange = { onToggleCheck(bit) },
                        label = {
                            Text("$emoji $label", style = MaterialTheme.typography.labelSmall)
                        },
                        secondaryLabel = {
                            Text(itemText, style = MaterialTheme.typography.bodySmall)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Bouton navigation
                item {
                    Spacer(Modifier.height(4.dp))
                    when {
                        // Pages 0 et 1 : bouton Suivant
                        page < 2 -> Button(
                            onClick = { page++ },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            Text("Suivant →")
                        }

                        // Page 2, toutes les cases cochées : Reprendre
                        state.allChecked -> Button(
                            onClick = onResume,
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            Text("Reprendre")
                        }

                        // Page 2, cases manquantes : bouton désactivé
                        else -> {
                            val n = state.uncheckedCount
                            Button(
                                onClick = {},
                                enabled = false,
                                modifier = Modifier.fillMaxWidth(0.85f)
                            ) {
                                Text(
                                    "Encore $n case${if (n > 1) "s" else ""} à cocher",
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Bouton Retour (pages 1 et 2)
                if (page > 0) {
                    item {
                        Button(
                            onClick = { page-- },
                            modifier = Modifier.fillMaxWidth(0.85f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Text("← Retour")
                        }
                    }
                }
            }
        }
    }
}
