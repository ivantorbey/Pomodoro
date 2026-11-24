package com.ivantorbey.pomodoro.watch.presentation

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.ivantorbey.pomodoro.watch.data.MAX_BREAK_MIN
import com.ivantorbey.pomodoro.watch.data.MAX_WORK_MIN
import com.ivantorbey.pomodoro.watch.data.MIN_BREAK_MIN
import com.ivantorbey.pomodoro.watch.data.MIN_WORK_MIN
import com.ivantorbey.pomodoro.watch.data.SessionState

/** Champ sélectionné sur l'écran d'accueil, réagissant à la couronne. */
private enum class Field { WORK, BREAK }

@Composable
fun HomeScreen(
    state: SessionState,
    onStart: () -> Unit,
    onSetWorkDuration: (Int) -> Unit,
    onSetBreakDuration: (Int) -> Unit,
    onLongPress: () -> Unit,
) {
    var selectedField by remember { mutableStateOf(Field.WORK) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onRotaryScrollEvent { event ->
                val delta = if (event.verticalScrollPixels > 0) 1 else -1
                when (selectedField) {
                    Field.WORK -> {
                        val newVal = (state.workDurationMin + delta).coerceIn(MIN_WORK_MIN, MAX_WORK_MIN)
                        onSetWorkDuration(newVal)
                    }
                    Field.BREAK -> {
                        val newVal = (state.breakDurationMin + delta).coerceIn(MIN_BREAK_MIN, MAX_BREAK_MIN)
                        onSetBreakDuration(newVal)
                    }
                }
                true
            }
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { onLongPress() })
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            if (state.testMode) {
                Text(
                    text = "MODE TEST",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
            }

            // Sélecteur Travail
            DurationRow(
                label = "Travail",
                value = state.workDurationMin,
                unit = "min",
                selected = selectedField == Field.WORK,
                onClick = { selectedField = Field.WORK }
            )
            Spacer(Modifier.height(6.dp))

            // Sélecteur Pause
            DurationRow(
                label = "Pause",
                value = state.breakDurationMin,
                unit = "min",
                selected = selectedField == Field.BREAK,
                onClick = { selectedField = Field.BREAK }
            )
            Spacer(Modifier.height(10.dp))

            Button(
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(0.75f)
            ) {
                Text("Démarrer")
            }
        }
    }
}

@Composable
private fun DurationRow(
    label: String,
    value: Int,
    unit: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val color = if (selected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .pointerInput(Unit) { detectTapGestures(onTap = { onClick() }) }
            .padding(horizontal = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
        Text(
            text = "$value $unit",
            style = MaterialTheme.typography.titleMedium,
            color = color
        )
    }
}
