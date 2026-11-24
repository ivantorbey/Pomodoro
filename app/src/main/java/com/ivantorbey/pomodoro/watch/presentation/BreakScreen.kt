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
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.ivantorbey.pomodoro.watch.PomodoroContent
import com.ivantorbey.pomodoro.watch.data.Phase
import com.ivantorbey.pomodoro.watch.data.SessionState
import kotlinx.coroutines.delay

/** (bit dans checkedMask, emoji, label) pour chaque item de pause. */
private data class BreakItem(val bit: Int, val emoji: String, val label: String)
private val ITEMS = listOf(
    BreakItem(0, "🧠", "Question"),
    BreakItem(1, "🧘", "Action"),
    BreakItem(2, "🚶", "Lève-toi"),
)

@Composable
fun BreakScreen(
    state: SessionState,
    isAmbient: Boolean,
    onResume: () -> Unit,
    /** Coche le bit donné (ne bascule que si pas encore coché). */
    onCheckItem: (bit: Int) -> Unit,
    onLongPress: () -> Unit,
) {
    val isBreakDone = state.phase == Phase.BREAK_DONE
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
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
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
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

                // Texte de l'item courant (lecture seule, pas de case)
                item {
                    val item = ITEMS[page]
                    val text = when (item.bit) {
                        0 -> PomodoroContent.formatQuestion(
                            PomodoroContent.QUESTIONS[state.drawnQuestion.coerceAtLeast(0)],
                            state.workDurationMin
                        )
                        1 -> PomodoroContent.ACTIONS[state.drawnAction.coerceAtLeast(0)]
                        else -> PomodoroContent.LEVER[state.drawnLever.coerceAtLeast(0)]
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${item.emoji} ${item.label}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Bouton principal : valide l'item courant
                item {
                    Spacer(Modifier.height(6.dp))
                    val item = ITEMS[page]
                    val isLast = page == 2
                    Button(
                        onClick = {
                            // Coche l'item si pas encore fait
                            val alreadyChecked = state.checkedMask and (1 shl item.bit) != 0
                            if (!alreadyChecked) onCheckItem(item.bit)
                            if (isLast) onResume() else page++
                        },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        Text(if (isLast) "Reprendre" else "Suivant →")
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
