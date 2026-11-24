package com.ivantorbey.pomodoro.watch.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.foundation.AmbientAware
import androidx.wear.compose.foundation.AmbientState
import androidx.wear.compose.material3.AppScaffold
import com.ivantorbey.pomodoro.watch.data.Phase
import com.ivantorbey.pomodoro.watch.notification.NotificationHelper
import com.ivantorbey.pomodoro.watch.presentation.theme.PomodoroTheme
import androidx.compose.runtime.collectAsState

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.ensureChannel(this)
        setContent {
            PomodoroTheme {
                PomodoroApp()
            }
        }
    }
}

@Composable
fun PomodoroApp(vm: PomodoroViewModel = viewModel()) {
    val state by vm.sessionState.collectAsState()
    var showStopConfirm by remember { mutableStateOf(false) }

    AppScaffold {
        AmbientAware { ambientStateUpdate ->
            val isAmbient = ambientStateUpdate.ambientState is AmbientState.Ambient

            if (showStopConfirm) {
                StopConfirmScreen(
                    onConfirm = {
                        vm.stopSession()
                        showStopConfirm = false
                    },
                    onCancel = { showStopConfirm = false }
                )
            } else {
                when (state.phase) {
                    Phase.IDLE -> HomeScreen(
                        state = state,
                        onStart = { vm.startSession() },
                        onSetWorkDuration = { vm.setWorkDuration(it) },
                        onSetBreakDuration = { vm.setBreakDuration(it) },
                        onLongPress = { vm.toggleTestMode() }
                    )

                    Phase.WORK -> WorkScreen(
                        state = state,
                        isAmbient = isAmbient,
                        onPauseNow = { vm.pauseNow() },
                        onLongPress = { showStopConfirm = true }
                    )

                    Phase.BREAK, Phase.BREAK_DONE -> BreakScreen(
                        state = state,
                        isAmbient = isAmbient,
                        onResume = { vm.resumeWork() },
                        onToggleCheck = { vm.toggleCheck(it) },
                        onLongPress = { showStopConfirm = true }
                    )
                }
            }
        }
    }
}
